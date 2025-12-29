package com.smartticket.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EscalationSummaryDto {
    private int level;
    private long count;
}

