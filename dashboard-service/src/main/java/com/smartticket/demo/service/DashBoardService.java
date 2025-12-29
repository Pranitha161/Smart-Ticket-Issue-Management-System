package com.smartticket.demo.service;

import com.smartticket.demo.dto.AgentSummaryDto;
import com.smartticket.demo.dto.EscalationSummaryDto;
import com.smartticket.demo.dto.PrioritySummaryDto;
import com.smartticket.demo.dto.StatusSummaryDto;

import reactor.core.publisher.Flux;

public interface DashBoardService {

	Flux<StatusSummaryDto> getTicketStatusSummary();

	Flux<PrioritySummaryDto> getTicketStatusPrioritySummary();

	Flux<AgentSummaryDto> getAssignmentsPerAgent();

	Flux<EscalationSummaryDto> getEscalationSummary();

}
