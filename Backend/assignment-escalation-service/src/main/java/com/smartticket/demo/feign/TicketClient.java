package com.smartticket.demo.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smartticket.demo.dto.TicketDto;
import com.smartticket.demo.entity.TicketActivity;

@FeignClient(name = "ticket-service")
public interface TicketClient {

	@GetMapping("/tickets/internal/{id}")
	TicketDto getTicketById(@PathVariable String id);

	@PutMapping("/tickets/internal/{id}/assign")
	void assignTicket(@PathVariable String id,@RequestParam("agentId") String agentId);
	
	@PutMapping("/tickets/internal/{id}/escalate")
	void escalateTicket(@PathVariable String id);
	
	
	@PutMapping("/tickets/internal/{id}/resolve")
	void resolveTicket(@PathVariable String id);
	
	@PostMapping("/tickets/{ticketId}/activity/internal/comment")
	TicketActivity logActivity(@PathVariable String ticketId, @RequestParam String actorId,
			@RequestParam String comment);

}
