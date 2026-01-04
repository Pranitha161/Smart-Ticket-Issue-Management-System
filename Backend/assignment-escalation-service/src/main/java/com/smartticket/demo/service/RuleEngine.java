package com.smartticket.demo.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.smartticket.demo.dto.AgentDto;
import com.smartticket.demo.dto.TicketDto;
import com.smartticket.demo.feign.UserClient;

@Service
public class RuleEngine {
	private final UserClient userClient;

	public RuleEngine(UserClient userClient) {
		this.userClient = userClient;
	}

	public String pickAgentForTicket(TicketDto ticket) {
		List<AgentDto> candidateAgents = userClient.getAgentsByCategory(ticket.getCategoryId());
		if (candidateAgents.isEmpty()) {
			throw new RuntimeException("No agents available for category " + ticket.getCategoryId());
		}
		return candidateAgents.stream()
				.min(Comparator.comparing(agent -> agent.getCurrentAssignments())).map(AgentDto::getId)
				.orElseThrow(() -> new RuntimeException("No agents available"));
	}
}
