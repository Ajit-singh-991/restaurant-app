import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MenuService, MenuItem } from '@shared';
import { environment } from '@environments/environment';
import { MenuItemFormComponent } from '../menu-item-form/menu-item-form.component';

@Component({
  selector: 'app-item-details',
  template: `
    <div class="details" *ngIf="item">
      <button mat-button routerLink="/menu"><mat-icon>arrow_back</mat-icon> Back</button>
      <h2>{{ item.name }}</h2>
      <p class="description">{{ item.description }}</p>
      <p><strong>Price:</strong> {{ item.price | currency:'INR' }}</p>
      <p><strong>Category:</strong> {{ categoryName }}</p>
      <p><strong>Status:</strong> {{ item.available ? 'Available' : 'Unavailable' }}</p>
      <img *ngIf="item.imageUrl" [src]="imageFullUrl" class="thumb" alt="">
      <div class="actions">
        <button mat-raised-button color="primary" (click)="openEdit()">Edit</button>
      </div>
    </div>
    <div *ngIf="!item && !loading" class="loading">Item not found</div>
    <div *ngIf="loading" class="loading"><mat-spinner diameter="40"></mat-spinner></div>
  `,
  styles: [`
    .details { max-width: 600px; padding: 16px; }
    .description { color: #666; }
    .thumb { max-width: 200px; max-height: 150px; object-fit: contain; display: block; margin: 8px 0; }
    .actions { margin-top: 16px; }
    .loading { padding: 24px; text-align: center; }
  `]
})
export class ItemDetailsComponent implements OnInit {
  item: MenuItem | null = null;
  categoryName = '';
  loading = true;

  get imageFullUrl(): string {
    if (!this.item?.imageUrl) return '';
    const url = this.item.imageUrl;
    if (url.startsWith('http')) return url;
    const base = environment.apiUrl.replace(/\/api\/?$/, '');
    return base + (url.startsWith('/') ? url : '/' + url);
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private menuService: MenuService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  openEdit(): void {
    const itemForForm = this.item && {
      ...this.item,
      categoryId: (this.item as any).category?.id ?? (this.item as any).categoryId ?? 0,
      categoryName: (this.item as any).category?.name ?? this.categoryName
    };
    const ref = this.dialog.open(MenuItemFormComponent, { width: '500px', data: { item: itemForForm } });
    ref.afterClosed().subscribe(ok => {
      if (ok) { this.snackBar.open('Item updated', 'OK', { duration: 2000 }); this.ngOnInit(); }
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) { this.loading = false; return; }
    this.menuService.getItemById(+id).subscribe({
      next: item => {
        this.item = item as MenuItem;
        if ((this.item as any).category?.name) this.categoryName = (this.item as any).category.name;
        else if ((this.item as any).categoryName) this.categoryName = (this.item as any).categoryName;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }
}
