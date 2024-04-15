package camel.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages ="camel.example")
public class CamelProducerSpringApp {
    public static void main(String[] args) {
        SpringApplication.run(CamelProducerSpringApp.class, args);
    }
}
