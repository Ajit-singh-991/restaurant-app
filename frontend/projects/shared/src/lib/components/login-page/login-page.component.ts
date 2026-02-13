import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'lib-login-page',
  template: `
    <div class="login-container">
      <mat-card class="login-card">
        <mat-card-header>
          <mat-icon mat-card-avatar class="header-icon">{{ icon }}</mat-icon>
          <mat-card-title>{{ title }}</mat-card-title>
          <mat-card-subtitle>{{ subtitle }}</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Username</mat-label>
              <mat-icon matPrefix>person</mat-icon>
              <input matInput formControlName="username" placeholder="Enter username"
                     autocomplete="username">
              <mat-error *ngIf="loginForm.get('username')?.hasError('required')">
                Username is required
              </mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Password</mat-label>
              <mat-icon matPrefix>lock</mat-icon>
              <input matInput [type]="hidePassword ? 'password' : 'text'"
                     formControlName="password"
                     autocomplete="current-password">
              <button mat-icon-button matSuffix type="button" (click)="hidePassword = !hidePassword">
                <mat-icon>{{ hidePassword ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              <mat-error *ngIf="loginForm.get('password')?.hasError('required')">
                Password is required
              </mat-error>
            </mat-form-field>

            <div *ngIf="error" class="error-message">
              <mat-icon inline>error</mat-icon> {{ error }}
            </div>

            <button mat-raised-button color="primary" type="submit"
                    class="full-width submit-btn" [disabled]="loading || loginForm.invalid">
              <mat-spinner *ngIf="loading" diameter="20" class="inline-spinner"></mat-spinner>
              {{ loading ? 'Signing in...' : 'Sign In' }}
            </button>
          </form>
        </mat-card-content>

        <mat-card-footer *ngIf="showHint">
          <div class="hint">
            <small>Hint: Use your staff credentials to sign in</small>
          </div>
        </mat-card-footer>
      </mat-card>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex; justify-content: center; align-items: center;
      min-height: 100vh; padding: 16px; background: #f5f5f5;
    }
    .login-card { max-width: 420px; width: 100%; padding-bottom: 16px; }
    .header-icon { font-size: 40px !important; width: 40px !important; height: 40px !important; }
    .full-width { width: 100%; }
    .error-message {
      color: #f44336; margin-bottom: 16px; padding: 8px 12px;
      background: #ffebee; border-radius: 4px; font-size: 14px;
      display: flex; align-items: center; gap: 8px;
    }
    .inline-spinner { display: inline-block; margin-right: 8px; }
    .submit-btn { height: 48px; font-size: 16px; }
    .hint { text-align: center; padding: 12px; color: #888; }
  `]
})
export class LoginPageComponent {
  @Input() title = 'Sign In';
  @Input() subtitle = 'Enter your credentials';
  @Input() icon = 'login';
  @Input() showHint = false;
  @Input() error = '';
  @Input() loading = false;
  @Output() loginSubmit = new EventEmitter<{ username: string; password: string }>();

  loginForm: FormGroup;
  hidePassword = true;

  constructor(private fb: FormBuilder) {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.loginSubmit.emit(this.loginForm.value);
    }
  }
}
