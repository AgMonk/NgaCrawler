package com.gin.ngacrawler;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@MapperScan(value = "com.gin.ngacrawler.dao")
public class NgaCrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NgaCrawlerApplication.class, args);
    }

}
