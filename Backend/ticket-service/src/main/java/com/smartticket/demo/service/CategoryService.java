package com.smartticket.demo.service;

import com.smartticket.demo.entity.Category;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryService {

	Mono<Category> createCategory(Category category);

	Mono<Category> getCategoryById(String id);

	Flux<Category> getAllCategories();

	Mono<Category> updateCategory(String id, Category category);

	Mono<Void> deleteCategory(String id, String reassignTo);
}
