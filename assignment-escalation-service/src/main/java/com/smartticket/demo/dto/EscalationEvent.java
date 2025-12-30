package com.smartticket.demo.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EscalationEvent {
    private String ticketId;          
    private String action;            
    private String agentId;           
    private int escalationLevel;      
    private Instant timestamp; 
}

