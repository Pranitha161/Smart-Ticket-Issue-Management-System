package com.smartticket.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTicketStatsDto {
    private long total;
    private long open;
    private long resolved;
    private long critical;
}
