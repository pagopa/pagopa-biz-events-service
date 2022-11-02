package it.gov.pagopa.bizeventsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.DependsOn;

@SpringBootApplication
@DependsOn("expressionResolver")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
