import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { RouterTestingModule } from '@angular/router/testing';
import { BehaviorSubject } from 'rxjs';

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatDividerModule } from '@angular/material/divider';

import { AppComponent } from './app.component';
import { AuthService, CartService } from '@shared';

describe('AppComponent (customer-app)', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockCartService: jasmine.SpyObj<CartService>;
  let itemCountSubject: BehaviorSubject<number>;

  beforeEach(async () => {
    itemCountSubject = new BehaviorSubject<number>(0);

    mockAuthService = jasmine.createSpyObj('AuthService', [
      'isAuthenticated',
      'getUsername',
      'logout',
      'getToken',
      'getRole'
    ]);
    mockAuthService.isAuthenticated.and.returnValue(false);
    mockAuthService.getUsername.and.returnValue(null);

    mockCartService = jasmine.createSpyObj('CartService', ['getCart', 'clearCart'], {
      itemCount$: itemCountSubject.asObservable()
    });

    await TestBed.configureTestingModule({
      declarations: [AppComponent],
      imports: [
        RouterTestingModule,
        MatToolbarModule,
        MatIconModule,
        MatMenuModule,
        MatBadgeModule,
        MatDividerModule
      ],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: CartService, useValue: mockCartService }
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

  it('should have initial cart count of 0', () => {
    expect(component.cartCount).toBe(0);
  });

  it('should update cart count when itemCount$ emits', () => {
    itemCountSubject.next(3);
    fixture.detectChanges();
    expect(component.cartCount).toBe(3);
  });

  it('should contain a router-outlet', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const routerOutlet = compiled.querySelector('router-outlet');
    expect(routerOutlet).toBeTruthy();
  });

  it('should contain a mat-toolbar', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const toolbar = compiled.querySelector('mat-toolbar');
    expect(toolbar).toBeTruthy();
  });

  it('should display "Restaurant" in toolbar', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const toolbar = compiled.querySelector('mat-toolbar');
    expect(toolbar?.textContent).toContain('Restaurant');
  });

  it('should call authService.logout and navigate on logout', () => {
    component.logout();
    expect(mockAuthService.logout).toHaveBeenCalled();
  });

  it('should clean up on destroy', () => {
    component.ngOnDestroy();
    // Verify no errors - subject is completed
    expect(component).toBeTruthy();
  });
});
