import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatDialogModule } from '@angular/material/dialog';
import { MatRadioModule } from '@angular/material/radio';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCheckboxModule } from '@angular/material/checkbox';

import { BillViewComponent } from './bill-view/bill-view.component';
import { PaymentDialogComponent } from './payment-dialog/payment-dialog.component';
import { InvoiceViewComponent } from './invoice-view/invoice-view.component';
import { SplitBillDialogComponent } from './split-bill-dialog/split-bill-dialog.component';

const routes: Routes = [
  { path: ':orderId', component: BillViewComponent },
  { path: ':orderId/invoice', component: InvoiceViewComponent }
];

@NgModule({
  declarations: [
    BillViewComponent,
    PaymentDialogComponent,
    InvoiceViewComponent,
    SplitBillDialogComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule.forChild(routes),
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatDialogModule,
    MatRadioModule,
    MatFormFieldModule,
    MatInputModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatButtonToggleModule,
    MatCheckboxModule
  ]
})
export class BillingModule {}
