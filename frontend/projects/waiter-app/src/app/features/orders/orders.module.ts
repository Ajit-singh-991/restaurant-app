import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBarModule } from '@angular/material/snack-bar';

import { WaiterOrdersComponent } from './waiter-orders/waiter-orders.component';

const routes: Routes = [
  { path: '', component: WaiterOrdersComponent }
];

@NgModule({
  declarations: [WaiterOrdersComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatSnackBarModule
  ]
})
export class OrdersModule {}
