package com.smartticket.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartticket.demo.entity.Category;
import com.smartticket.demo.service.implementation.CategoryServiceImplementation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {
	@Mock
	private CategoryServiceImplementation categoryService;
	@InjectMocks
	private CategoryController controller;

	@Test
	void createCategory_success() {
		Category category = Category.builder().id("C1").name("IT").build();
		when(categoryService.createCategory(category)).thenReturn(Mono.just(category));
		StepVerifier.create(controller.createCategory(category)).assertNext(response -> {
			assertEquals(201, response.getStatusCodeValue());
			assertTrue(response.getBody().isSuccess());
			assertTrue(response.getBody().getMessage().contains("Created category successfully C1"));
		}).verifyComplete();
	}

	@Test
	void getCategoryById_success() {
		Category category = Category.builder().id("C1").name("IT").build();
		when(categoryService.getCategoryById("C1")).thenReturn(Mono.just(category));
		StepVerifier.create(controller.getCategoryById("C1")).assertNext(response -> {
			assertEquals(200, response.getStatusCodeValue());
			assertEquals("IT", response.getBody().getName());
		}).verifyComplete();
	}

	@Test
	void getCategoryById_notFound() {
		when(categoryService.getCategoryById("C1")).thenReturn(Mono.empty());
		StepVerifier.create(controller.getCategoryById("C1"))
				.assertNext(response -> assertEquals(404, response.getStatusCodeValue())).verifyComplete();
	}

	@Test
	void updateCategory_notFound() {
		Category updated = Category.builder().id("C1").name("New").build();
		when(categoryService.updateCategory("C1", updated)).thenReturn(Mono.empty());
		StepVerifier.create(controller.updateCategory("C1", updated)).assertNext(response -> {
			assertEquals(404, response.getStatusCodeValue());
			assertFalse(response.getBody().isSuccess());
			assertEquals("Category not found", response.getBody().getMessage());
		}).verifyComplete();
	}

	@Test
	void getAllCategories_success() {
		Category c1 = Category.builder().id("C1").name("IT").build();
		Category c2 = Category.builder().id("C2").name("HR").build();
		when(categoryService.getAllCategories()).thenReturn(Flux.just(c1, c2));
		StepVerifier.create(controller.getAllCategories()).expectNext(c1).expectNext(c2).verifyComplete();
	}

	@Test
	void updateCategory_success() {
		Category updated = Category.builder().id("C1").name("New").build();
		when(categoryService.updateCategory("C1", updated)).thenReturn(Mono.just(updated));
		StepVerifier.create(controller.updateCategory("C1", updated)).assertNext(response -> {
			assertEquals(200, response.getStatusCodeValue());
			assertTrue(response.getBody().isSuccess());
			assertTrue(response.getBody().getMessage().contains("Category updated successfullyC1"));
		}).verifyComplete();
	}

	@Test
	void deleteCategory_success() {
		when(categoryService.deleteCategory("C1", null)).thenReturn(Mono.empty());
		StepVerifier.create(controller.deleteCategory("C1", null)).assertNext(response -> {
			assertEquals(200, response.getStatusCodeValue());
			assertTrue(response.getBody().isSuccess());
			assertEquals("Category deleted successfully", response.getBody().getMessage());
		}).verifyComplete();
	}
}
