import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule, provideHttpClient, withInterceptors } from '@angular/common/http';
import { RouterModule, Routes } from '@angular/router';

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatSnackBarModule } from '@angular/material/snack-bar';

import { AppComponent } from './app.component';
import { AdminLoginComponent } from './features/auth/admin-login.component';
import { jwtInterceptor, errorInterceptor, authGuard, roleGuard, SharedModule } from '@shared';

const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'login', component: AdminLoginComponent },
  {
    path: 'dashboard',
    loadChildren: () => import('./features/dashboard/dashboard.module').then(m => m.DashboardModule),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN', 'MANAGER'] }
  },
  {
    path: 'menu',
    loadChildren: () => import('./features/menu-management/menu-management.module').then(m => m.MenuManagementModule),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'orders',
    loadChildren: () => import('./features/order-management/order-management.module').then(m => m.OrderManagementModule),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN', 'MANAGER'] }
  }
];

@NgModule({
  declarations: [AppComponent, AdminLoginComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    RouterModule.forRoot(routes),
    SharedModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatSidenavModule,
    MatListModule,
    MatSnackBarModule
  ],
  providers: [
    provideHttpClient(withInterceptors([jwtInterceptor, errorInterceptor]))
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
