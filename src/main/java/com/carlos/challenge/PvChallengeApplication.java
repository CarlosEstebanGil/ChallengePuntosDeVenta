package com.carlos.challenge;

import com.carlos.challenge.infrastructure.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class PvChallengeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PvChallengeApplication.class, args);
	}

}

