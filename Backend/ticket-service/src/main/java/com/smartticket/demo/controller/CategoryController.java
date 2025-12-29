package com.smartticket.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartticket.demo.entity.Category;
import com.smartticket.demo.service.implementation.CategoryServiceImplementation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/categories")
public class CategoryController {

	private final CategoryServiceImplementation categoryService;

	public CategoryController(CategoryServiceImplementation categoryService) {
		this.categoryService = categoryService;
	}

	@PostMapping("/create")
	public Mono<ResponseEntity<Category>> createCategory(@RequestBody Category category) {
		return categoryService.createCategory(category)
				.map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved))
				.onErrorResume(IllegalArgumentException.class, e -> Mono.just(ResponseEntity.badRequest().build()));
	}

	@GetMapping("/{id}")
	public Mono<ResponseEntity<Category>> getCategoryById(@PathVariable String id) {
		return categoryService.getCategoryById(id).map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping
	public Flux<Category> getAllCategories() {
		return categoryService.getAllCategories();
	}

	@PutMapping("/{id}")
	public Mono<ResponseEntity<Category>> updateCategory(@PathVariable String id, @RequestBody Category category) {
		return categoryService.updateCategory(id, category).map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> deleteCategory(@PathVariable String id,
			@RequestParam(required = false) String reassignTo) {
		return categoryService.deleteCategory(id, reassignTo).map(v -> ResponseEntity.noContent().<Void>build())
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}
}
