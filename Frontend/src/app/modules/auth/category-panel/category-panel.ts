import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Category, CategoryDto } from '../../../core/services/category';
import { Toast } from '../../../core/services/toast';
import { LookupService } from '../../../core/services/lookup-service';

@Component({
  selector: 'app-category-panel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './category-panel.html',
  styleUrls: ['./category-panel.css']
})
export class CategoryPanel implements OnInit {
  private categoryService = inject(Category);
  private toast = inject(Toast);
  private lookup = inject(LookupService);
  private cd = inject(ChangeDetectorRef);

  categories: CategoryDto[] = [];
  formCategory: CategoryDto = { name: '', description: '', linkedSlaId: '', active: true };
  categoryToDelete: CategoryDto | null = null;
  createMode = false;
  editMode = false;

  get slasList() {
    return this.lookup.getSlaList();
  }

  ngOnInit(): void {
    this.loadCategories();
    if (this.slasList.length === 0) {
      this.lookup.refreshSlas();
    }
  }

  getSlaPriorityName(slaId: string | undefined): string {
    if (!slaId) return 'No SLA';
    const foundSla = this.slasList.find(s => s.id === slaId);
    return foundSla ? foundSla.priority : 'No SLA';
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe({
      next: (res) => {
        this.categories = res;
        this.cd.detectChanges();
      },
      error: (err) => this.toast.show(err.error?.message || err.message, 'error')
    });
  }

  saveCategory(): void {
    const request = this.createMode
      ? this.categoryService.createCategory(this.formCategory)
      : this.categoryService.updateCategory(this.formCategory.id!, this.formCategory);
    request.subscribe({
      next: () => {
        this.toast.show('Changes saved successfully!', 'success');
        this.lookup.refreshCategories();
        this.loadCategories();
        this.cancel();
      },
      error: (err) => {
        this.toast.show(err.error?.message || err.message, 'error');
      }
    });
  }

  confirmDelete(category: CategoryDto): void {
    this.categoryToDelete = category;
  }

  executeDelete(): void {
    if (this.categoryToDelete?.id) {
      this.categoryService.deleteCategory(this.categoryToDelete.id).subscribe({
        next: () => {
          this.toast.show('Category deleted', 'success');
          this.lookup.refreshCategories();
          this.categoryToDelete = null;
          this.loadCategories();
        },
        error: (err) => {
          this.toast.show(err.error?.message || err.message, 'error');
          this.categoryToDelete = null;
        }
      });
    }
  }

  openCreateForm(): void {
    this.createMode = true;
    this.editMode = false;
    this.formCategory = { name: '', description: '', linkedSlaId: '', active: true };
  }

  editCategory(category: CategoryDto): void {
    this.editMode = true;
    this.createMode = false;
    this.formCategory = { ...category };
  }

  cancel(): void {
    this.createMode = false;
    this.editMode = false;
    this.categoryToDelete = null;
    this.formCategory = { name: '', description: '', linkedSlaId: '', active: true };
  }
}