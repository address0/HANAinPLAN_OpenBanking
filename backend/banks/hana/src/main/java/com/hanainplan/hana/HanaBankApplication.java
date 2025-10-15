package com.hanainplan.hana;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HanaBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(HanaBankApplication.class, args);
    }

}