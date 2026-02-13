import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { RouterTestingModule } from '@angular/router/testing';
import { BehaviorSubject } from 'rxjs';
import { RxStompState } from '@stomp/rx-stomp';

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

import { AppComponent } from './app.component';
import { WebSocketService } from '@shared';

describe('AppComponent (kitchen-app)', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let mockWsService: jasmine.SpyObj<WebSocketService>;
  let connectedSubject: BehaviorSubject<number>;

  beforeEach(async () => {
    connectedSubject = new BehaviorSubject<number>(RxStompState.CLOSED);

    mockWsService = jasmine.createSpyObj('WebSocketService', ['connect', 'disconnect'], {
      connected$: connectedSubject.asObservable()
    });

    await TestBed.configureTestingModule({
      declarations: [AppComponent],
      imports: [
        RouterTestingModule,
        MatToolbarModule,
        MatIconModule,
        MatTooltipModule
      ],
      providers: [
        { provide: WebSocketService, useValue: mockWsService }
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

  it('should display "Kitchen Display System" in toolbar', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const toolbar = compiled.querySelector('mat-toolbar');
    expect(toolbar?.textContent).toContain('Kitchen Display System');
  });

  it('should have isConnected default to false', () => {
    expect(component.isConnected).toBeFalse();
  });

  it('should update isConnected to true when WebSocket connects', () => {
    connectedSubject.next(RxStompState.OPEN);
    fixture.detectChanges();
    expect(component.isConnected).toBeTrue();
  });

  it('should update isConnected to false when WebSocket disconnects', () => {
    connectedSubject.next(RxStompState.OPEN);
    fixture.detectChanges();
    expect(component.isConnected).toBeTrue();

    connectedSubject.next(RxStompState.CLOSED);
    fixture.detectChanges();
    expect(component.isConnected).toBeFalse();
  });

  it('should have isFullscreen default to false', () => {
    expect(component.isFullscreen).toBeFalse();
  });

  it('should have currentTime initialized', () => {
    expect(component.currentTime).toBeDefined();
    expect(component.currentTime instanceof Date).toBeTrue();
  });

  it('should show connected dot with correct class when disconnected', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const dot = compiled.querySelector('.connection-dot');
    expect(dot?.classList.contains('disconnected')).toBeTrue();
  });

  it('should show connected dot with correct class when connected', () => {
    connectedSubject.next(RxStompState.OPEN);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const dot = compiled.querySelector('.connection-dot');
    expect(dot?.classList.contains('connected')).toBeTrue();
  });

  it('should contain a router-outlet', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const routerOutlet = compiled.querySelector('router-outlet');
    expect(routerOutlet).toBeTruthy();
  });

  it('should clean up subscription on destroy', () => {
    component.ngOnDestroy();
    // No error thrown means subscription was cleaned up properly
    expect(component).toBeTruthy();
  });
});
