import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '@shared';

@Component({
  selector: 'app-login',
  template: `
    <div class="auth-container">
      <mat-card class="auth-card">
        <mat-card-header>
          <mat-card-title>Login</mat-card-title>
          <mat-card-subtitle>Sign in to your account</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Username</mat-label>
              <input matInput formControlName="username" placeholder="Enter username">
              <mat-error *ngIf="loginForm.get('username')?.hasError('required')">
                Username is required
              </mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Password</mat-label>
              <input matInput [type]="hidePassword ? 'password' : 'text'" formControlName="password">
              <button mat-icon-button matSuffix type="button" (click)="hidePassword = !hidePassword">
                <mat-icon>{{ hidePassword ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              <mat-error *ngIf="loginForm.get('password')?.hasError('required')">
                Password is required
              </mat-error>
            </mat-form-field>

            <div *ngIf="error" class="error-message">{{ error }}</div>

            <button mat-raised-button color="primary" type="submit"
                    class="full-width" [disabled]="loading">
              <mat-spinner *ngIf="loading" diameter="20" class="inline-spinner"></mat-spinner>
              {{ loading ? 'Signing in...' : 'Login' }}
            </button>
          </form>
        </mat-card-content>

        <mat-card-actions>
          <p>Don't have an account? <a routerLink="/login/register">Register</a></p>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .auth-container { display: flex; justify-content: center; align-items: center; min-height: calc(100vh - 64px); padding: 16px; }
    .auth-card { max-width: 400px; width: 100%; }
    .full-width { width: 100%; }
    .error-message { color: #f44336; margin-bottom: 16px; text-align: center; }
    .inline-spinner { display: inline-block; margin-right: 8px; }
    mat-card-actions { text-align: center; }
    a { color: #3f51b5; text-decoration: none; }
  `]
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = false;
  error = '';
  hidePassword = true;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) return;

    this.loading = true;
    this.error = '';

    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/menu';
        this.router.navigateByUrl(returnUrl);
      },
      error: err => {
        this.error = err.error?.message || 'Invalid username or password';
        this.loading = false;
      }
    });
  }
}
