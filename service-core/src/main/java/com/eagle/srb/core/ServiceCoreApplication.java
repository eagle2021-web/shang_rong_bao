package com.eagle.srb.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author eagle2020
 */
@SpringBootApplication
@ComponentScan({"com.eagle"})
public class ServiceCoreApplication {
    /**
     * http://localhost:8110/swagger-ui.html 打开swagger
     * @param args 0
     */
    public static void main(String[] args) {
        SpringApplication.run(ServiceCoreApplication.class, args);
    }
}