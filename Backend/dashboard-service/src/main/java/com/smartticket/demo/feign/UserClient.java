package com.smartticket.demo.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.smartticket.demo.dto.AgentStatsDto;
import com.smartticket.demo.dto.UserStatsDto;

@FeignClient(name = "auth-user-service")
public interface UserClient {

	@GetMapping("/dashboard/auth/users/stats")
	UserStatsDto getUserStats();
	
	@GetMapping("/dashboard/{agentId}/stats") 
	AgentStatsDto getAgentStats(@PathVariable String agentId); 
	
	@GetMapping("/dashboard/stats")
	List<AgentStatsDto> getAllAgentStats();
}