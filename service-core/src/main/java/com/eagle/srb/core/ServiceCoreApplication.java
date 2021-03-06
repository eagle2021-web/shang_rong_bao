package com.eagle.srb.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author eagle2020
 */
//(exclude = DataSourceAutoConfiguration.class)
@SpringBootApplication
@ComponentScan({"com.eagle"})
public class ServiceCoreApplication {
    /**
     * http://localhost:8110/swagger-ui.html 打开swagger
     * http://localhost:8110/doc.html 打开swagger token
     * http://localhost:8848/nacos
     * @param args null
     */
    public static void main(String[] args) {
        SpringApplication.run(ServiceCoreApplication.class, args);
    }
}