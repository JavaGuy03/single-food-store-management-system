package com.fm.foodmanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FoodManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodManagementSystemApplication.class, args);
    }

}
