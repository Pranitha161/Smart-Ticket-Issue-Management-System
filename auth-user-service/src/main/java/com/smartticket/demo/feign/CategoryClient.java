package com.smartticket.demo.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.smartticket.demo.entity.Category;

@FeignClient(name = "ticket-service")
public interface CategoryClient {

	@GetMapping("/categories/{id}")
	Category getCategoryById(@PathVariable("id") String id);
}
