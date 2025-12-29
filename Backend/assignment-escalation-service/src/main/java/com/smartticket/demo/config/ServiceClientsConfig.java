package com.smartticket.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ServiceClientsConfig {

    @Value("${ticket.service.url}")
    private String ticketBaseUrl;

    @Value("${user.service.url}")
    private String userBaseUrl;

    @Bean("ticketWebClient")
    public WebClient ticketWebClient(WebClient.Builder builder) {
        return builder.baseUrl(ticketBaseUrl).build();
    }

    @Bean("userWebClient")
    public WebClient userWebClient(WebClient.Builder builder) {
        return builder.baseUrl(userBaseUrl).build();
    }
}
