package com.smartticket.demo.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.smartticket.demo.dto.TicketDto;

@FeignClient(name = "ticket-service")
public interface TicketClient {

    @GetMapping("/tickets/{id}")
    TicketDto getTicketById(@PathVariable String id);
}
