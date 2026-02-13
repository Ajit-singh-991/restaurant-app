import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, map } from 'rxjs';
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  UserProfile,
  ChangePasswordRequest,
  UpdateProfileRequest
} from '../models/auth.model';
import { environment } from '@environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private currentUserSubject = new BehaviorSubject<AuthResponse | null>(this.getStoredUser());
  public currentUser$ = this.currentUserSubject.asObservable();

  /** Emits true/false whenever auth state changes */
  public isAuthenticated$ = this.currentUser$.pipe(map(user => !!user));

  constructor(private http: HttpClient) {}

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(response => this.storeAuth(response))
    );
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, request).pipe(
      tap(response => this.storeAuth(response))
    );
  }

  logout(): void {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_user');
    this.currentUserSubject.next(null);
  }

  /** GET /auth/me - fetch full user profile from server */
  getCurrentUser(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.apiUrl}/me`);
  }

  /** POST /auth/refresh - get a new token */
  refreshToken(): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/refresh`, {}).pipe(
      tap(response => this.storeAuth(response))
    );
  }

  /** PUT /auth/me - update profile fields */
  updateProfile(request: UpdateProfileRequest): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.apiUrl}/me`, request);
  }

  /** POST /auth/change-password */
  changePassword(request: ChangePasswordRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/change-password`, request);
  }

  getToken(): string | null {
    return localStorage.getItem('auth_token');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getRole(): string | null {
    const user = this.getStoredUser();
    return user ? user.role : null;
  }

  getUserId(): number | null {
    const user = this.getStoredUser();
    return user ? user.userId : null;
  }

  getUsername(): string | null {
    const user = this.getStoredUser();
    return user ? user.username : null;
  }

  private storeAuth(response: AuthResponse): void {
    localStorage.setItem('auth_token', response.token);
    localStorage.setItem('auth_user', JSON.stringify(response));
    this.currentUserSubject.next(response);
  }

  private getStoredUser(): AuthResponse | null {
    const stored = localStorage.getItem('auth_user');
    if (!stored) return null;
    try {
      return JSON.parse(stored);
    } catch {
      localStorage.removeItem('auth_user');
      return null;
    }
  }
}
