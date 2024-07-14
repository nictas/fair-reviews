package com.nictas.reviews;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FairReviewsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FairReviewsApplication.class, args);
    }

}
