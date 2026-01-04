package com.smartticket.demo.entity;

import lombok.Data;

@Data
public class Category {
    private String id;
    private String name;
    private String description;
    private String linkedSlaId;
    private boolean active = true; 
}

