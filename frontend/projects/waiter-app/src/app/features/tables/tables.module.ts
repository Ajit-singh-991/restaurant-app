import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';

import { TableGridComponent } from './table-grid/table-grid.component';

const routes: Routes = [
  { path: '', component: TableGridComponent }
];

@NgModule({
  declarations: [TableGridComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule
  ]
})
export class TablesModule {}
