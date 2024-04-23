package com.lhx.goodchoice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@SpringBootApplication
@MapperScan("com.lhx.goodchoice.mapper")
@EnableScheduling
public class GoodChoiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoodChoiceApplication.class, args);
    }

}
