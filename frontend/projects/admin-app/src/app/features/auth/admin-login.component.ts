import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '@shared';

@Component({
  selector: 'app-admin-login',
  template: `
    <lib-login-page
      title="Admin Dashboard"
      subtitle="Sign in with your admin credentials"
      icon="admin_panel_settings"
      [showHint]="true"
      [error]="error"
      [loading]="loading"
      (loginSubmit)="onLogin($event)">
    </lib-login-page>
  `
})
export class AdminLoginComponent {
  error = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) {
    if (authService.isAuthenticated()) {
      router.navigate(['/dashboard']);
    }
  }

  onLogin(credentials: { username: string; password: string }): void {
    this.loading = true;
    this.error = '';

    this.authService.login(credentials).subscribe({
      next: (response) => {
        if (response.role !== 'ADMIN' && response.role !== 'MANAGER') {
          this.authService.logout();
          this.error = 'Access denied. Admin or manager role required.';
          this.loading = false;
          return;
        }
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.error = err.error?.message || 'Invalid credentials';
        this.loading = false;
      }
    });
  }
}
