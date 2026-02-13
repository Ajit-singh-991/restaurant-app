import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule, provideHttpClient, withInterceptors } from '@angular/common/http';
import { RouterModule, Routes } from '@angular/router';

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatBadgeModule } from '@angular/material/badge';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { DragDropModule } from '@angular/cdk/drag-drop';

import { AppComponent } from './app.component';
import { KitchenDisplayComponent } from './features/kitchen/kitchen-display/kitchen-display.component';
import { OrderCardComponent } from './features/kitchen/order-card/order-card.component';
import { KitchenLoginComponent } from './features/auth/kitchen-login.component';
import { jwtInterceptor, errorInterceptor, authGuard, roleGuard, SharedModule } from '@shared';

const routes: Routes = [
  { path: '', component: KitchenDisplayComponent, canActivate: [authGuard, roleGuard], data: { roles: ['KITCHEN', 'ADMIN'] } },
  { path: 'login', component: KitchenLoginComponent }
];

@NgModule({
  declarations: [
    AppComponent,
    KitchenDisplayComponent,
    OrderCardComponent,
    KitchenLoginComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    RouterModule.forRoot(routes),
    SharedModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatBadgeModule,
    MatCardModule,
    MatChipsModule,
    MatCheckboxModule,
    MatSnackBarModule,
    MatTooltipModule,
    MatFormFieldModule,
    MatSelectModule,
    DragDropModule
  ],
  providers: [
    provideHttpClient(withInterceptors([jwtInterceptor, errorInterceptor]))
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
