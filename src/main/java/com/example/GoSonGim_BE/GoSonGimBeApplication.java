package com.example.GoSonGim_BE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class GoSonGimBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoSonGimBeApplication.class, args);
	}

}
