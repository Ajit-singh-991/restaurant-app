import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '@shared';

@Component({
  selector: 'app-kitchen-login',
  template: `
    <lib-login-page
      title="Kitchen Display"
      subtitle="Sign in with your kitchen credentials"
      icon="restaurant"
      [showHint]="true"
      [error]="error"
      [loading]="loading"
      (loginSubmit)="onLogin($event)">
    </lib-login-page>
  `,
  styles: [`:host { display: block; background: #1a1a2e; }`]
})
export class KitchenLoginComponent {
  error = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) {
    if (authService.isAuthenticated()) {
      router.navigate(['/']);
    }
  }

  onLogin(credentials: { username: string; password: string }): void {
    this.loading = true;
    this.error = '';

    this.authService.login(credentials).subscribe({
      next: (response) => {
        if (response.role !== 'KITCHEN' && response.role !== 'ADMIN') {
          this.authService.logout();
          this.error = 'Access denied. Kitchen staff or admin role required.';
          this.loading = false;
          return;
        }
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.error = err.error?.message || 'Invalid credentials';
        this.loading = false;
      }
    });
  }
}
