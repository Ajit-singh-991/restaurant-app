import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { WebSocketService } from '@shared';
import { RxStompState } from '@stomp/rx-stomp';

@Component({
  selector: 'app-root',
  template: `
    <mat-toolbar color="warn">
      <mat-icon>restaurant</mat-icon>
      <span style="margin-left: 8px">Kitchen Display System</span>
      <span style="flex: 1 1 auto"></span>
      <span class="connection-dot" [class.connected]="isConnected" [class.disconnected]="!isConnected"
            [matTooltip]="isConnected ? 'Connected' : 'Disconnected'"></span>
      <span style="margin-right: 16px">{{ currentTime | date:'HH:mm:ss' }}</span>
      <button mat-icon-button (click)="toggleFullscreen()" [matTooltip]="isFullscreen ? 'Exit Fullscreen' : 'Fullscreen'">
        <mat-icon>{{ isFullscreen ? 'fullscreen_exit' : 'fullscreen' }}</mat-icon>
      </button>
    </mat-toolbar>
    <router-outlet></router-outlet>
  `,
  styles: [`
    mat-toolbar { position: sticky; top: 0; z-index: 1000; }
    .connection-dot {
      display: inline-block; width: 10px; height: 10px; border-radius: 50%;
      margin-right: 8px;
    }
    .connection-dot.connected { background: #4caf50; }
    .connection-dot.disconnected { background: #f44336; }
  `]
})
export class AppComponent implements OnInit, OnDestroy {
  currentTime = new Date();
  isFullscreen = false;
  isConnected = false;
  private sub?: Subscription;

  constructor(private wsService: WebSocketService) {
    setInterval(() => this.currentTime = new Date(), 1000);
  }

  ngOnInit(): void {
    this.sub = this.wsService.connected$.subscribe(state => {
      this.isConnected = state === RxStompState.OPEN;
    });

    document.addEventListener('fullscreenchange', () => {
      this.isFullscreen = !!document.fullscreenElement;
    });
  }

  toggleFullscreen(): void {
    if (!document.fullscreenElement) {
      document.documentElement.requestFullscreen();
    } else {
      document.exitFullscreen();
    }
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }
}
