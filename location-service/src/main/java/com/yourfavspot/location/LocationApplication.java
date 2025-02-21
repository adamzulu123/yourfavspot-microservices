package com.yourfavspot.location;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class LocationApplication {
    public static void main(String[] args) {
        SpringApplication.run(LocationApplication.class, args);
    }
}
