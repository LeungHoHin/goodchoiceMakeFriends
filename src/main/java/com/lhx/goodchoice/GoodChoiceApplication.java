package com.lhx.goodchoice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lhx.goodchoice.mapper")
public class GoodChoiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoodChoiceApplication.class, args);
    }

}
