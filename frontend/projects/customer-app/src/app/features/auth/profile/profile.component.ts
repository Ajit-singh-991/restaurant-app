import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService, UserProfile } from '@shared';

@Component({
  selector: 'app-profile',
  template: `
    <div class="profile-container">
      <h2>My Profile</h2>

      <mat-card class="profile-card" *ngIf="profile">
        <mat-card-header>
          <mat-icon mat-card-avatar style="font-size:40px;width:40px;height:40px">account_circle</mat-icon>
          <mat-card-title>{{ profile.fullName }}</mat-card-title>
          <mat-card-subtitle>{{ profile.role }} | Member since {{ profile.createdAt | date:'mediumDate' }}</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="profileForm" (ngSubmit)="updateProfile()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Full Name</mat-label>
              <input matInput formControlName="fullName">
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Email</mat-label>
              <input matInput formControlName="email" type="email">
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Phone</mat-label>
              <input matInput formControlName="phone">
            </mat-form-field>

            <button mat-raised-button color="primary" type="submit"
                    [disabled]="profileForm.pristine || profileForm.invalid || savingProfile">
              {{ savingProfile ? 'Saving...' : 'Update Profile' }}
            </button>
          </form>
        </mat-card-content>
      </mat-card>

      <mat-card class="password-card">
        <mat-card-header>
          <mat-card-title>Change Password</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <form [formGroup]="passwordForm" (ngSubmit)="changePassword()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Current Password</mat-label>
              <input matInput type="password" formControlName="currentPassword">
              <mat-error>Required</mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>New Password</mat-label>
              <input matInput type="password" formControlName="newPassword">
              <mat-error *ngIf="passwordForm.get('newPassword')?.hasError('minlength')">Min 6 characters</mat-error>
            </mat-form-field>

            <button mat-raised-button color="warn" type="submit"
                    [disabled]="passwordForm.invalid || savingPassword">
              {{ savingPassword ? 'Changing...' : 'Change Password' }}
            </button>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .profile-container { max-width: 600px; margin: 24px auto; padding: 0 16px; }
    .profile-card, .password-card { margin-bottom: 24px; }
    .full-width { width: 100%; }
    button { margin-top: 8px; }
  `]
})
export class ProfileComponent implements OnInit {
  profile: UserProfile | null = null;
  profileForm: FormGroup;
  passwordForm: FormGroup;
  savingProfile = false;
  savingPassword = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {
    this.profileForm = this.fb.group({
      fullName: ['', Validators.required],
      email: ['', Validators.email],
      phone: ['']
    });
    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit(): void {
    this.authService.getCurrentUser().subscribe(profile => {
      this.profile = profile;
      this.profileForm.patchValue({
        fullName: profile.fullName,
        email: profile.email,
        phone: profile.phone
      });
    });
  }

  updateProfile(): void {
    this.savingProfile = true;
    this.authService.updateProfile(this.profileForm.value).subscribe({
      next: (updated) => {
        this.profile = updated;
        this.profileForm.markAsPristine();
        this.savingProfile = false;
        this.snackBar.open('Profile updated', 'OK', { duration: 3000 });
      },
      error: (err) => {
        this.savingProfile = false;
        this.snackBar.open(err.error?.message || 'Update failed', 'OK', { duration: 3000 });
      }
    });
  }

  changePassword(): void {
    this.savingPassword = true;
    this.authService.changePassword(this.passwordForm.value).subscribe({
      next: () => {
        this.passwordForm.reset();
        this.savingPassword = false;
        this.snackBar.open('Password changed successfully', 'OK', { duration: 3000 });
      },
      error: (err) => {
        this.savingPassword = false;
        this.snackBar.open(err.error?.message || 'Password change failed', 'OK', { duration: 3000 });
      }
    });
  }
}
