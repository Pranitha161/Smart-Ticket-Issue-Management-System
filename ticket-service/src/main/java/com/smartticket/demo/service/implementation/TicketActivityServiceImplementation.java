package com.smartticket.demo.service.implementation;

import org.springframework.stereotype.Service;

import com.smartticket.demo.entity.TicketActivity;
import com.smartticket.demo.enums.ACTION_TYPE;
import com.smartticket.demo.repository.TicketActivityRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
public class TicketActivityServiceImplementation {

 private final TicketActivityRepository ticketActivityRepo;

 TicketActivityServiceImplementation(TicketActivityRepository ticketActivityRepo) {
     this.ticketActivityRepo = ticketActivityRepo;
 }

 public Mono<TicketActivity> log(String ticketId, String actorId, ACTION_TYPE type, String details, Instant timestamp) {
     TicketActivity activity =new TicketActivity();
     activity.setTicketId(ticketId);
     activity.setActorId(actorId);
     activity.setActionType(type);
     activity.setDetails(details);
     activity.setTimestamp(timestamp != null ? timestamp : Instant.now());
     return ticketActivityRepo.save(activity);
 }

 public Flux<TicketActivity> getTimeline(String ticketId) {
     return ticketActivityRepo.findByTicketIdOrderByTimestampAsc(ticketId);
 }
}

