package com.smartticket.demo.dto;

import com.smartticket.demo.entity.AgentProfile;

import lombok.Data;

@Data
public class AgentDto {
    private String id;
    private String username;
    private String email;
    private AgentProfile agentProfile; 
}