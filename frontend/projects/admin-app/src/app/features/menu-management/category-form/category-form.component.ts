import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MenuService, Category } from '@shared';

@Component({
  selector: 'app-category-form',
  template: `
    <h2 mat-dialog-title>{{ data?.category ? 'Edit' : 'Add' }} Category</h2>
    <form [formGroup]="form" (ngSubmit)="save()">
      <mat-dialog-content>
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Name</mat-label>
          <input matInput formControlName="name" required>
          <mat-error>Name is required</mat-error>
        </mat-form-field>
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Description</mat-label>
          <input matInput formControlName="description">
        </mat-form-field>
        <div class="full-width">
          <label class="label">Category Image</label>
          <app-image-upload [currentUrl]="form.get('imageUrl')?.value || ''"
                            (urlChange)="form.get('imageUrl')?.setValue($event)">
          </app-image-upload>
        </div>
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Display Order</mat-label>
          <input matInput type="number" formControlName="displayOrder" min="0">
        </mat-form-field>
        <mat-slide-toggle formControlName="active">Active</mat-slide-toggle>
      </mat-dialog-content>
      <mat-dialog-actions align="end">
        <button mat-button type="button" mat-dialog-close>Cancel</button>
        <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid || saving">
          {{ saving ? 'Saving...' : 'Save' }}
        </button>
      </mat-dialog-actions>
    </form>
  `,
  styles: [`
    .full-width { width: 100%; }
    mat-dialog-content { display: flex; flex-direction: column; min-width: 320px; }
    .label { display: block; margin-bottom: 8px; color: rgba(0,0,0,0.6); font-size: 12px; }
  `]
})
export class CategoryFormComponent {
  form: FormGroup;
  saving = false;

  constructor(
    private fb: FormBuilder,
    private menuService: MenuService,
    private dialogRef: MatDialogRef<CategoryFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { category?: Category }
  ) {
    const c = data?.category;
    this.form = this.fb.group({
      name: [c?.name ?? '', Validators.required],
      description: [c?.description ?? ''],
      imageUrl: [c?.imageUrl ?? ''],
      displayOrder: [c?.displayOrder ?? 0],
      active: [c?.active ?? true]
    });
  }

  save(): void {
    if (this.form.invalid) return;
    this.saving = true;
    const value = this.form.value;
    if (this.data?.category) {
      this.menuService.updateCategory(this.data.category.id, value).subscribe({
        next: () => { this.dialogRef.close(true); },
        error: () => { this.saving = false; }
      });
    } else {
      this.menuService.createCategory(value).subscribe({
        next: () => { this.dialogRef.close(true); },
        error: () => { this.saving = false; }
      });
    }
  }
}
