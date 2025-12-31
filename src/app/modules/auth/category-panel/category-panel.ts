// src/app/modules/auth/category-panel/category-panel.ts
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Category, CategoryDto } from '../../../core/services/category';

@Component({
  selector: 'app-category-panel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './category-panel.html',
  styleUrls: ['./category-panel.css']
})
export class CategoryPanel implements OnInit {
  categories: CategoryDto[] = [];
  formCategory: CategoryDto = { name: '', description: '', linkedSlaId: '', active: true };
  createMode = false;
  editMode = false;

  constructor(private categoryService: Category,private cd:ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadCategories();
    

  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe(res =>{ this.categories = res
      ;this.cd.detectChanges();   console.log(res);
    });
 
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

  saveCategory(): void {
    if (this.createMode) {
      this.categoryService.createCategory(this.formCategory).subscribe(() => {
        this.loadCategories();
        this.cancel();
      });
    } else if (this.editMode) {
      this.categoryService.updateCategory(this.formCategory.id!, this.formCategory).subscribe(() => {
        this.loadCategories();
        this.cancel();
      });
    }
  }

  deleteCategory(id: string): void {
    this.categoryService.deleteCategory(id).subscribe(() => this.loadCategories());
  }

  cancel(): void {
    this.createMode = false;
    this.editMode = false;
    this.formCategory = { name: '', description: '', linkedSlaId: '', active: true };
  }
}
