package com.smartticket.demo.service.implementation;

import org.springframework.stereotype.Service;

import com.smartticket.demo.entity.Category;
import com.smartticket.demo.repository.CategoryRepository;
import com.smartticket.demo.repository.TicketRepository;
import com.smartticket.demo.service.CategoryService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CategoryServiceImplementation implements CategoryService {

	private final CategoryRepository categoryRepo;
	private final TicketRepository ticketRepo;

	public CategoryServiceImplementation(CategoryRepository categoryRepo, TicketRepository ticketRepo) {
		this.categoryRepo = categoryRepo;
		this.ticketRepo = ticketRepo;
	}

	@Override
	public Mono<Category> createCategory(Category category) {
		return categoryRepo.findByName(category.getName())
				.flatMap(existing -> Mono.<Category>error(new IllegalArgumentException("Category already exists")))
				.switchIfEmpty(categoryRepo.save(category));
	}

	@Override
	public Mono<Category> getCategoryById(String id) {
		return categoryRepo.findById(id);
	}

	@Override
	public Flux<Category> getAllCategories() {
		return categoryRepo.findAll();
	}

	@Override
	public Mono<Category> updateCategory(String id, Category updatedCategory) {
		return categoryRepo.findById(id).flatMap(existing -> {
			existing.setName(updatedCategory.getName());
			existing.setDescription(updatedCategory.getDescription());
			existing.setLinkedSlaId(updatedCategory.getLinkedSlaId());
			return categoryRepo.save(existing);
		});
	}

	@Override
	public Mono<Void> deleteCategory(String id, String reassignTo) {
		return ticketRepo.findByCategoryId(id).flatMap(ticket -> {
			if (reassignTo != null) {
				ticket.setCategoryId(reassignTo);
				return ticketRepo.save(ticket);
			} else {
				return Mono.error(new IllegalArgumentException("Tickets exist, reassign required"));
			}
		}).then(categoryRepo.deleteById(id));
	}
}
