import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { MenuService, Category } from '@shared';
import { CategoryFormComponent } from '../category-form/category-form.component';

@Component({
  selector: 'app-category-list',
  templateUrl: './category-list.component.html',
  styleUrls: ['./category-list.component.scss']
})
export class CategoryListComponent implements OnInit {
  categories: Category[] = [];
  loading = true;

  constructor(
    private menuService: MenuService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading = true;
    this.menuService.getAdminCategories().subscribe({
      next: c => { this.categories = c; this.loading = false; },
      error: () => this.loading = false
    });
  }

  openCreate(): void {
    const ref = this.dialog.open(CategoryFormComponent, { width: '400px', data: {} });
    ref.afterClosed().subscribe(ok => { if (ok) { this.snackBar.open('Category created', 'OK', { duration: 2000 }); this.loadCategories(); } });
  }

  openEdit(cat: Category): void {
    const ref = this.dialog.open(CategoryFormComponent, { width: '400px', data: { category: cat } });
    ref.afterClosed().subscribe(ok => { if (ok) { this.snackBar.open('Category updated', 'OK', { duration: 2000 }); this.loadCategories(); } });
  }

  deleteCategory(cat: Category): void {
    if (!confirm(`Delete category "${cat.name}"?`)) return;
    this.menuService.deleteCategory(cat.id).subscribe({
      next: () => { this.snackBar.open('Category deleted', 'OK', { duration: 2000 }); this.loadCategories(); },
      error: err => this.snackBar.open(err?.error?.message || 'Delete failed', 'OK')
    });
  }

  drop(event: CdkDragDrop<Category[]>): void {
    moveItemInArray(this.categories, event.previousIndex, event.currentIndex);
    const ids = this.categories.map(c => c.id);
    this.menuService.reorderCategories(ids).subscribe({
      next: () => this.snackBar.open('Order saved', 'OK', { duration: 2000 }),
      error: () => this.snackBar.open('Reorder failed', 'OK')
    });
  }
}
