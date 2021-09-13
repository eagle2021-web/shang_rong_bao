package com.eagle.srb.sms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.eagle.srb", "com.eagle.common"})
@EnableFeignClients
public class ServiceSmsApplication {
    //http://localhost:8120/swagger-ui.html 打开swagger
    public static void main(String[] args) {
        SpringApplication.run(ServiceSmsApplication.class, args);
    }
}