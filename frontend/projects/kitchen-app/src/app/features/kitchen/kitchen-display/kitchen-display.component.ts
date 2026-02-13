import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription, interval } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';
import { KitchenService, KitchenStats, WebSocketService, Order } from '@shared';
import type { KitchenStation } from '@shared';

@Component({
  selector: 'app-kitchen-display',
  template: `
    <div class="kitchen-container">
      <div class="toolbar">
        <mat-form-field appearance="outline" class="station-select">
          <mat-label>Station</mat-label>
          <mat-select [(value)]="selectedStationId" (selectionChange)="onStationChange()">
            <mat-option [value]="null">All Stations</mat-option>
            <mat-option *ngFor="let s of stations" [value]="s.id">{{ s.name }}</mat-option>
          </mat-select>
        </mat-form-field>
      </div>
      <div class="stats-bar">
        <div class="stat">
          <span class="stat-value">{{ stats.activeOrders }}</span>
          <span class="stat-label">Active</span>
        </div>
        <div class="stat">
          <span class="stat-value">{{ stats.preparingOrders }}</span>
          <span class="stat-label">Preparing</span>
        </div>
        <div class="stat">
          <span class="stat-value">{{ stats.readyOrders }}</span>
          <span class="stat-label">Ready</span>
        </div>
        <div class="stat">
          <span class="stat-value">{{ stats.avgPrepTimeMinutes | number:'1.0-1' }} min</span>
          <span class="stat-label">Avg Prep</span>
        </div>
      </div>

      <div class="kanban-board" cdkDropListGroup>
        <div class="kanban-column" cdkDropList id="new" [cdkDropListData]="newOrdersList"
             [cdkDropListConnectedTo]="['preparing']" (cdkDropListDropped)="onDrop($event)">
          <h3 class="column-header new">
            <mat-icon>fiber_new</mat-icon> New Orders ({{ newOrdersList.length }})
          </h3>
          <app-order-card *ngFor="let order of newOrdersList" cdkDrag [cdkDragData]="order"
                          [order]="order"
                          (statusChange)="onStatusChange($event)"
                          (itemComplete)="onItemComplete($event)">
          </app-order-card>
        </div>

        <div class="kanban-column" cdkDropList id="preparing" [cdkDropListData]="preparingOrdersList"
             [cdkDropListConnectedTo]="['new', 'ready']" (cdkDropListDropped)="onDrop($event)">
          <h3 class="column-header preparing">
            <mat-icon>outdoor_grill</mat-icon> In Progress ({{ preparingOrdersList.length }})
          </h3>
          <app-order-card *ngFor="let order of preparingOrdersList" cdkDrag [cdkDragData]="order"
                          [order]="order"
                          (statusChange)="onStatusChange($event)"
                          (itemComplete)="onItemComplete($event)">
          </app-order-card>
        </div>

        <div class="kanban-column" cdkDropList id="ready" [cdkDropListData]="readyOrdersList"
             [cdkDropListConnectedTo]="['preparing']" (cdkDropListDropped)="onDrop($event)">
          <h3 class="column-header ready">
            <mat-icon>check_circle</mat-icon> Ready ({{ readyOrdersList.length }})
          </h3>
          <app-order-card *ngFor="let order of readyOrdersList" cdkDrag [cdkDragData]="order"
                          [order]="order"
                          (statusChange)="onStatusChange($event)"
                          (itemComplete)="onItemComplete($event)">
          </app-order-card>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .kitchen-container { padding: 16px; height: calc(100vh - 64px); display: flex; flex-direction: column; }
    .stats-bar {
      display: flex; gap: 24px; justify-content: center; padding: 12px;
      background: rgba(255,255,255,0.05); border-radius: 8px; margin-bottom: 16px;
    }
    .stat { text-align: center; }
    .stat-value { display: block; font-size: 24px; font-weight: bold; color: #fff; }
    .stat-label { font-size: 12px; color: #aaa; text-transform: uppercase; }
    .toolbar { margin-bottom: 12px; }
    .station-select { min-width: 200px; }
    .station-select .mat-mdc-form-field-subscript-wrapper { display: none; }
    .kanban-board { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; flex: 1; overflow: hidden; }
    .kanban-column { background: rgba(255,255,255,0.05); border-radius: 8px; padding: 12px; overflow-y: auto; }
    .column-header { display: flex; align-items: center; gap: 8px; margin: 0 0 12px; padding-bottom: 8px; border-bottom: 2px solid; }
    .column-header.new { border-color: #ff9800; color: #ff9800; }
    .column-header.preparing { border-color: #2196f3; color: #2196f3; }
    .column-header.ready { border-color: #4caf50; color: #4caf50; }
    .cdk-drag-preview { opacity: 0.9; box-shadow: 0 4px 12px rgba(0,0,0,0.3); }
    .cdk-drag-placeholder { opacity: 0.3; }
  `]
})
export class KitchenDisplayComponent implements OnInit, OnDestroy {
  allOrders: Order[] = [];
  newOrdersList: Order[] = [];
  preparingOrdersList: Order[] = [];
  readyOrdersList: Order[] = [];
  stations: KitchenStation[] = [];
  selectedStationId: number | null = null;
  stats: KitchenStats = { activeOrders: 0, preparingOrders: 0, readyOrders: 0, avgPrepTimeMinutes: 0 };
  private sub?: Subscription;
  private newOrderAudio: HTMLAudioElement;

