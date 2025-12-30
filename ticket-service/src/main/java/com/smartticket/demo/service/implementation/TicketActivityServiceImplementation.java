package com.smartticket.demo.service.implementation;

import org.springframework.stereotype.Service;

import com.smartticket.demo.dto.SlaReportDto;
import com.smartticket.demo.entity.TicketActivity;
import com.smartticket.demo.enums.ACTION_TYPE;
import com.smartticket.demo.repository.TicketActivityRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Service
public class TicketActivityServiceImplementation {

	private final TicketActivityRepository ticketActivityRepo;

	TicketActivityServiceImplementation(TicketActivityRepository ticketActivityRepo) {
		this.ticketActivityRepo = ticketActivityRepo;
	}

	public Mono<TicketActivity> log(String ticketId, String actorId, ACTION_TYPE type, String details,
			Instant timestamp) {
		TicketActivity activity = TicketActivity.builder().id(ticketId).actorId(actorId).actionType(type)
				.details(details).timestamp(timestamp != null ? timestamp : Instant.now()).build();

		return ticketActivityRepo.save(activity);
	}

	public Flux<TicketActivity> getTimeline(String ticketId) {
		return ticketActivityRepo.findByTicketIdOrderByTimestampAsc(ticketId);
	}
	
	public Flux<SlaReportDto> getSlaCompliance() {
	    return ticketActivityRepo.findAll()
	        .filter(activity -> activity.getActionType() == ACTION_TYPE.RESOLVED)
	        .flatMap(closed -> ticketActivityRepo.findByTicketId(closed.getTicketId())
	            .filter(act -> act.getActionType() == ACTION_TYPE.ASSIGNMENT)
	            .next()
	            .map(created -> {
	                long resolutionMinutes = Duration.between(created.getTimestamp(), closed.getTimestamp()).toMinutes();
	                return SlaReportDto.builder().ticketId(closed.getTicketId()).resolutionMinutes(resolutionMinutes).build();
	                	
	            })
	        );
	}

}
