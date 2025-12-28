package com.smartticket.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class SmartticketConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartticketConfigServerApplication.class, args);
	}

}
