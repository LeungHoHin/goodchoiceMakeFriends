package com.lhx.goodchoice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * 接口文档配置
 *
 * @author 梁浩轩
 */
@Configuration
@EnableSwagger2WebMvc
@Profile("dev")
public class Knife4jConfiguration {

    @Bean(value = "defaultApi2")
    public Docket defaultApi2() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("良好选组队交友平台")
                        .description("接口文档")
                        .termsOfServiceUrl("https://github.com/LeungHoHin")
                        .contact("leunghohin@163.com")
                        .version("1.0")
                        .build())
                //分组名称
                .groupName("1.0")
                .select()
                //这里指定Controller扫描包路径
                .apis(RequestHandlerSelectors.basePackage("com.lhx.goodchoice.controller"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }
}