  constructor(
    private kitchenService: KitchenService,
    private wsService: WebSocketService,
    private snackBar: MatSnackBar
  ) {
    // Use a simple beep via AudioContext as the alert sound
    this.newOrderAudio = new Audio('data:audio/wav;base64,UklGRl9vT19teleWF2ZWZtdCAQAAAAABAAEARB8AAEQfAAABAAgAZGF0YQ==');
  }

  ngOnInit(): void {
    this.kitchenService.getStations().subscribe(s => this.stations = s);
    this.loadOrders();
    this.loadStats();
    this.wsService.connect();

    this.sub = this.wsService.subscribeToKitchen().subscribe(notification => {
      this.loadOrders();
      this.loadStats();
      if (notification?.type === 'ORDER_CREATED') {
        this.playNewOrderAlert(notification.message || 'New order received!');
      }
    });

    // Fallback: poll every 30 seconds
    this.sub.add(
      interval(30000).pipe(
        switchMap(() => this.kitchenService.getActiveOrders())
      ).subscribe(orders => {
        this.allOrders = orders;
        this.newOrdersList = orders.filter(o => o.status === 'PENDING' || o.status === 'CONFIRMED');
        this.preparingOrdersList = orders.filter(o => o.status === 'PREPARING');
        this.readyOrdersList = orders.filter(o => o.status === 'READY');
        this.loadStats();
      })
    );
  }

  onStationChange(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    const $ = this.selectedStationId != null
      ? this.kitchenService.getActiveOrdersByStation(this.selectedStationId)
      : this.kitchenService.getActiveOrders();
    $.subscribe(orders => {
      this.allOrders = orders;
      this.newOrdersList = orders.filter(o => o.status === 'PENDING' || o.status === 'CONFIRMED');
      this.preparingOrdersList = orders.filter(o => o.status === 'PREPARING');
      this.readyOrdersList = orders.filter(o => o.status === 'READY');
    });
  }

  onDrop(event: { previousContainer: { id: string; data: Order[] }; container: { id: string }; item: { data: Order } }): void {
    const order = event.item.data;
    const from = event.previousContainer.id;
    const to = event.container.id;
    if (from === 'new' && to === 'preparing') {
      this.kitchenService.startPreparing(order.id).subscribe(() => this.loadOrders());
    } else if (from === 'preparing' && to === 'ready') {
      this.kitchenService.markReady(order.id).subscribe(() => this.loadOrders());
    }
  }

  loadStats(): void {
    this.kitchenService.getStats().subscribe(stats => this.stats = stats);
  }

  onStatusChange(event: { orderId: number; status: string }): void {
    const action$ = event.status === 'PREPARING'
      ? this.kitchenService.startPreparing(event.orderId)
      : this.kitchenService.markReady(event.orderId);

    action$.subscribe(() => {
      this.loadOrders();
      this.loadStats();
    });
  }

  onItemComplete(event: { orderId: number; itemId: number }): void {
    this.kitchenService.markItemComplete(event.orderId, event.itemId)
      .subscribe(() => {
        this.loadOrders();
        this.loadStats();
      });
  }

  private playNewOrderAlert(message: string): void {
    // Play sound using AudioContext for cross-browser support
    try {
      const ctx = new AudioContext();
      const oscillator = ctx.createOscillator();
      const gain = ctx.createGain();
      oscillator.connect(gain);
      gain.connect(ctx.destination);
      oscillator.frequency.value = 800;
      oscillator.type = 'sine';
      gain.gain.value = 0.3;
      oscillator.start();
      oscillator.stop(ctx.currentTime + 0.3);
    } catch {
      // Audio not available — ignore
    }
    this.snackBar.open(message, 'OK', { duration: 5000, panelClass: 'new-order-snack' });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    this.wsService.disconnect();
  }
}
