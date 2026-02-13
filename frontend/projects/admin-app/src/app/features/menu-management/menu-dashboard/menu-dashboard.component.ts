import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource } from '@angular/material/table';
import { MatSort } from '@angular/material/sort';
import { MatPaginator } from '@angular/material/paginator';
import { MenuService, MenuItem, Category } from '@shared';
import { MenuItemFormComponent } from '../menu-item-form/menu-item-form.component';
import { environment } from '@environments/environment';

@Component({
  selector: 'app-menu-dashboard',
  template: `
    <div class="menu-header">
      <h2>Menu Management</h2>
      <div class="header-actions">
        <button mat-button routerLink="/menu/categories">Categories</button>
        <input type="file" #csvInput accept=".csv" style="display:none" (change)="onBulkFile($event)">
        <button mat-stroked-button (click)="csvInput.click()" [disabled]="bulkUploading">
          <mat-icon>upload_file</mat-icon> Bulk Upload CSV
        </button>
        <button mat-raised-button color="primary" (click)="openCreateDialog()">
          <mat-icon>add</mat-icon> Add Item
        </button>
      </div>
    </div>

    <div class="stats-row">
      <mat-card class="stat-card">
        <div class="stat-value">{{ categories.length }}</div>
        <div class="stat-label">Categories</div>
      </mat-card>
      <mat-card class="stat-card">
        <div class="stat-value">{{ items.length }}</div>
        <div class="stat-label">Total Items</div>
      </mat-card>
      <mat-card class="stat-card">
        <div class="stat-value">{{ availableCount }}</div>
        <div class="stat-label">Available</div>
      </mat-card>
      <mat-card class="stat-card">
        <div class="stat-value">{{ items.length - availableCount }}</div>
        <div class="stat-label">Unavailable</div>
      </mat-card>
    </div>

    <div class="toolbar">
      <mat-form-field appearance="outline" class="filter-field">
        <mat-label>Category</mat-label>
        <mat-select [(value)]="categoryFilter" (selectionChange)="applyFilter()">
          <mat-option value="">All</mat-option>
          <mat-option *ngFor="let c of categories" [value]="c.id">{{ c.name }}</mat-option>
        </mat-select>
      </mat-form-field>
    </div>

    <h3>Menu Items</h3>
    <table mat-table [dataSource]="dataSource" matSort class="full-width">
      <ng-container matColumnDef="image">
        <th mat-header-cell *matHeaderCellDef>Image</th>
        <td mat-cell *matCellDef="let item">
          <img *ngIf="getImageUrl(item)" [src]="getImageUrl(item)" class="item-thumb" alt="">
          <span *ngIf="!getImageUrl(item)" class="no-img">—</span>
        </td>
      </ng-container>
      <ng-container matColumnDef="name">
        <th mat-header-cell *matHeaderCellDef mat-sort-header>Name</th>
        <td mat-cell *matCellDef="let item">{{ item.name }}</td>
      </ng-container>
      <ng-container matColumnDef="category">
        <th mat-header-cell *matHeaderCellDef mat-sort-header>Category</th>
        <td mat-cell *matCellDef="let item">{{ item.category?.name || item.categoryName }}</td>
      </ng-container>
      <ng-container matColumnDef="price">
        <th mat-header-cell *matHeaderCellDef mat-sort-header>Price</th>
        <td mat-cell *matCellDef="let item">{{ item.price | currency:'INR' }}</td>
      </ng-container>
      <ng-container matColumnDef="available">
        <th mat-header-cell *matHeaderCellDef>Status</th>
        <td mat-cell *matCellDef="let item">
          <mat-slide-toggle [checked]="item.available"
                            (change)="toggleAvailability(item)">
          </mat-slide-toggle>
        </td>
      </ng-container>
      <ng-container matColumnDef="actions">
        <th mat-header-cell *matHeaderCellDef>Actions</th>
        <td mat-cell *matCellDef="let item">
          <button mat-icon-button routerLink="/menu/items/{{ item.id }}" matTooltip="View">
            <mat-icon>visibility</mat-icon>
          </button>
          <button mat-icon-button color="primary" (click)="openEditDialog(item)" matTooltip="Edit">
            <mat-icon>edit</mat-icon>
          </button>
          <button mat-icon-button (click)="duplicateItem(item)" matTooltip="Duplicate">
            <mat-icon>content_copy</mat-icon>
          </button>
          <button mat-icon-button color="warn" (click)="deleteItem(item)" matTooltip="Delete">
            <mat-icon>delete</mat-icon>
          </button>
        </td>
      </ng-container>

      <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
      <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
    </table>
    <mat-paginator [pageSizeOptions]="[10, 25, 50]" showFirstLastButtons></mat-paginator>
  `,
  styles: [`
    .menu-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
    .header-actions { display: flex; gap: 8px; align-items: center; }
    .menu-header h2 { margin: 0; }
    .stats-row { display: flex; gap: 16px; margin-bottom: 24px; }
    .stat-card { text-align: center; padding: 16px; flex: 1; }
    .stat-value { font-size: 24px; font-weight: 500; }
    .stat-label { color: #666; }
    .toolbar { margin-bottom: 12px; }
    .filter-field { width: 200px; }
    .full-width { width: 100%; }
    .item-thumb { width: 40px; height: 40px; object-fit: cover; border-radius: 4px; }
    .no-img { color: #999; font-size: 12px; }
  `]
})
export class MenuDashboardComponent implements OnInit, AfterViewInit {
  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  categories: Category[] = [];
  items: MenuItem[] = [];
  dataSource = new MatTableDataSource<MenuItem>([]);
  displayedColumns = ['image', 'name', 'category', 'price', 'available', 'actions'];
  categoryFilter: number | '' = '';
  bulkUploading = false;

