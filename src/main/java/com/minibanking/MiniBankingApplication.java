package com.minibanking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class MiniBankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniBankingApplication.class, args);
    }
}



