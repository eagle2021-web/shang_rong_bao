package com.eagle.srb.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author eagle2020
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ServiceGatewayApplication {
    // http://localhost/service-core/admin/core/integralGrade/list
    public static void main(String[] args) {
        SpringApplication.run(ServiceGatewayApplication.class, args);
    }
}