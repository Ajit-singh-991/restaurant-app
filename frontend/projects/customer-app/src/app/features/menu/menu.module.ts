import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatBadgeModule } from '@angular/material/badge';

import { MenuListComponent } from './menu-list/menu-list.component';
import { MenuItemCardComponent } from './menu-item-card/menu-item-card.component';

const routes: Routes = [
  { path: '', component: MenuListComponent }
];

@NgModule({
  declarations: [
    MenuListComponent,
    MenuItemCardComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule.forChild(routes),
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatTabsModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatBadgeModule
  ]
})
export class MenuModule {}
