package com.smartticket.demo.service;

import com.smartticket.demo.entity.Category;
import com.smartticket.demo.entity.Ticket;
import com.smartticket.demo.repository.CategoryRepository;
import com.smartticket.demo.repository.TicketRepository;
import com.smartticket.demo.service.implementation.CategoryServiceImplementation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)

public class CatergoryServiceImplmentationTest {
	@Mock
	private CategoryRepository categoryRepo;
	@Mock
	private TicketRepository ticketRepo;
	@InjectMocks
	private CategoryServiceImplementation service;

	@Test
	void createCategory_success() {
		Category category = Category.builder().id("C1").name("IT").description("IT issues").build();
		when(categoryRepo.findByName("IT")).thenReturn(Mono.empty());
		when(categoryRepo.save(category)).thenReturn(Mono.just(category));
		StepVerifier.create(service.createCategory(category)).assertNext(saved -> assertEquals("IT", saved.getName()))
				.verifyComplete();
	}

	@Test
	void createCategory_alreadyExists() {
		Category existing = Category.builder().id("C1").name("IT").build();
		when(categoryRepo.findByName("IT")).thenReturn(Mono.just(existing));
		when(categoryRepo.save(existing)).thenReturn(Mono.just(existing));

		StepVerifier.create(service.createCategory(existing)).expectErrorMatches(
				ex -> ex instanceof IllegalArgumentException && ex.getMessage().equals("Category already exists"))
				.verify();
	}

	@Test
	void getCategoryById_success() {
		Category category = Category.builder().id("C1").name("IT").build();
		when(categoryRepo.findById("C1")).thenReturn(Mono.just(category));
		StepVerifier.create(service.getCategoryById("C1")).assertNext(cat -> assertEquals("IT", cat.getName()))
				.verifyComplete();
	}

	@Test
	void getCategoryById_notFound() {
		when(categoryRepo.findById("C1")).thenReturn(Mono.empty());
		StepVerifier.create(service.getCategoryById("C1")).verifyComplete();
	}

	@Test
	void getAllCategories_success() {
		Category c1 = Category.builder().id("C1").name("IT").build();
		Category c2 = Category.builder().id("C2").name("HR").build();
		when(categoryRepo.findAll()).thenReturn(Flux.just(c1, c2));
		StepVerifier.create(service.getAllCategories()).expectNext(c1).expectNext(c2).verifyComplete();
	}

	@Test
	void getAllCategories_empty() {
		when(categoryRepo.findAll()).thenReturn(Flux.empty());
		StepVerifier.create(service.getAllCategories()).verifyComplete();
	}

	@Test
	void updateCategory_success() {
		Category existing = Category.builder().id("C1").name("Old").description("Old desc").linkedSlaId("S1").build();
		Category updated = Category.builder().id("C1").name("New").description("New desc").linkedSlaId("S2").build();
		when(categoryRepo.findById("C1")).thenReturn(Mono.just(existing));
		when(categoryRepo.save(any(Category.class))).thenReturn(Mono.just(updated));
		StepVerifier.create(service.updateCategory("C1", updated)).assertNext(cat -> {
			assertEquals("New", cat.getName());
			assertEquals("New desc", cat.getDescription());
			assertEquals("S2", cat.getLinkedSlaId());
		}).verifyComplete();
	}

	@Test
	void updateCategory_notFound() {
		Category updated = Category.builder().id("C1").name("New").build();
		when(categoryRepo.findById("C1")).thenReturn(Mono.empty());
		StepVerifier.create(service.updateCategory("C1", updated)).verifyComplete();
	}

	@Test
	void deleteCategory_successWithReassign() {
	    Ticket ticket = Ticket.builder().id("T1").categoryId("C1").build();
	    when(ticketRepo.findByCategoryId("C1")).thenReturn(Flux.just(ticket));
	    when(ticketRepo.save(any(Ticket.class))).thenReturn(Mono.just(ticket));
	    when(categoryRepo.deleteById("C1")).thenReturn(Mono.empty());

	    StepVerifier.create(service.deleteCategory("C1", "C2")).verifyComplete();
	}


	@Test
	void deleteCategory_ticketsExistWithoutReassign() {
	    Ticket ticket = Ticket.builder().id("T1").categoryId("C1").build();
	    when(ticketRepo.findByCategoryId("C1")).thenReturn(Flux.just(ticket));
	    when(categoryRepo.deleteById("C1")).thenReturn(Mono.empty());

	    StepVerifier.create(service.deleteCategory("C1", null))
	        .expectErrorMatches(ex -> ex instanceof IllegalArgumentException
	            && ex.getMessage().equals("Tickets exist, reassign required"))
	        .verify();
	}


	@Test
	void deleteCategory_noTickets() {
	    when(ticketRepo.findByCategoryId("C1")).thenReturn(Flux.empty());
	    when(categoryRepo.deleteById("C1")).thenReturn(Mono.empty());

	    StepVerifier.create(service.deleteCategory("C1", null)).verifyComplete();
	}


}
