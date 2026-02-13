import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';

import { OrderListComponent } from './order-list/order-list.component';

const routes: Routes = [
  { path: '', component: OrderListComponent }
];

@NgModule({
  declarations: [OrderListComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    MatTableModule,
    MatCardModule,
    MatChipsModule,
    MatButtonModule
  ]
})
export class OrderManagementModule {}
