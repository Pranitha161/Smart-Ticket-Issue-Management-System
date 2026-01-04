package com.smartticket.demo.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.smartticket.demo.dto.CategorySummaryDto;
import com.smartticket.demo.dto.PrioritySummaryDto;
import com.smartticket.demo.dto.StatusSummaryDto;

@FeignClient(name = "ticket-service")
public interface TicketClient {
	@GetMapping("/tickets/status-summary")
	List<StatusSummaryDto> getTicketStatusSummary();

	@GetMapping("/tickets/status-priority-summary")
	List<PrioritySummaryDto> getTicketStatusPrioritySummary();

	@GetMapping("/tickets/category-summary")
	List<CategorySummaryDto> getCategorySummary();
}
