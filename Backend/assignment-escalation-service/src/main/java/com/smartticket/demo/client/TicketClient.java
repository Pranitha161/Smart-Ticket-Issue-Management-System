package com.smartticket.demo.client;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.smartticket.demo.dto.TicketDto;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TicketClient {

    private final WebClient.Builder webClientBuilder;

    public Mono<TicketDto> getTicketById(String ticketId) {
        return webClientBuilder.build()
                .get()
                .uri("http://ticket-service/tickets/{id}", ticketId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                          resp -> Mono.error(new RuntimeException("Ticket not found")))
                .onStatus(HttpStatusCode::is5xxServerError,
                          resp -> Mono.error(new RuntimeException("Ticket service error")))
                .bodyToMono(TicketDto.class);
    }
}
