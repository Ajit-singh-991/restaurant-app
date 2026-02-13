import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '@shared';

@Component({
  selector: 'app-root',
  template: `
    <mat-sidenav-container class="sidenav-container">
      <mat-sidenav #sidenav mode="side" opened class="sidenav">
        <div class="brand">
          <mat-icon>restaurant</mat-icon>
          <h3>Admin Panel</h3>
        </div>
        <mat-nav-list>
          <a mat-list-item routerLink="/dashboard" routerLinkActive="active">
            <mat-icon matListItemIcon>dashboard</mat-icon>
            <span matListItemTitle>Dashboard</span>
          </a>
          <a mat-list-item routerLink="/menu" routerLinkActive="active">
            <mat-icon matListItemIcon>restaurant_menu</mat-icon>
            <span matListItemTitle>Menu Management</span>
          </a>
          <a mat-list-item routerLink="/orders" routerLinkActive="active">
            <mat-icon matListItemIcon>receipt</mat-icon>
            <span matListItemTitle>Orders</span>
          </a>
          <a mat-list-item routerLink="/dashboard/reports" routerLinkActive="active">
            <mat-icon matListItemIcon>assessment</mat-icon>
            <span matListItemTitle>Reports</span>
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
          <span>Restaurant Admin</span>
          <span style="flex:1 1 auto"></span>
          <span class="user-name">{{ authService.getUsername() }}</span>
          <button mat-icon-button (click)="logout()" matTooltip="Logout">
            <mat-icon>logout</mat-icon>
          </button>
        </mat-toolbar>
        <div class="content">
          <router-outlet></router-outlet>
        </div>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`
    .sidenav-container { height: 100vh; }
    .sidenav { width: 250px; display: flex; flex-direction: column; }
    .brand { display: flex; align-items: center; gap: 8px; padding: 16px; border-bottom: 1px solid #ddd; }
    .brand h3 { margin: 0; }
    .sidenav-footer { margin-top: auto; padding: 16px; border-top: 1px solid #e0e0e0; }
    .logout-btn { width: 100%; }
    .content { padding: 16px; }
    .active { background: rgba(0,0,0,0.1); }
    .user-name { font-size: 14px; margin-right: 8px; opacity: 0.8; }
  `]
})
export class AppComponent {
  constructor(public authService: AuthService, private router: Router) {}

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
