package com.smartticket.demo.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-user-service")
public interface UserClient {
    @GetMapping("/auth/{id}")
    String getUserEmail(@PathVariable("id") String userId);
}
