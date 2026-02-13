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
import { MatBadgeModule } from '@angular/material/badge';
import { MatSnackBarModule } from '@angular/material/snack-bar';

import { AppComponent } from './app.component';
import { WaiterLoginComponent } from './features/auth/waiter-login.component';
import { jwtInterceptor, errorInterceptor, authGuard, roleGuard, SharedModule } from '@shared';

const routes: Routes = [
  { path: '', redirectTo: 'tables', pathMatch: 'full' },
  { path: 'login', component: WaiterLoginComponent },
  {
    path: 'tables',
    loadChildren: () => import('./features/tables/tables.module').then(m => m.TablesModule),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['WAITER', 'ADMIN'] }
  },
  {
    path: 'orders',
    loadChildren: () => import('./features/orders/orders.module').then(m => m.OrdersModule),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['WAITER', 'ADMIN'] }
  },
  {
    path: 'billing',
    loadChildren: () => import('./features/billing/billing.module').then(m => m.BillingModule),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['WAITER', 'ADMIN'] }
  }
];

@NgModule({
  declarations: [AppComponent, WaiterLoginComponent],
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
    MatBadgeModule,
    MatSnackBarModule
  ],
  providers: [
    provideHttpClient(withInterceptors([jwtInterceptor, errorInterceptor]))
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
