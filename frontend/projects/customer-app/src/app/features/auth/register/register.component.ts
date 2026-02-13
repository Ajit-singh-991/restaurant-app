import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '@shared';

@Component({
  selector: 'app-register',
  template: `
    <div class="auth-container">
      <mat-card class="auth-card">
        <mat-card-header>
          <mat-card-title>Register</mat-card-title>
          <mat-card-subtitle>Create a new account</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="registerForm" (ngSubmit)="onSubmit()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Full Name</mat-label>
              <input matInput formControlName="fullName">
              <mat-icon matPrefix>person</mat-icon>
              <mat-error *ngIf="registerForm.get('fullName')?.hasError('required')">Name is required</mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Username</mat-label>
              <input matInput formControlName="username">
              <mat-icon matPrefix>account_circle</mat-icon>
              <mat-error *ngIf="registerForm.get('username')?.hasError('required')">Username is required</mat-error>
              <mat-error *ngIf="registerForm.get('username')?.hasError('minlength')">Min 3 characters</mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Email</mat-label>
              <input matInput formControlName="email" type="email">
              <mat-icon matPrefix>email</mat-icon>
              <mat-error *ngIf="registerForm.get('email')?.hasError('email')">Invalid email format</mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Phone</mat-label>
              <input matInput formControlName="phone">
              <mat-icon matPrefix>phone</mat-icon>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Password</mat-label>
              <input matInput [type]="hidePassword ? 'password' : 'text'" formControlName="password">
              <mat-icon matPrefix>lock</mat-icon>
              <button mat-icon-button matSuffix type="button" (click)="hidePassword = !hidePassword">
                <mat-icon>{{ hidePassword ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              <mat-error *ngIf="registerForm.get('password')?.hasError('required')">Password is required</mat-error>
              <mat-error *ngIf="registerForm.get('password')?.hasError('minlength')">Min 6 characters</mat-error>
              <mat-hint *ngIf="registerForm.get('password')?.value">
                Strength: {{ getPasswordStrength() }}
              </mat-hint>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Confirm Password</mat-label>
              <input matInput [type]="hidePassword ? 'password' : 'text'" formControlName="confirmPassword">
              <mat-icon matPrefix>lock_outline</mat-icon>
              <mat-error *ngIf="registerForm.get('confirmPassword')?.hasError('required')">Please confirm password</mat-error>
              <mat-error *ngIf="registerForm.get('confirmPassword')?.hasError('passwordMismatch')">Passwords do not match</mat-error>
            </mat-form-field>

            <div *ngIf="error" class="error-message">{{ error }}</div>

            <button mat-raised-button color="primary" type="submit"
                    class="full-width submit-btn" [disabled]="loading || registerForm.invalid">
              <mat-spinner *ngIf="loading" diameter="20" class="inline-spinner"></mat-spinner>
              {{ loading ? 'Creating account...' : 'Create Account' }}
            </button>
          </form>
        </mat-card-content>

        <mat-card-actions>
          <p>Already have an account? <a routerLink="/login">Login</a></p>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .auth-container { display: flex; justify-content: center; align-items: center; min-height: calc(100vh - 64px); padding: 16px; }
    .auth-card { max-width: 440px; width: 100%; }
    .full-width { width: 100%; }
    .error-message { color: #f44336; margin-bottom: 16px; text-align: center; }
    .inline-spinner { display: inline-block; margin-right: 8px; }
    .submit-btn { height: 48px; font-size: 16px; }
    a { color: #3f51b5; text-decoration: none; }
    mat-card-actions { text-align: center; }
  `]
})
export class RegisterComponent {
  registerForm: FormGroup;
  loading = false;
  error = '';
  hidePassword = true;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      fullName: ['', Validators.required],
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', Validators.email],
      phone: [''],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required, this.matchPassword.bind(this)]]
    });

    // Re-validate confirmPassword when password changes
    this.registerForm.get('password')?.valueChanges.subscribe(() => {
      this.registerForm.get('confirmPassword')?.updateValueAndValidity();
    });
  }

  private matchPassword(control: AbstractControl): ValidationErrors | null {
    if (!this.registerForm) return null;
    const password = this.registerForm.get('password')?.value;
    return control.value !== password ? { passwordMismatch: true } : null;
  }

  getPasswordStrength(): string {
    const password = this.registerForm.get('password')?.value || '';
    if (password.length < 6) return 'Too short';
    let score = 0;
    if (password.length >= 8) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/[0-9]/.test(password)) score++;
    if (/[^A-Za-z0-9]/.test(password)) score++;
    if (score <= 1) return 'Weak';
    if (score === 2) return 'Fair';
    if (score === 3) return 'Good';
    return 'Strong';
  }

  onSubmit(): void {
    if (this.registerForm.invalid) return;
    this.loading = true;
    this.error = '';

    const { confirmPassword, ...registerData } = this.registerForm.value;
    this.authService.register(registerData).subscribe({
      next: () => this.router.navigate(['/menu']),
      error: err => {
        this.error = err.error?.message || 'Registration failed. Please try again.';
        this.loading = false;
      }
    });
  }
}
