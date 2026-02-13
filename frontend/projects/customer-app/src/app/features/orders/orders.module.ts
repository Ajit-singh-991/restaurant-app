import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { OrderListComponent } from './order-list/order-list.component';

const routes: Routes = [
  { path: '', component: OrderListComponent }
];

@NgModule({
  declarations: [OrderListComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    MatCardModule,
    MatChipsModule,
    MatIconModule,
    MatProgressSpinnerModule
  ]
})
export class OrdersModule {}
