import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Order, OrderService, SplitRequest } from '@shared';

type SplitMode = 'equal' | 'percentage' | 'custom';

@Component({
  selector: 'app-split-bill-dialog',
  template: `
    <h2 mat-dialog-title>Split Bill</h2>
    <mat-dialog-content>
      <p class="total">Total: {{ data.order.totalAmount | currency:'INR' }}</p>

      <mat-button-toggle-group [(ngModel)]="mode" (ngModelChange)="onModeChange()" class="mode-toggle">
        <mat-button-toggle value="equal">Equal</mat-button-toggle>
        <mat-button-toggle value="percentage">By %</mat-button-toggle>
        <mat-button-toggle value="custom">Custom</mat-button-toggle>
      </mat-button-toggle-group>

      <div *ngIf="mode === 'equal'" class="field">
        <mat-form-field appearance="outline">
          <mat-label>Number of ways</mat-label>
          <input matInput type="number" min="2" [(ngModel)]="ways" (ngModelChange)="loadSplit()">
        </mat-form-field>
      </div>

      <div *ngIf="mode === 'percentage'" class="field">
        <p class="hint">Enter percentages that sum to 100.</p>
        <div class="percent-row" *ngFor="let p of percentages; let i = index">
          <mat-form-field appearance="outline" class="percent-field">
            <mat-label>Person {{ i + 1 }} %</mat-label>
            <input matInput type="number" min="0" max="100" [(ngModel)]="percentages[i]" (ngModelChange)="loadSplit()">
          </mat-form-field>
        </div>
        <button mat-stroked-button (click)="addPercentPerson()">Add person</button>
        <p class="sum" *ngIf="percentSum !== null" [class.invalid]="percentSum !== 100">Sum: {{ percentSum }}%</p>
      </div>

      <div *ngIf="mode === 'custom'" class="field">
        <p class="hint">Assign items to each person. Each item goes to one person.</p>
        <div class="person-bucket" *ngFor="let bucket of personItemIds; let pi = index">
          <strong>Person {{ pi + 1 }}</strong>
          <div *ngFor="let item of data.order.items" class="item-row">
            <mat-checkbox [checked]="isItemAssigned(item.id, pi)" (change)="toggleItem(item.id, pi)">
              {{ item.quantity }}x {{ item.menuItemName }} — {{ item.totalPrice | currency:'INR' }}
            </mat-checkbox>
          </div>
        </div>
        <button mat-stroked-button (click)="addCustomPerson()">Add person</button>
        <button mat-stroked-button (click)="loadSplit()">Calculate</button>
      </div>

      <p class="per-person" *ngIf="amountsPerPerson.length">
        <strong>Amounts:</strong>
        <span *ngFor="let a of amountsPerPerson; let i = index">
          Person {{ i + 1 }}: {{ a | currency:'INR' }}{{ i < amountsPerPerson.length - 1 ? '; ' : '' }}
        </span>
      </p>
      <p class="loading" *ngIf="loading">Calculating...</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Close</button>
      <button mat-raised-button color="primary" mat-dialog-close>OK</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .total { font-size: 18px; margin-bottom: 16px; }
    .mode-toggle { margin-bottom: 16px; }
    .field { margin: 12px 0; }
    .hint { font-size: 12px; color: #666; margin-bottom: 8px; }
    .percent-row { display: inline-block; margin-right: 12px; }
    .percent-field { width: 120px; }
    .sum.invalid { color: #f44336; }
    .per-person { margin-top: 16px; padding: 12px; background: #f5f5f5; border-radius: 8px; }
    .loading { color: #666; font-size: 13px; }
    mat-form-field { width: 100%; }
    .person-bucket { margin-bottom: 16px; padding: 12px; border: 1px solid #ddd; border-radius: 8px; }
    .item-row { margin: 4px 0; }
  `]
})
export class SplitBillDialogComponent {
  mode: SplitMode = 'equal';
  ways = 2;
  percentages: number[] = [50, 50];
  personItemIds: number[][] = [[], []];
  amountsPerPerson: number[] = [];
  loading = false;

  get percentSum(): number | null {
    if (this.mode !== 'percentage') return null;
    return this.percentages.reduce((a, b) => a + b, 0);
  }

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { order: Order },
    private orderService: OrderService,
    private dialogRef: MatDialogRef<SplitBillDialogComponent>
  ) {
    this.loadSplit();
  }

  onModeChange(): void {
    if (this.mode === 'percentage') this.percentages = [50, 50];
    if (this.mode === 'custom') this.personItemIds = [[], []];
    this.loadSplit();
  }

  addPercentPerson(): void {
    this.percentages.push(0);
    this.loadSplit();
  }

  addCustomPerson(): void {
    this.personItemIds.push([]);
  }

  isItemAssigned(itemId: number, personIndex: number): boolean {
    return (this.personItemIds[personIndex] || []).includes(itemId);
  }

  toggleItem(itemId: number, personIndex: number): void {
    // Remove item from all other persons first (each item can only be in one person)
    this.personItemIds.forEach((arr, i) => {
      if (i !== personIndex) {
        const idx = arr.indexOf(itemId);
        if (idx >= 0) arr.splice(idx, 1);
      }
    });
    const list = this.personItemIds[personIndex] || [];
    const idx = list.indexOf(itemId);
    if (idx >= 0) list.splice(idx, 1);
    else list.push(itemId);
    this.loadSplit();
  }

  loadSplit(): void {
    if (!this.data.order?.id) return;
    let req: SplitRequest;
    if (this.mode === 'equal') {
      if (this.ways < 2) return;
      req = { numberOfWays: this.ways };
    } else if (this.mode === 'percentage') {
      if (this.percentSum !== 100) return;
      req = { percentages: this.percentages };
    } else {
      const assigned = this.personItemIds.filter(arr => arr.length > 0);
      if (assigned.length < 2) return;
      req = { personItemIds: assigned };
    }
    this.loading = true;
    this.orderService.calculateSplit(this.data.order.id, req).subscribe({
      next: (res) => { this.amountsPerPerson = res.amountsPerPerson || []; this.loading = false; },
      error: () => this.loading = false
    });
  }
}
