import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MenuService, MenuItem, Category, CreateMenuItemRequest, KitchenService, KitchenStation } from '@shared';

@Component({
  selector: 'app-menu-item-form',
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Edit' : 'Add' }} Menu Item</h2>
    <form [formGroup]="form" (ngSubmit)="save()">
      <mat-dialog-content class="form-content">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Name</mat-label>
          <input matInput formControlName="name" required>
          <mat-error>Name is required</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Category</mat-label>
          <mat-select formControlName="categoryId" required>
            <mat-option *ngFor="let cat of categories" [value]="cat.id">
              {{ cat.name }}
            </mat-option>
          </mat-select>
          <mat-error>Category is required</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Kitchen Station</mat-label>
          <mat-select formControlName="stationId">
            <mat-option [value]="null">— None —</mat-option>
            <mat-option *ngFor="let s of stations" [value]="s.id">
              {{ s.name }}
            </mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Description</mat-label>
          <textarea matInput formControlName="description" rows="3"></textarea>
        </mat-form-field>

        <div class="row">
          <mat-form-field appearance="outline">
            <mat-label>Price</mat-label>
            <input matInput type="number" formControlName="price" min="0.01" step="0.01" required>
            <span matPrefix>&#8377;&nbsp;</span>
            <mat-error>Price is required</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Prep Time (min)</mat-label>
            <input matInput type="number" formControlName="preparationTimeMinutes" min="0">
          </mat-form-field>
        </div>

        <div class="full-width">
          <label class="label">Image</label>
          <app-image-upload [currentUrl]="form.get('imageUrl')?.value || ''"
                            (urlChange)="form.get('imageUrl')?.setValue($event)">
          </app-image-upload>
        </div>

        <div class="full-width allergen-chips">
          <label class="label">Allergens (select all that apply)</label>
          <mat-chip-listbox formControlName="allergens" multiple aria-label="Allergens">
            <mat-chip-option *ngFor="let a of allergenOptions" [value]="a">{{ a }}</mat-chip-option>
          </mat-chip-listbox>
        </div>

        <div class="toggles">
          <mat-slide-toggle formControlName="vegetarian">Vegetarian</mat-slide-toggle>
          <mat-slide-toggle formControlName="vegan">Vegan</mat-slide-toggle>
          <mat-slide-toggle formControlName="glutenFree">Gluten Free</mat-slide-toggle>
        </div>
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-button mat-dialog-close type="button">Cancel</button>
        <button mat-raised-button color="primary" type="submit"
                [disabled]="saving || form.invalid">
          {{ saving ? 'Saving...' : (isEdit ? 'Update' : 'Create') }}
        </button>
      </mat-dialog-actions>
    </form>
  `,
  styles: [`
    .form-content { display: flex; flex-direction: column; min-width: 400px; }
    .full-width { width: 100%; }
    .row { display: flex; gap: 16px; }
    .row mat-form-field { flex: 1; }
    .toggles { display: flex; gap: 24px; margin: 8px 0 16px; }
    .label { display: block; margin-bottom: 8px; color: rgba(0,0,0,0.6); font-size: 12px; }
    .allergen-chips mat-chip-listbox { display: block; margin-bottom: 8px; }
    .allergen-chips mat-chip-option { margin: 4px; }
  `]
})
export class MenuItemFormComponent implements OnInit {
  form!: FormGroup;
  categories: Category[] = [];
  stations: KitchenStation[] = [];
  saving = false;
  isEdit = false;
  allergenOptions = ['Nuts', 'Gluten', 'Dairy', 'Shellfish', 'Eggs'];

  constructor(
    private fb: FormBuilder,
    private menuService: MenuService,
    private kitchenService: KitchenService,
    private dialogRef: MatDialogRef<MenuItemFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { item?: MenuItem }
  ) {}

  ngOnInit(): void {
    this.isEdit = !!(this.data?.item && (this.data.item as any).id != null);
    const item = this.data?.item;
    const allergensValue = (item?.allergens || '').split(',').map(s => s.trim()).filter(Boolean);

    this.form = this.fb.group({
      name: [item?.name || '', Validators.required],
      categoryId: [item?.categoryId || null, Validators.required],
      stationId: [item?.stationId ?? null],
      description: [item?.description || ''],
      price: [item?.price || null, [Validators.required, Validators.min(0.01)]],
      preparationTimeMinutes: [item?.preparationTimeMinutes || null],
      imageUrl: [item?.imageUrl || ''],
      allergens: [allergensValue],
      vegetarian: [item?.vegetarian || false],
      vegan: [item?.vegan || false],
      glutenFree: [item?.glutenFree || false]
    });

    this.menuService.getAdminCategories().subscribe(cats => this.categories = cats);
    this.kitchenService.getStations().subscribe(stations => this.stations = stations);
  }

  save(): void {
    if (this.form.invalid || this.saving) return;
    this.saving = true;

    const raw = this.form.value;
    const request: CreateMenuItemRequest = {
      ...raw,
      allergens: Array.isArray(raw.allergens) ? raw.allergens.filter(Boolean).join(', ') : (raw.allergens || ''),
      stationId: raw.stationId || undefined
    };
    const action = this.isEdit
      ? this.menuService.updateItem(this.data.item!.id, request)
      : this.menuService.createItem(request);

    action.subscribe({
      next: (result) => {
        this.saving = false;
        this.dialogRef.close(result);
      },
      error: () => this.saving = false
    });
  }
}
