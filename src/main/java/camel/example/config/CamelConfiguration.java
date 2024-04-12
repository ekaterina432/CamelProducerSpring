package camel.example.config;

import camel.example.components.Producer;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.component.ComponentsBuilderFactory;
import org.apache.camel.impl.DefaultCamelContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelConfiguration {

    private static final String DIRECT_KAFKA_START = "direct:KafkaStart";
    public static final String HEADERS = "${headers}";

    @Value("${kafka.brokers}")
    private String kafkaBrokers;

    @Value("${producer.topic}")
    private String producerTopic;

    @Bean
    public CamelContext camelContext() throws Exception {
        DefaultCamelContext camelContext = new DefaultCamelContext();
        camelContext.getPropertiesComponent().setLocation("classpath:application.properties");
        setUpComponent(camelContext);
        camelContext.addRoutes(createRoutBuider());
        return camelContext;

    }

    public static void  setUpComponent(CamelContext camelContext){
        ComponentsBuilderFactory.kafka()
                .brokers("{{kafka.brokers}}")
                .register(camelContext, "kafka");
    }


    public static RouteBuilder createRoutBuider(){
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(DIRECT_KAFKA_START).routeId("DirectToKafka")
                        .to("kafka:{{producer.topic}}")
                        .log(HEADERS);

                //Фиктивный топик - это топик, который используется в качестве промежуточной точки при тестировании
                // или отладке приложения, когда реальный топик не требуется или не желателен.
                from("direct:kafkaStartNoTopic").routeId("kafkaStartNoTopic")
                        .to("kafka:dummySpring")
                        .log(HEADERS);

            }
        };
    }
    @Bean
    public Producer producer(CamelContext camelContext){
        return new Producer(camelContext, kafkaBrokers, producerTopic);
    }

}
