package com.desertakal.desertakal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DesertakalApplication {

	public static void main(String[] args) {
		SpringApplication.run(DesertakalApplication.class, args);
	}

}
