package com.example.kidsapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KidsAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(KidsAppApplication.class, args);
    }
}