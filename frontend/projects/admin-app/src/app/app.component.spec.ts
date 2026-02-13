import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { RouterTestingModule } from '@angular/router/testing';

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatTooltipModule } from '@angular/material/tooltip';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

import { AppComponent } from './app.component';
import { AuthService } from '@shared';

describe('AppComponent (admin-app)', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    mockAuthService = jasmine.createSpyObj('AuthService', [
      'isAuthenticated',
      'getUsername',
      'logout',
      'getToken',
      'getRole'
    ]);
    mockAuthService.isAuthenticated.and.returnValue(true);
    mockAuthService.getUsername.and.returnValue('admin');

    await TestBed.configureTestingModule({
      declarations: [AppComponent],
      imports: [
        RouterTestingModule,
        NoopAnimationsModule,
        MatToolbarModule,
        MatIconModule,
        MatSidenavModule,
        MatListModule,
        MatTooltipModule
      ],
      providers: [
        { provide: AuthService, useValue: mockAuthService }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the app', () => {
    expect(component).toBeTruthy();
  });

  it('should contain a router-outlet', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const routerOutlet = compiled.querySelector('router-outlet');
    expect(routerOutlet).toBeTruthy();
  });

  it('should display "Restaurant Admin" in toolbar', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const toolbar = compiled.querySelector('mat-toolbar');
    expect(toolbar?.textContent).toContain('Restaurant Admin');
  });

  it('should display "Admin Panel" in sidenav', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const brand = compiled.querySelector('.brand');
    expect(brand?.textContent).toContain('Admin Panel');
  });

  it('should display username from AuthService', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const userName = compiled.querySelector('.user-name');
    expect(userName?.textContent).toContain('admin');
  });

  it('should call authService.logout on logout', () => {
    component.logout();
    expect(mockAuthService.logout).toHaveBeenCalled();
  });

  it('should have navigation links in sidenav', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const navLinks = compiled.querySelectorAll('mat-nav-list a[mat-list-item]');
    expect(navLinks.length).toBeGreaterThanOrEqual(1);
  });
});
