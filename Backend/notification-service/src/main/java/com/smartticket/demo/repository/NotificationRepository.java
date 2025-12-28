package com.smartticket.demo.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.smartticket.demo.entity.Notification;

public interface NotificationRepository extends ReactiveMongoRepository<Notification, String>{

}
