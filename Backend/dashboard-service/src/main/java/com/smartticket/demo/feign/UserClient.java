package com.smartticket.demo.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.smartticket.demo.dto.UserStatsDto;

@FeignClient(name = "auth-user-service")
public interface UserClient {

	@GetMapping("/auth/users/stats")
	UserStatsDto getUserStats();
}