package com.smartticket.demo.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "auth-user-service")
public interface UserClient {
	@PutMapping("/internal/agents/{agentId}/resolved") 
	void incrementResolvedCount(@PathVariable("agentId") String agentId);
	
	 @PutMapping("/internal/agents/{agentId}/unresolved")
		void incrementEscalatedCount(@PathVariable("agentId") String agentId);
	 
	 @PutMapping("/internal/agents/{agentId}/increment-assignments") 
	    void incrementAssignments(@PathVariable("agentId") String agentId);
	    
	    @PutMapping("/internal/agents/{agentId}/decrement-assignments") 
	    void decrementAssignments(@PathVariable("agentId") String agentId);
	    
}
