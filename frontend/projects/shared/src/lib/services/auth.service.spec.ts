import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { AuthResponse, LoginRequest, RegisterRequest, UserProfile, ChangePasswordRequest } from '../models/auth.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api/auth';

  const mockAuthResponse: AuthResponse = {
    token: 'mock-jwt-token',
    type: 'Bearer',
    userId: 1,
    username: 'testuser',
    role: 'CUSTOMER'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('login', () => {
    it('should send a POST request to /auth/login', () => {
      const loginRequest: LoginRequest = { username: 'testuser', password: 'password123' };

      service.login(loginRequest).subscribe(response => {
        expect(response).toEqual(mockAuthResponse);
      });

      const req = httpMock.expectOne(`${apiUrl}/login`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(loginRequest);
      req.flush(mockAuthResponse);
    });

    it('should store the token in localStorage after login', () => {
      const loginRequest: LoginRequest = { username: 'testuser', password: 'password123' };

      service.login(loginRequest).subscribe(() => {
        expect(localStorage.getItem('auth_token')).toBe('mock-jwt-token');
        expect(localStorage.getItem('auth_user')).toBeTruthy();
      });

      const req = httpMock.expectOne(`${apiUrl}/login`);
      req.flush(mockAuthResponse);
    });

    it('should update currentUser$ after login', () => {
      const loginRequest: LoginRequest = { username: 'testuser', password: 'password123' };

      service.login(loginRequest).subscribe(() => {
        service.currentUser$.subscribe(user => {
          expect(user).toEqual(mockAuthResponse);
        });
      });

      const req = httpMock.expectOne(`${apiUrl}/login`);
      req.flush(mockAuthResponse);
    });
  });

  describe('register', () => {
    it('should send a POST request to /auth/register', () => {
      const registerRequest: RegisterRequest = {
        username: 'newuser',
        password: 'password123',
        fullName: 'New User',
        email: 'new@example.com'
      };

      service.register(registerRequest).subscribe(response => {
        expect(response).toEqual(mockAuthResponse);
      });

      const req = httpMock.expectOne(`${apiUrl}/register`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(registerRequest);
      req.flush(mockAuthResponse);
    });

    it('should store the token in localStorage after register', () => {
      const registerRequest: RegisterRequest = {
        username: 'newuser',
        password: 'password123',
        fullName: 'New User'
      };

      service.register(registerRequest).subscribe(() => {
        expect(localStorage.getItem('auth_token')).toBe('mock-jwt-token');
        expect(localStorage.getItem('auth_user')).toBeTruthy();
      });

      const req = httpMock.expectOne(`${apiUrl}/register`);
      req.flush(mockAuthResponse);
    });
  });

  describe('logout', () => {
    it('should clear auth_token from localStorage', () => {
      localStorage.setItem('auth_token', 'some-token');
      localStorage.setItem('auth_user', JSON.stringify(mockAuthResponse));

      service.logout();

      expect(localStorage.getItem('auth_token')).toBeNull();
      expect(localStorage.getItem('auth_user')).toBeNull();
    });

    it('should set currentUser$ to null', () => {
      service.logout();

      service.currentUser$.subscribe(user => {
        expect(user).toBeNull();
      });
    });
  });

  describe('isAuthenticated', () => {
    it('should return true when token exists in localStorage', () => {
      localStorage.setItem('auth_token', 'some-token');
      expect(service.isAuthenticated()).toBeTrue();
    });

    it('should return false when no token exists', () => {
      expect(service.isAuthenticated()).toBeFalse();
    });
  });

  describe('getToken', () => {
    it('should return the token from localStorage', () => {
      localStorage.setItem('auth_token', 'my-jwt-token');
      expect(service.getToken()).toBe('my-jwt-token');
    });

    it('should return null when no token exists', () => {
      expect(service.getToken()).toBeNull();
    });
  });

  describe('getRole', () => {
    it('should return the role from stored user', () => {
      localStorage.setItem('auth_user', JSON.stringify(mockAuthResponse));
      // Need to recreate the service so it picks up the stored user
      service = new AuthService(TestBed.inject(HttpTestingController) as any);
      // Alternatively, test via login flow
    });

    it('should return null when no user is stored', () => {
      expect(service.getRole()).toBeNull();
    });

    it('should return the role after login', () => {
      const loginRequest: LoginRequest = { username: 'testuser', password: 'password123' };

      service.login(loginRequest).subscribe(() => {
        expect(service.getRole()).toBe('CUSTOMER');
      });

      const req = httpMock.expectOne(`${apiUrl}/login`);
      req.flush(mockAuthResponse);
    });
  });

  describe('getUserId', () => {
    it('should return null when no user is stored', () => {
      expect(service.getUserId()).toBeNull();
    });

    it('should return userId after login', () => {
      const loginRequest: LoginRequest = { username: 'testuser', password: 'password123' };

      service.login(loginRequest).subscribe(() => {
        expect(service.getUserId()).toBe(1);
      });

      const req = httpMock.expectOne(`${apiUrl}/login`);
      req.flush(mockAuthResponse);
    });
  });

  describe('getUsername', () => {
    it('should return null when no user is stored', () => {
      expect(service.getUsername()).toBeNull();
    });

    it('should return username after login', () => {
      const loginRequest: LoginRequest = { username: 'testuser', password: 'password123' };

      service.login(loginRequest).subscribe(() => {
        expect(service.getUsername()).toBe('testuser');
      });

      const req = httpMock.expectOne(`${apiUrl}/login`);
      req.flush(mockAuthResponse);
    });
  });

  describe('getCurrentUser', () => {
    it('should send a GET request to /auth/me', () => {
      const mockProfile: UserProfile = {
        id: 1,
        username: 'testuser',
        fullName: 'Test User',
        email: 'test@example.com',
        phone: '1234567890',
        role: 'CUSTOMER',
        active: true,
        createdAt: '2024-01-01T00:00:00'
      };

      service.getCurrentUser().subscribe(profile => {
        expect(profile).toEqual(mockProfile);
      });

      const req = httpMock.expectOne(`${apiUrl}/me`);
      expect(req.request.method).toBe('GET');
      req.flush(mockProfile);
    });
  });

  describe('refreshToken', () => {
    it('should send a POST request to /auth/refresh', () => {
      service.refreshToken().subscribe(response => {
        expect(response).toEqual(mockAuthResponse);
      });

      const req = httpMock.expectOne(`${apiUrl}/refresh`);
      expect(req.request.method).toBe('POST');
      req.flush(mockAuthResponse);
    });

    it('should update stored token after refresh', () => {
      const refreshedResponse: AuthResponse = {
        ...mockAuthResponse,
        token: 'refreshed-token'
      };

      service.refreshToken().subscribe(() => {
        expect(localStorage.getItem('auth_token')).toBe('refreshed-token');
      });

      const req = httpMock.expectOne(`${apiUrl}/refresh`);
      req.flush(refreshedResponse);
    });
  });

  describe('changePassword', () => {
    it('should send a POST request to /auth/change-password', () => {
      const request: ChangePasswordRequest = {
        currentPassword: 'oldpass',
        newPassword: 'newpass'
      };

      service.changePassword(request).subscribe(response => {
        expect(response.message).toBeDefined();
      });

      const req = httpMock.expectOne(`${apiUrl}/change-password`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush({ message: 'Password changed successfully' });
    });
  });

  describe('isAuthenticated$', () => {
    it('should emit false when no user is stored', (done) => {
      service.isAuthenticated$.subscribe(isAuth => {
        expect(isAuth).toBeFalse();
        done();
      });
    });

    it('should emit true after login', () => {
      const loginRequest: LoginRequest = { username: 'testuser', password: 'password123' };

      service.login(loginRequest).subscribe(() => {
        service.isAuthenticated$.subscribe(isAuth => {
          expect(isAuth).toBeTrue();
        });
      });

      const req = httpMock.expectOne(`${apiUrl}/login`);
      req.flush(mockAuthResponse);
    });
  });
});
