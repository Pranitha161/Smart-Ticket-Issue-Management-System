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
	@GetMapping("/tickets/dashboard/status-summary")
	List<StatusSummaryDto> getTicketStatusSummary();

	@GetMapping("/tickets/dashboard/status-priority-summary")
	List<PrioritySummaryDto> getTicketStatusPrioritySummary();

	@GetMapping("/tickets/dashboard/category-summary")
	List<CategorySummaryDto> getCategorySummary();
	
	@GetMapping("/tickets/dashboard/user/{userId}/stats")
	UserTicketStatsDto getUserStats(@PathVariable String userId);
	
	@GetMapping("/tickets/dashboard/agent/{agentId}/stats")
	UserTicketStatsDto getAgentStats(@PathVariable String agentId);
	
	@GetMapping("/tickets/dashboard/global-stats")
	UserTicketStatsDto getGlobalStats();
}
