import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { NgChartsModule } from 'ng2-charts';

import { DashboardHomeComponent } from './dashboard-home/dashboard-home.component';
import { ReportsComponent } from './reports/reports.component';

const routes: Routes = [
  { path: '', component: DashboardHomeComponent },
  { path: 'reports', component: ReportsComponent }
];

@NgModule({
  declarations: [DashboardHomeComponent, ReportsComponent],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule.forChild(routes),
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTableModule,
    NgChartsModule
  ]
})
export class DashboardModule {}
