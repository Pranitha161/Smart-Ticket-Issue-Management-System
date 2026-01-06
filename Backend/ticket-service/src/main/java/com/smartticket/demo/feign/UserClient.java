package com.smartticket.demo.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "auth-user-service")
public interface UserClient {
	@PutMapping("/agents/{agentId}/resolved") 
	void incrementResolvedCount(@PathVariable("agentId") String agentId);
	
	 @PutMapping("/agents/{agentId}/unresolved")
		void incrementEscalatedCount(@PathVariable String agentId);
	 
	 @PutMapping("/agents/{agentId}/increment-assignments") 
	    void incrementAssignments(@PathVariable("agentId") String agentId);
	    
	    @PutMapping("/agents/{agentId}/decrement-assignments") 
	    void decrementAssignments(@PathVariable("agentId") String agentId);
	    
}
