package com.smartticket.demo.feign;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "ticket-service")
public interface TicketClient {
	@GetMapping("/tickets/status-summary")
	Map<String, Long> getTicketStatusSummary();

	@GetMapping("/tickets/status-priority-summary")
	Map<String, Map<String, Long>> getTicketStatusPrioritySummary();
}
