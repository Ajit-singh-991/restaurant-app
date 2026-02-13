import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '@shared';

@Component({
  selector: 'app-root',
  template: `
    <mat-sidenav-container class="sidenav-container">
      <mat-sidenav #sidenav mode="side" opened class="sidenav">
        <div class="sidenav-header">
          <mat-icon>room_service</mat-icon>
          <span>{{ authService.getUsername() || 'Waiter' }}</span>
        </div>
        <mat-nav-list>
          <a mat-list-item routerLink="/tables" routerLinkActive="active">
            <mat-icon matListItemIcon>table_restaurant</mat-icon>
            <span matListItemTitle>Tables</span>
          </a>
          <a mat-list-item routerLink="/orders" routerLinkActive="active">
            <mat-icon matListItemIcon>receipt</mat-icon>
            <span matListItemTitle>Orders</span>
          </a>
        </mat-nav-list>
        <div class="sidenav-footer">
          <button mat-button color="warn" (click)="logout()" class="logout-btn">
            <mat-icon>logout</mat-icon> Logout
          </button>
        </div>
      </mat-sidenav>
      <mat-sidenav-content>
        <mat-toolbar color="primary">
          <button mat-icon-button (click)="sidenav.toggle()">
            <mat-icon>menu</mat-icon>
          </button>
          <span>Waiter Panel</span>
          <span style="flex:1 1 auto"></span>
          <span class="user-name">{{ authService.getUsername() }}</span>
        </mat-toolbar>
        <router-outlet></router-outlet>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`
    .sidenav-container { height: 100vh; }
    .sidenav { width: 220px; display: flex; flex-direction: column; }
    .sidenav-header { display: flex; align-items: center; gap: 8px; padding: 16px; font-weight: 500; border-bottom: 1px solid #e0e0e0; }
    .sidenav-footer { margin-top: auto; padding: 16px; border-top: 1px solid #e0e0e0; }
    .logout-btn { width: 100%; }
    .active { background: rgba(0,0,0,0.1); }
    .user-name { font-size: 14px; opacity: 0.8; }
  `]
})
export class AppComponent {
  constructor(public authService: AuthService, private router: Router) {}

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
