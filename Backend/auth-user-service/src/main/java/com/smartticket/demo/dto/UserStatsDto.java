package com.smartticket.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatsDto {
    private long totalUsers;
    private long activeUsers;
    private long supportAgents;
    private long endUsers;
    private long managers; 
    private long admins;
}
