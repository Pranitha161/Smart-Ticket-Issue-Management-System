package com.smartticket.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.smartticket.demo.dto.AgentDto;
import com.smartticket.demo.dto.TicketDto;
import com.smartticket.demo.feign.UserClient;

class RuleEngineTest {

	private UserClient userClient;
	private RuleEngine ruleEngine;

	@BeforeEach
	void setup() {
		userClient = mock(UserClient.class);
		ruleEngine = new RuleEngine(userClient);
	}

	@Test
	void pickAgentForTicket_returnsAgentWithFewestAssignments() {
		TicketDto ticket = new TicketDto();
		ticket.setCategoryId("C1");

		AgentDto agent1 = new AgentDto();
		agent1.setId("A1");
		agent1.setCurrentAssignments(5);

		AgentDto agent2 = new AgentDto();
		agent2.setId("A2");
		agent2.setCurrentAssignments(2);

		List<AgentDto> agents = Arrays.asList(agent1, agent2);
		when(userClient.getAgentsByCategory("C1")).thenReturn(agents);

		String chosen = ruleEngine.pickAgentForTicket(ticket);
		assertEquals("A2", chosen);
	}

	@Test
	void pickAgentForTicket_noAgentsThrowsException() {
		TicketDto ticket = new TicketDto();
		ticket.setCategoryId("C3");

		when(userClient.getAgentsByCategory("C3")).thenReturn(Collections.emptyList());

		RuntimeException ex = assertThrows(RuntimeException.class, () -> ruleEngine.pickAgentForTicket(ticket));
		assertTrue(ex.getMessage().contains("No agents available for category C3"));
	}

}
