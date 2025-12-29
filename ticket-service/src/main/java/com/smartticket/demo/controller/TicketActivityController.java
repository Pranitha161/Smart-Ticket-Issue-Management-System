package com.smartticket.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.smartticket.demo.entity.TicketActivity;
import com.smartticket.demo.enums.ACTION_TYPE;
import com.smartticket.demo.service.implementation.TicketActivityServiceImplementation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/tickets/{ticketId}/activity")
public class TicketActivityController {

 private final TicketActivityServiceImplementation service;

 public TicketActivityController(TicketActivityServiceImplementation service) {
     this.service = service;
 }


 @GetMapping(produces = MediaType.APPLICATION_NDJSON_VALUE)
 public Flux<TicketActivity> getTimeline(@PathVariable String ticketId) {
     return service.getTimeline(ticketId);
 }

 @PostMapping("/comment")
 public Mono<TicketActivity> addComment(@PathVariable String ticketId,
                                        @RequestParam String actorId,
                                        @RequestParam String comment) {
     return service.log(ticketId, actorId, ACTION_TYPE.COMMENT, comment, null);
 }
}

