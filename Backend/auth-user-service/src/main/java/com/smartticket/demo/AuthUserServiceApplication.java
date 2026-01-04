package com.smartticket.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AuthUserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthUserServiceApplication.class, args);
	}

}
