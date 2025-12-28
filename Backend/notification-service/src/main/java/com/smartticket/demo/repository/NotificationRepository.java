package com.smartticket.demo.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.smartticket.demo.entity.Notification;

@Repository
public interface NotificationRepository extends ReactiveMongoRepository<Notification, String>{

}