  get availableCount(): number {
    return this.items.filter(i => i.available).length;
  }

  constructor(
    private menuService: MenuService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.menuService.getAdminCategories().subscribe(c => this.categories = c);
    this.menuService.getAdminMenuItems().subscribe(i => {
      this.items = i;
      this.dataSource.data = i;
      this.applyFilter();
    });
  }

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
  }

  getImageUrl(item: MenuItem): string | null {
    const url = item.imageUrl;
    if (!url) return null;
    if (url.startsWith('http')) return url;
    const base = environment.apiUrl.replace(/\/api\/?$/, '');
    return base + url;
  }

  applyFilter(): void {
    if (this.categoryFilter === '') {
      this.dataSource.filter = '';
      this.dataSource.filterPredicate = () => true;
    } else {
      const catId = this.categoryFilter as number;
      this.dataSource.filterPredicate = (item: MenuItem) => item.categoryId === catId || (item as any).category?.id === catId;
      this.dataSource.filter = 'x';
    }
    this.dataSource.paginator?.firstPage();
  }

  onBulkFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.bulkUploading = true;
    this.menuService.bulkUploadItems(file).subscribe({
      next: (res) => {
        this.bulkUploading = false;
        input.value = '';
        const msg = `Created: ${res.created}, Failed: ${res.failed}` + (res.errors?.length ? '. ' + res.errors.slice(0, 3).join('; ') : '');
        this.snackBar.open(msg, 'OK', { duration: 5000 });
        this.loadData();
      },
      error: () => { this.bulkUploading = false; input.value = ''; }
    });
  }

  duplicateItem(item: MenuItem): void {
    const copy = {
      ...item,
      id: undefined,
      name: item.name + ' (copy)',
      categoryId: (item as any).category?.id ?? item.categoryId,
      categoryName: (item as any).category?.name ?? item.categoryName
    } as any;
    delete copy.id;
    const dialogRef = this.dialog.open(MenuItemFormComponent, { width: '500px', data: { item: copy } });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.snackBar.open('Item duplicated', 'OK', { duration: 2000 });
        this.loadData();
      }
    });
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(MenuItemFormComponent, {
      width: '500px',
      data: {}
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.snackBar.open('Item created', 'OK', { duration: 2000 });
        this.loadData();
      }
    });
  }

  openEditDialog(item: MenuItem): void {
    const normalized = {
      ...item,
      categoryId: (item as any).category?.id ?? (item as any).categoryId,
      categoryName: (item as any).category?.name ?? (item as any).categoryName
    };
    const dialogRef = this.dialog.open(MenuItemFormComponent, {
      width: '500px',
      data: { item: normalized }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.snackBar.open('Item updated', 'OK', { duration: 2000 });
        this.loadData();
      }
    });
  }

  toggleAvailability(item: MenuItem): void {
    this.menuService.toggleAvailability(item.id).subscribe(() => this.loadData());
  }

  deleteItem(item: MenuItem): void {
    if (confirm(`Delete "${item.name}"?`)) {
      this.menuService.deleteItem(item.id).subscribe(() => {
        this.snackBar.open('Item deleted', 'OK', { duration: 2000 });
        this.loadData();
      });
    }
  }
}
