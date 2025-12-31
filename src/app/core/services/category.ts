import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";

export interface CategoryDto {
  id?: string;
  name: string;
  description: string;
  linkedSlaId?: string;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
export class Category {
  private baseUrl = 'http://localhost:8765/ticket-service/categories';

  constructor(private http: HttpClient) {}

  getAllCategories(): Observable<CategoryDto[]> {
    return this.http.get<CategoryDto[]>(this.baseUrl);
  }

  createCategory(category: CategoryDto): Observable<CategoryDto> {
    return this.http.post<CategoryDto>(this.baseUrl, category);
  }

  updateCategory(id: string, category: CategoryDto): Observable<CategoryDto> {
    return this.http.put<CategoryDto>(`${this.baseUrl}/${id}`, category);
  }

  deleteCategory(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}

