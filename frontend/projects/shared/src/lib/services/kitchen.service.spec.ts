import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { KitchenService, KitchenStats } from './kitchen.service';
import { Order } from '../models/order.model';

describe('KitchenService', () => {
  let service: KitchenService;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api/kitchen';

  const mockOrder: Order = {
    id: 1,
    orderNumber: 'ORD-001',
    tableId: 3,
    tableNumber: 3,
    waiterId: 2,
    waiterName: 'waiter1',
    status: 'PREPARING',
    orderType: 'DINE_IN',
    items: [
      {
        id: 1,
        menuItemId: 10,
        menuItemName: 'Burger',
        quantity: 2,
        unitPrice: 9.99,
        totalPrice: 19.98,
        specialRequests: 'No onions',
        status: 'PENDING'
      }
    ],
    subtotal: 19.98,
    taxAmount: 2.00,
    totalAmount: 21.98,
    specialInstructions: '',
    createdAt: '2024-01-15T12:00:00',
    completedAt: ''
  };

  const mockStats: KitchenStats = {
    activeOrders: 5,
    preparingOrders: 3,
    readyOrders: 2,
    avgPrepTimeMinutes: 15
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [KitchenService]
    });
    service = TestBed.inject(KitchenService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getActiveOrders', () => {
    it('should send a GET request to /kitchen/orders/active', () => {
      const mockOrders: Order[] = [mockOrder];

      service.getActiveOrders().subscribe(orders => {
        expect(orders).toEqual(mockOrders);
        expect(orders.length).toBe(1);
      });

      const req = httpMock.expectOne(`${apiUrl}/orders/active`);
      expect(req.request.method).toBe('GET');
      req.flush(mockOrders);
    });

    it('should return an empty array when there are no active orders', () => {
      service.getActiveOrders().subscribe(orders => {
        expect(orders).toEqual([]);
        expect(orders.length).toBe(0);
      });

      const req = httpMock.expectOne(`${apiUrl}/orders/active`);
      req.flush([]);
    });
  });

  describe('startPreparing', () => {
    it('should send a POST request to /kitchen/orders/{id}/start', () => {
      const preparingOrder: Order = { ...mockOrder, status: 'PREPARING' };

      service.startPreparing(1).subscribe(order => {
        expect(order).toEqual(preparingOrder);
        expect(order.status).toBe('PREPARING');
      });

      const req = httpMock.expectOne(`${apiUrl}/orders/1/start`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toBeNull();
      req.flush(preparingOrder);
    });

    it('should handle different order IDs', () => {
      service.startPreparing(42).subscribe();

      const req = httpMock.expectOne(`${apiUrl}/orders/42/start`);
      expect(req.request.method).toBe('POST');
      req.flush(mockOrder);
    });
  });

  describe('markReady', () => {
    it('should send a POST request to /kitchen/orders/{id}/ready', () => {
      const readyOrder: Order = { ...mockOrder, status: 'READY' };

      service.markReady(1).subscribe(order => {
        expect(order).toEqual(readyOrder);
        expect(order.status).toBe('READY');
      });

      const req = httpMock.expectOne(`${apiUrl}/orders/1/ready`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toBeNull();
      req.flush(readyOrder);
    });

    it('should handle different order IDs', () => {
      service.markReady(99).subscribe();

      const req = httpMock.expectOne(`${apiUrl}/orders/99/ready`);
      expect(req.request.method).toBe('POST');
      req.flush(mockOrder);
    });
  });

  describe('markItemComplete', () => {
    it('should send a POST request to /kitchen/orders/{id}/items/{itemId}/complete', () => {
      service.markItemComplete(1, 5).subscribe(response => {
        expect(response).toBeTruthy();
      });

      const req = httpMock.expectOne(`${apiUrl}/orders/1/items/5/complete`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toBeNull();
      req.flush({ success: true });
    });

    it('should handle different order and item IDs', () => {
      service.markItemComplete(10, 20).subscribe();

      const req = httpMock.expectOne(`${apiUrl}/orders/10/items/20/complete`);
      expect(req.request.method).toBe('POST');
      req.flush({});
    });
  });

  describe('getStats', () => {
    it('should send a GET request to /kitchen/stats', () => {
      service.getStats().subscribe(stats => {
        expect(stats).toEqual(mockStats);
        expect(stats.activeOrders).toBe(5);
        expect(stats.preparingOrders).toBe(3);
        expect(stats.readyOrders).toBe(2);
        expect(stats.avgPrepTimeMinutes).toBe(15);
      });

      const req = httpMock.expectOne(`${apiUrl}/stats`);
      expect(req.request.method).toBe('GET');
      req.flush(mockStats);
    });
  });

  describe('error handling', () => {
    it('should propagate HTTP errors from getActiveOrders', () => {
      service.getActiveOrders().subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.status).toBe(500);
        }
      });

      const req = httpMock.expectOne(`${apiUrl}/orders/active`);
      req.flush('Server error', { status: 500, statusText: 'Internal Server Error' });
    });

    it('should propagate HTTP errors from startPreparing', () => {
      service.startPreparing(999).subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.status).toBe(404);
        }
      });

      const req = httpMock.expectOne(`${apiUrl}/orders/999/start`);
      req.flush('Not found', { status: 404, statusText: 'Not Found' });
    });
  });
});
