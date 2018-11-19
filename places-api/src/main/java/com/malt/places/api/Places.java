package com.malt.places.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class Places {
    public static void main(String[] args) {
        SpringApplication.run(Places.class, args);
    }
}
