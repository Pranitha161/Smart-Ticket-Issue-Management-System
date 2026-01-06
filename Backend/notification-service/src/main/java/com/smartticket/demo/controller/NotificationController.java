package com.smartticket.demo.controller;

import org.springframework.web.bind.annotation.*;

import com.smartticket.demo.entity.Notification;
import com.smartticket.demo.service.NotificationService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    
    @PostMapping("/send")
    public Mono<Notification> sendNotification(@RequestBody Notification notification) {
        return notificationService.sendEmail(notification);
    }

    @GetMapping("/status/{id}")
    public Mono<Notification> getStatus(@PathVariable String id) {
    	return notificationService.notificationsById(id);
    }

    @GetMapping("/history/{userEmail}")
    public Flux<Notification> getHistory(@PathVariable String userEmail) {
        return notificationService.notificationsByEmail(userEmail);
    }
}

