package com.smartticket.demo.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.smartticket.demo.dto.CategorySummaryDto;
import com.smartticket.demo.dto.PrioritySummaryDto;
import com.smartticket.demo.dto.StatusSummaryDto;
import com.smartticket.demo.dto.UserTicketStatsDto;

@FeignClient(name = "ticket-service")
public interface TicketClient {
	@GetMapping("/tickets/status-summary")
	List<StatusSummaryDto> getTicketStatusSummary();

	@GetMapping("/tickets/status-priority-summary")
	List<PrioritySummaryDto> getTicketStatusPrioritySummary();

	@GetMapping("/tickets/category-summary")
	List<CategorySummaryDto> getCategorySummary();
	
	@GetMapping("/tickets/user/{userId}/stats")
	UserTicketStatsDto getUserStats(@PathVariable String userId);
	
	@GetMapping("/tickets/agent/{agentId}/stats")
	UserTicketStatsDto getAgentStats(@PathVariable String agentId);
	
	@GetMapping("/tickets/global-stats")
	UserTicketStatsDto getGlobalStats();
}
