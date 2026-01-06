package com.smartticket.demo.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smartticket.demo.dto.AgentDto;

@FeignClient(name = "auth-user-service")
public interface UserClient {

    @GetMapping("/agents")
    List<AgentDto> getAgentsByCategory(@RequestParam("category") String category);
    
    @PutMapping("/agents/{agentId}/increment-assignments") 
    void incrementAssignments(@PathVariable("agentId") String agentId);
    
    @PutMapping("/agents/{agentId}/decrement-assignments") 
    void decrementAssignments(@PathVariable("agentId") String agentId);
    
}
