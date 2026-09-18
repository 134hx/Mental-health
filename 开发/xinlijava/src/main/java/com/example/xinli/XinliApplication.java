package com.example.xinli;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.xinli.mapper")
public class XinliApplication {
    public static void main(String[] args) {
        SpringApplication.run(XinliApplication.class, args);
    }
}

