package it.gov.pagopa.bizeventsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.DependsOn;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@DependsOn("expressionResolver")
@EnableFeignClients
@EnableRetry
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
