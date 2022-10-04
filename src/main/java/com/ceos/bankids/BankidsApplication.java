package com.ceos.bankids;

import java.time.LocalDateTime;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BankidsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankidsApplication.class, args);
        System.out.println("LocalDateTime.now() = " + LocalDateTime.now());
    }
}
