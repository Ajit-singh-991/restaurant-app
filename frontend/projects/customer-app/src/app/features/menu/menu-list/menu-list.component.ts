import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { MenuService, Category, MenuItem } from '@shared';

@Component({
  selector: 'app-menu-list',
  template: `
    <div class="menu-container">
      <div class="menu-header">
        <h1>Our Menu</h1>
        <mat-form-field appearance="outline" class="search-field">
          <mat-icon matPrefix>search</mat-icon>
          <input matInput [formControl]="searchControl" placeholder="Search dishes...">
        </mat-form-field>
      </div>

      <div class="filter-row">
        <div class="filter-chips">
          <button mat-stroked-button (click)="toggleDietary('vegetarian')"
                  [color]="dietaryFilter.includes('vegetarian') ? 'primary' : ''">
            Vegetarian
          </button>
          <button mat-stroked-button (click)="toggleDietary('vegan')"
                  [color]="dietaryFilter.includes('vegan') ? 'primary' : ''">
            Vegan
          </button>
          <button mat-stroked-button (click)="toggleDietary('glutenFree')"
                  [color]="dietaryFilter.includes('glutenFree') ? 'primary' : ''">
            Gluten Free
          </button>
        </div>
        <div class="allergen-chips" *ngIf="allergenOptions.length">
          <span class="filter-label">Exclude:</span>
          <button mat-stroked-button *ngFor="let a of allergenOptions"
                  (click)="toggleAllergen(a)"
                  [color]="excludeAllergens.includes(a) ? 'warn' : ''">
            {{ a }}
          </button>
        </div>
        <div class="price-range">
          <mat-form-field appearance="outline" class="price-field">
            <mat-label>Min price</mat-label>
            <input matInput type="number" min="0" [(ngModel)]="minPrice" (ngModelChange)="applyFilters()" placeholder="0">
          </mat-form-field>
          <mat-form-field appearance="outline" class="price-field">
            <mat-label>Max price</mat-label>
            <input matInput type="number" min="0" [(ngModel)]="maxPrice" (ngModelChange)="applyFilters()" placeholder="Any">
          </mat-form-field>
        </div>
      </div>

      <mat-tab-group (selectedTabChange)="onCategoryChange($event)" dynamicHeight>
        <mat-tab label="All">
          <ng-template matTabContent>
            <div class="menu-grid" *ngIf="!loading; else loadingTpl">
              <app-menu-item-card
                *ngFor="let item of filteredItems"
                [item]="item">
              </app-menu-item-card>
            </div>
          </ng-template>
        </mat-tab>
        <mat-tab *ngFor="let category of categories" [label]="category.name">
          <ng-template matTabContent>
            <div class="menu-grid" *ngIf="!loading; else loadingTpl">
              <app-menu-item-card
                *ngFor="let item of filteredItems"
                [item]="item">
              </app-menu-item-card>
            </div>
          </ng-template>
        </mat-tab>
      </mat-tab-group>

      <div *ngIf="filteredItems.length === 0 && !loading" class="empty-state">
        <mat-icon>search_off</mat-icon>
        <p>No items found</p>
      </div>
    </div>

    <ng-template #loadingTpl>
      <div class="loading">
        <mat-spinner diameter="40"></mat-spinner>
        <p>Loading menu...</p>
      </div>
    </ng-template>
  `,
  styles: [`
    .menu-container { padding: 16px; max-width: 1200px; margin: 0 auto; }
    .menu-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px; margin-bottom: 16px; }
    .menu-header h1 { margin: 0; }
    .search-field { width: 300px; }
    .filter-row { margin-bottom: 16px; display: flex; flex-wrap: wrap; align-items: center; gap: 16px; }
    .filter-chips { display: flex; flex-wrap: wrap; gap: 8px; }
    .allergen-chips { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; }
    .filter-label { font-size: 13px; color: #666; margin-right: 4px; }
    .price-range { display: flex; gap: 8px; align-items: center; }
    .price-field { width: 100px; }
    .menu-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; padding: 16px 0; }
    .loading { display: flex; flex-direction: column; align-items: center; padding: 48px; }
    .empty-state { text-align: center; padding: 48px; color: #666; }
    .empty-state mat-icon { font-size: 48px; width: 48px; height: 48px; }
    @media (max-width: 600px) {
      .search-field { width: 100%; }
      .menu-grid { grid-template-columns: 1fr; }
    }
  `]
})
export class MenuListComponent implements OnInit {
  categories: Category[] = [];
  allItems: MenuItem[] = [];
  filteredItems: MenuItem[] = [];
  loading = true;
  searchControl = new FormControl('');
  selectedCategory: number | null = null;
  dietaryFilter: string[] = [];
  excludeAllergens: string[] = [];
  /** Common allergen labels for filter chips; items with these in allergens string are excluded when selected */
  allergenOptions = ['Nuts', 'Gluten', 'Dairy', 'Shellfish', 'Eggs'];
  minPrice: number | null = null;
  maxPrice: number | null = null;

  constructor(private menuService: MenuService) {}

  ngOnInit(): void {
    this.loadMenu();
    this.searchControl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(query => this.filterItems(query || ''));
  }

  loadMenu(): void {
    this.menuService.getCategories().subscribe(cats => this.categories = cats);
    this.menuService.getMenuItems().subscribe(items => {
      this.allItems = items;
      this.applyFilters();
      this.loading = false;
    });
  }

  onCategoryChange(event: any): void {
    const index = event.index;
    if (index === 0) {
      this.selectedCategory = null;
    } else {
      this.selectedCategory = this.categories[index - 1].id;
    }
    this.applyFilters();
  }

  filterItems(query: string): void {
    this.applyFilters();
  }

  toggleDietary(key: string): void {
    const idx = this.dietaryFilter.indexOf(key);
    if (idx >= 0) this.dietaryFilter.splice(idx, 1);
    else this.dietaryFilter.push(key);
    this.applyFilters();
  }

  toggleAllergen(allergen: string): void {
    const idx = this.excludeAllergens.indexOf(allergen);
    if (idx >= 0) this.excludeAllergens.splice(idx, 1);
    else this.excludeAllergens.push(allergen);
    this.applyFilters();
  }

  applyFilters(): void {
    let items = this.selectedCategory
      ? this.allItems.filter(i => i.categoryId === this.selectedCategory)
      : this.allItems;

    const query = this.searchControl.value?.trim() || '';
    if (query) {
      const lower = query.toLowerCase();
      items = items.filter(i => i.name.toLowerCase().includes(lower) || i.description?.toLowerCase().includes(lower));
    }

    if (this.dietaryFilter.length > 0) {
      items = items.filter(i => {
        if (this.dietaryFilter.includes('vegetarian') && !i.vegetarian) return false;
        if (this.dietaryFilter.includes('vegan') && !i.vegan) return false;
        if (this.dietaryFilter.includes('glutenFree') && !i.glutenFree) return false;
        return true;
      });
    }

    if (this.minPrice != null && this.minPrice > 0) {
      items = items.filter(i => i.price >= this.minPrice!);
    }
    if (this.maxPrice != null && this.maxPrice > 0) {
      items = items.filter(i => i.price <= this.maxPrice!);
    }

    if (this.excludeAllergens.length > 0) {
      const lowerAllergens = this.excludeAllergens.map(a => a.toLowerCase());
      items = items.filter(i => {
        const itemAllergens = (i.allergens || '').toLowerCase();
        return !lowerAllergens.some(a => itemAllergens.includes(a));
      });
    }

    this.filteredItems = items;
  }
}
