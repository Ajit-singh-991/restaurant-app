import { Injectable, OnDestroy } from '@angular/core';
import { RxStomp } from '@stomp/rx-stomp';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '@environments/environment';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService implements OnDestroy {
  private rxStomp: RxStomp;

  constructor() {
    this.rxStomp = new RxStomp();
  }

  connect(): void {
    this.rxStomp.configure({
      brokerURL: environment.wsUrl,
      heartbeatIncoming: 0,
      heartbeatOutgoing: 20000,
      reconnectDelay: 5000,
    });
    this.rxStomp.activate();
  }

  disconnect(): void {
    this.rxStomp.deactivate();
  }

  subscribeToOrders(): Observable<any> {
    return this.rxStomp.watch('/topic/orders').pipe(
      map(message => JSON.parse(message.body))
    );
  }

  subscribeToKitchen(): Observable<any> {
    return this.rxStomp.watch('/topic/kitchen').pipe(
      map(message => JSON.parse(message.body))
    );
  }

  subscribeToTables(): Observable<any> {
    return this.rxStomp.watch('/topic/tables').pipe(
      map(message => JSON.parse(message.body))
    );
  }

  subscribeToWaiter(): Observable<any> {
    return this.rxStomp.watch('/topic/waiter').pipe(
      map(message => JSON.parse(message.body))
    );
  }

  get connected$(): Observable<number> {
    return this.rxStomp.connectionState$;
  }

  get isConnected(): boolean {
    return this.rxStomp.connected();
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}
