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

import com.smartticket.demo.entity.ApiResponse;
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
	public Mono<ResponseEntity<ApiResponse>> createCategory(@RequestBody Category category) {
		return categoryService.createCategory(category).map(saved -> ResponseEntity.status(HttpStatus.CREATED)
				.body(new ApiResponse(true, "Created category successfully " + saved.getId())));

	}

	@GetMapping("/{id}")
	public Mono<ResponseEntity<Category>> getCategoryById(@PathVariable String id) {
		return categoryService.getCategoryById(id).map(category -> ResponseEntity.ok(category))
				.defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	@GetMapping
	public Flux<Category> getAllCategories() {
		return categoryService.getAllCategories();
	}
	
	@PutMapping("/{id}")
	public Mono<ResponseEntity<ApiResponse>> updateCategory(@PathVariable String id, @RequestBody Category category) {
		return categoryService.updateCategory(id, category).map(
				updated -> ResponseEntity.ok(new ApiResponse(true, "Category updated successfully" + updated.getId())))
				.defaultIfEmpty(
						ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Category not found")));
	}

	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<ApiResponse>> deleteCategory(@PathVariable String id,
			@RequestParam(required = false) String reassignTo) {
		return categoryService.deleteCategory(id, reassignTo)
				.then(Mono.just(ResponseEntity.ok(new ApiResponse(true, "Category deleted successfully"))));

	}

}
