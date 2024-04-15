package camel.example.config;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.component.ComponentsBuilderFactory;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource("classpath:application.properties")
public class CamelConfiguration {

    @Value("${kafka.brokers}")
    private String kafkaBrokers;

    private static final String DIRECT_KAFKA_START = "direct:KafkaStart";
    public static final String HEADERS = "${headers}";

    @Bean
    public RouteBuilder kafkaRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(DIRECT_KAFKA_START).routeId("DirectToKafka")
                        .to("kafka:{{producer.topic}}")
                        .log("${headers}");

                //Фиктивный топик - это топик, который используется в качестве промежуточной точки при тестировании
                // или отладке приложения, когда реальный топик не требуется или не желателен.
                from("direct:kafkaStartNoTopic").routeId("kafkaStartNoTopic")
                        .to("kafka:dummy")
                        .log(HEADERS);

            }
        };
    }


    @Bean
    CamelContextConfiguration contextConfiguration(RouteBuilder kafkaRouteBuilder) {
        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart(CamelContext camelContext) {
                // Настройка компонента Kafka
                setUpKafkaComponent(camelContext);

            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {
                try {
                    camelContext.start();

                    String testMes = "Test message from producer" + Calendar.getInstance().getTime();
                    ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
                    Map<String, Object> headers = new HashMap<>();
                    headers.put(KafkaConstants.PARTITION_KEY, 0);
                    headers.put(KafkaConstants.KEY, "1");
                    producerTemplate.sendBodyAndHeaders(DIRECT_KAFKA_START, testMes, headers);
                    testMes = "TOPIC " + testMes;
                    headers.put(KafkaConstants.KEY, "2");
                    headers.put(KafkaConstants.TOPIC, "TestLog");
                    producerTemplate.sendBodyAndHeaders("direct:kafkaStartNoTopic", testMes, headers);

                    Thread.sleep(50_000);
                    camelContext.addStartupListener((camelContext1, alreadyStarted) -> {
                        if (!alreadyStarted) {
                            System.out.println("Camel application started, reading messages from Kafka...");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            private void setUpKafkaComponent(CamelContext camelContext) {
                ComponentsBuilderFactory.kafka()
                        .brokers(kafkaBrokers)
                        .register(camelContext, "kafka");
            }
        };
    }
}