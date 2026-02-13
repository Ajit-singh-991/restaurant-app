import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Category, MenuItem, CreateMenuItemRequest, BulkUploadResult } from '../models/menu.model';
import { environment } from '@environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MenuService {
  private apiUrl = `${environment.apiUrl}/menu`;

  constructor(private http: HttpClient) {}

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/categories`);
  }

  getAdminCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/admin/categories`);
  }

  updateCategory(id: number, category: Partial<Category>): Observable<Category> {
    return this.http.put<Category>(`${this.apiUrl}/categories/${id}`, category);
  }

  reorderCategories(ids: number[]): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/categories/reorder`, ids);
  }

  createCategory(category: Partial<Category>): Observable<Category> {
    return this.http.post<Category>(`${this.apiUrl}/categories`, category);
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/categories/${id}`);
  }

  getMenuItems(): Observable<MenuItem[]> {
    return this.http.get<MenuItem[]>(`${this.apiUrl}/items`);
  }

  getAdminMenuItems(): Observable<MenuItem[]> {
    return this.http.get<MenuItem[]>(`${this.apiUrl}/admin/items`);
  }

  getItemsByCategory(categoryId: number): Observable<MenuItem[]> {
    return this.http.get<MenuItem[]>(`${this.apiUrl}/items/category/${categoryId}`);
  }

  getItemById(id: number): Observable<MenuItem> {
    return this.http.get<MenuItem>(`${this.apiUrl}/items/${id}`);
  }

  searchItems(query: string): Observable<MenuItem[]> {
    return this.http.get<MenuItem[]>(`${this.apiUrl}/items/search`, { params: { q: query } });
  }

  createItem(request: CreateMenuItemRequest): Observable<MenuItem> {
    return this.http.post<MenuItem>(`${this.apiUrl}/items`, request);
  }

  updateItem(id: number, request: CreateMenuItemRequest): Observable<MenuItem> {
    return this.http.put<MenuItem>(`${this.apiUrl}/items/${id}`, request);
  }

  deleteItem(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/items/${id}`);
  }

  toggleAvailability(id: number): Observable<MenuItem> {
    return this.http.patch<MenuItem>(`${this.apiUrl}/items/${id}/availability`, {});
  }

  bulkUploadItems(file: File): Observable<BulkUploadResult> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<BulkUploadResult>(`${this.apiUrl}/admin/items/bulk`, formData);
  }
}
