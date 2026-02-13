import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { AuthService, CartService } from '@shared';

@Component({
  selector: 'app-root',
  template: `
    <mat-toolbar color="primary">
      <button mat-icon-button routerLink="/menu">
        <mat-icon>restaurant</mat-icon>
      </button>
      <span routerLink="/menu" style="cursor: pointer">Restaurant</span>
      <span class="spacer"></span>

      <app-language-selector></app-language-selector>

      <button mat-icon-button routerLink="/cart">
        <mat-icon [matBadge]="cartCount" matBadgeColor="accent"
                  [matBadgeHidden]="cartCount === 0">
          shopping_cart
        </mat-icon>
      </button>

      <button mat-icon-button [matMenuTriggerFor]="userMenu">
        <mat-icon>person</mat-icon>
      </button>

      <mat-menu #userMenu="matMenu">
        <ng-container *ngIf="authService.isAuthenticated(); else loginBtn">
          <div mat-menu-item disabled class="user-info">
            <mat-icon>account_circle</mat-icon>
            <span>{{ authService.getUsername() }}</span>
          </div>
          <mat-divider></mat-divider>
          <button mat-menu-item routerLink="/login/profile">
            <mat-icon>person</mat-icon>
            <span>My Profile</span>
          </button>
          <button mat-menu-item routerLink="/orders">
            <mat-icon>receipt</mat-icon>
            <span>My Orders</span>
          </button>
          <mat-divider></mat-divider>
          <button mat-menu-item (click)="logout()">
            <mat-icon>logout</mat-icon>
            <span>Logout</span>
          </button>
        </ng-container>
        <ng-template #loginBtn>
          <button mat-menu-item routerLink="/login">
            <mat-icon>login</mat-icon>
            <span>Login</span>
          </button>
          <button mat-menu-item routerLink="/login/register">
            <mat-icon>person_add</mat-icon>
            <span>Register</span>
          </button>
        </ng-template>
      </mat-menu>
    </mat-toolbar>

    <router-outlet></router-outlet>

    <button class="cart-fab" mat-fab color="accent" routerLink="/cart"
            [matBadge]="cartCount" matBadgeColor="warn" [matBadgeHidden]="cartCount === 0"
            aria-label="Cart">
      <mat-icon>shopping_cart</mat-icon>
    </button>
  `,
  styles: [`
    .spacer { flex: 1 1 auto; }
    mat-toolbar { position: sticky; top: 0; z-index: 1000; }
    .cart-fab {
      position: fixed;
      bottom: 24px;
      right: 24px;
      z-index: 1000;
    }
  `]
})
export class AppComponent implements OnInit, OnDestroy {
  cartCount = 0;
  private destroy$ = new Subject<void>();

  constructor(
    public authService: AuthService,
    private cartService: CartService,
    private router: Router,
    private translate: TranslateService
  ) {
    translate.addLangs(['en', 'hi', 'es']);
    translate.setDefaultLang('en');
    const savedLang = localStorage.getItem('preferred-language') || 'en';
    translate.use(savedLang);
  }

  ngOnInit(): void {
    this.cartService.itemCount$.pipe(takeUntil(this.destroy$))
      .subscribe(count => this.cartCount = count);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/menu']);
  }
}
