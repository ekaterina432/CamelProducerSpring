package camel.example.components;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static camel.example.config.CamelConfiguration.createRoutBuider;
import static camel.example.config.CamelConfiguration.setUpComponent;

@Component
public class Producer {
    private static final Logger LOG = LoggerFactory.getLogger(Producer.class);
    private final CamelContext camelContext;
    private final String kafkaBrokers;
    private final String producerTopic;

    @Autowired
    public Producer(CamelContext camelContext,
                    @Value("${kafka.broker}") String kafkaBrokers,
                    @Value("${producer.topic}") String producerTopic){
        this.camelContext = camelContext;
        this.producerTopic = producerTopic;
        this.kafkaBrokers = kafkaBrokers;
    }

    public void run() throws Exception{
        String testMes = "Test message from producer" + Calendar.getInstance().getTime();
        try (CamelContext camelContext = new DefaultCamelContext()) {
            camelContext.getPropertiesComponent().setLocation("classpath:application.properties");
            setUpComponent(camelContext);
            camelContext.addRoutes(createRoutBuider());

            try (ProducerTemplate producerTemplate = camelContext.createProducerTemplate()) {
                camelContext.start();
                Map<String, Object> headers = new HashMap<>();
                headers.put(KafkaConstants.PARTITION_KEY, 0);
                headers.put(KafkaConstants.KEY, "1");
                producerTemplate.sendBodyAndHeaders("direct:KafkaStart", testMes, headers);
                testMes = "TOPIC " + testMes;
                headers.put(KafkaConstants.KEY, "2");
                headers.put(KafkaConstants.TOPIC, "TestLog");
                producerTemplate.sendBodyAndHeaders("direct:kafkaStartNoTopic", testMes, headers);


            }
        }
        LOG.info("SUCCESSFULLY!");
        Thread.sleep(50_000);
    }

}
