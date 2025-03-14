package com.experimental.anonchat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync // Enable asynchronous method execution
public class AnonchatBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnonchatBackendApplication.class, args);
	}

}
