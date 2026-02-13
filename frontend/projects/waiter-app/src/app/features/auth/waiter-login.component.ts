import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '@shared';

@Component({
  selector: 'app-waiter-login',
  template: `
    <lib-login-page
      title="Waiter Panel"
      subtitle="Sign in with your waiter credentials"
      icon="room_service"
      [showHint]="true"
      [error]="error"
      [loading]="loading"
      (loginSubmit)="onLogin($event)">
    </lib-login-page>
  `
})
export class WaiterLoginComponent {
  error = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) {
    if (authService.isAuthenticated()) {
      router.navigate(['/tables']);
    }
  }

  onLogin(credentials: { username: string; password: string }): void {
    this.loading = true;
    this.error = '';

    this.authService.login(credentials).subscribe({
      next: (response) => {
        if (response.role !== 'WAITER' && response.role !== 'ADMIN') {
          this.authService.logout();
          this.error = 'Access denied. Waiter or admin role required.';
          this.loading = false;
          return;
        }
        this.router.navigate(['/tables']);
      },
      error: (err) => {
        this.error = err.error?.message || 'Invalid credentials';
        this.loading = false;
      }
    });
  }
}
