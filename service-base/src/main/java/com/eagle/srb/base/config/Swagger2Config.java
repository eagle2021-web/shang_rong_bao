package com.eagle.srb.base.config;

import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class Swagger2Config {
    @Bean
    public Docket adminApiConfig() {
        // swagger ui 分类管理
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("adminApi")
                .apiInfo(adminApiInfo())
                .select()
                .paths(Predicates.and(PathSelectors.regex("/admin/.*")))
                .build();
    }

    @Bean
    public Docket webApiConfig() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("webApi")
                .apiInfo(webApiInfo())
                .select()
                .paths(Predicates.and(PathSelectors.regex("/api/.*"))) //api跟controller类的RequestMapping设置有关
                .build();
    }
    private ApiInfo adminApiInfo(){
        return new ApiInfoBuilder().title("尚荣宝web API文档")
                .description("本文档描述了尚荣宝后台管理系统的各个模块的接口的调用方式")
                .version("1.6")
                .contact(new Contact("eagle", "http://www.chenwangying.cn:8080", "chenwangying16@163.com"))
                .build();
    }
    private ApiInfo webApiInfo(){
        return new ApiInfoBuilder().title("尚荣宝后台管理系统API文档")
                .description("本文档描述了尚荣宝网站统各个模块的接口的调用方式")
                .version("1.6")
                .contact(new Contact("eagle", "http://www.chenwangying.cn:8080", "chenwangying16@163.com"))
                .build();
    }
}
