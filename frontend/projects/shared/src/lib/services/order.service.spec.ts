import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { OrderService } from './order.service';
import { Order, CreateOrderRequest, OrderStatus } from '../models/order.model';

describe('OrderService', () => {
  let service: OrderService;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api/orders';

  const mockOrder: Order = {
    id: 1,
    orderNumber: 'ORD-001',
    tableId: 5,
    tableNumber: 5,
    waiterId: 2,
    waiterName: 'waiter1',
    status: 'PENDING',
    orderType: 'DINE_IN',
    items: [
      {
        id: 1,
        menuItemId: 10,
        menuItemName: 'Margherita Pizza',
        quantity: 1,
        unitPrice: 12.99,
        totalPrice: 12.99,
        specialRequests: '',
        status: 'PENDING'
      },
      {
        id: 2,
        menuItemId: 15,
        menuItemName: 'Caesar Salad',
        quantity: 2,
        unitPrice: 8.50,
        totalPrice: 17.00,
        specialRequests: 'No croutons',
        status: 'PENDING'
      }
    ],
    subtotal: 29.99,
    taxAmount: 3.00,
    totalAmount: 32.99,
    specialInstructions: 'Please serve salad first',
    createdAt: '2024-01-15T12:00:00',
    completedAt: ''
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [OrderService]
    });
    service = TestBed.inject(OrderService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('createOrder', () => {
    it('should send a POST request to /orders', () => {
      const createRequest: CreateOrderRequest = {
        tableId: 5,
        items: [
          { menuItemId: 10, quantity: 1 },
          { menuItemId: 15, quantity: 2, specialRequests: 'No croutons' }
        ],
        orderType: 'DINE_IN',
        specialInstructions: 'Please serve salad first'
      };

      service.createOrder(createRequest).subscribe(order => {
        expect(order).toEqual(mockOrder);
        expect(order.id).toBe(1);
        expect(order.orderNumber).toBe('ORD-001');
      });

      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(createRequest);
      req.flush(mockOrder);
    });

    it('should handle a minimal create order request', () => {
      const createRequest: CreateOrderRequest = {
        tableId: 1,
        items: [{ menuItemId: 5, quantity: 1 }]
      };

      service.createOrder(createRequest).subscribe(order => {
        expect(order).toBeTruthy();
      });

      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(createRequest);
      req.flush(mockOrder);
    });
  });

  describe('getOrder', () => {
    it('should send a GET request to /orders/{id}', () => {
      service.getOrder(1).subscribe(order => {
        expect(order).toEqual(mockOrder);
        expect(order.id).toBe(1);
      });

      const req = httpMock.expectOne(`${apiUrl}/1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockOrder);
    });

    it('should handle different order IDs', () => {
      service.getOrder(42).subscribe();

      const req = httpMock.expectOne(`${apiUrl}/42`);
      expect(req.request.method).toBe('GET');
      req.flush(mockOrder);
    });

    it('should propagate 404 errors for non-existent orders', () => {
      service.getOrder(999).subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.status).toBe(404);
        }
      });

      const req = httpMock.expectOne(`${apiUrl}/999`);
      req.flush('Not found', { status: 404, statusText: 'Not Found' });
    });
  });

  describe('getActiveOrders', () => {
    it('should send a GET request to /orders/active', () => {
      const mockOrders: Order[] = [mockOrder];

      service.getActiveOrders().subscribe(orders => {
        expect(orders).toEqual(mockOrders);
        expect(orders.length).toBe(1);
      });

      const req = httpMock.expectOne(`${apiUrl}/active`);
      expect(req.request.method).toBe('GET');
      req.flush(mockOrders);
    });

    it('should return an empty array when no active orders exist', () => {
      service.getActiveOrders().subscribe(orders => {
        expect(orders).toEqual([]);
        expect(orders.length).toBe(0);
      });

      const req = httpMock.expectOne(`${apiUrl}/active`);
      req.flush([]);
    });
  });

  describe('getOrdersByStatus', () => {
    it('should send a GET request to /orders/status/{status}', () => {
      const status: OrderStatus = 'PREPARING';
      const preparingOrders: Order[] = [{ ...mockOrder, status: 'PREPARING' }];

      service.getOrdersByStatus(status).subscribe(orders => {
        expect(orders).toEqual(preparingOrders);
        expect(orders[0].status).toBe('PREPARING');
      });

      const req = httpMock.expectOne(`${apiUrl}/status/PREPARING`);
      expect(req.request.method).toBe('GET');
      req.flush(preparingOrders);
    });

    it('should handle PENDING status', () => {
      service.getOrdersByStatus('PENDING').subscribe();

      const req = httpMock.expectOne(`${apiUrl}/status/PENDING`);
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });

    it('should handle COMPLETED status', () => {
      service.getOrdersByStatus('COMPLETED').subscribe();

      const req = httpMock.expectOne(`${apiUrl}/status/COMPLETED`);
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });

    it('should handle CANCELLED status', () => {
      service.getOrdersByStatus('CANCELLED').subscribe();

      const req = httpMock.expectOne(`${apiUrl}/status/CANCELLED`);
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });
  });

  describe('updateOrderStatus', () => {
    it('should send a PUT request to /orders/{id}/status with status param', () => {
      const updatedOrder: Order = { ...mockOrder, status: 'CONFIRMED' };

      service.updateOrderStatus(1, 'CONFIRMED').subscribe(order => {
        expect(order).toEqual(updatedOrder);
        expect(order.status).toBe('CONFIRMED');
      });

      const req = httpMock.expectOne(r =>
        r.url === `${apiUrl}/1/status` && r.params.get('status') === 'CONFIRMED'
      );
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toBeNull();
      req.flush(updatedOrder);
    });

    it('should handle transitioning to PREPARING', () => {
      const updatedOrder: Order = { ...mockOrder, status: 'PREPARING' };

      service.updateOrderStatus(1, 'PREPARING').subscribe(order => {
        expect(order.status).toBe('PREPARING');
      });

      const req = httpMock.expectOne(r =>
        r.url === `${apiUrl}/1/status` && r.params.get('status') === 'PREPARING'
      );
      expect(req.request.method).toBe('PUT');
      req.flush(updatedOrder);
    });

    it('should handle transitioning to CANCELLED', () => {
      const updatedOrder: Order = { ...mockOrder, status: 'CANCELLED' };

      service.updateOrderStatus(1, 'CANCELLED').subscribe(order => {
        expect(order.status).toBe('CANCELLED');
      });

      const req = httpMock.expectOne(r =>
        r.url === `${apiUrl}/1/status` && r.params.get('status') === 'CANCELLED'
      );
      req.flush(updatedOrder);
    });
  });

  describe('error handling', () => {
    it('should propagate HTTP errors from createOrder', () => {
      const createRequest: CreateOrderRequest = {
        tableId: 1,
        items: [{ menuItemId: 5, quantity: 1 }]
      };

      service.createOrder(createRequest).subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.status).toBe(400);
        }
      });

      const req = httpMock.expectOne(apiUrl);
      req.flush('Bad request', { status: 400, statusText: 'Bad Request' });
    });

    it('should propagate HTTP errors from updateOrderStatus', () => {
      service.updateOrderStatus(1, 'COMPLETED').subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.status).toBe(409);
        }
      });

      const req = httpMock.expectOne(r =>
        r.url === `${apiUrl}/1/status` && r.params.get('status') === 'COMPLETED'
      );
      req.flush('Conflict', { status: 409, statusText: 'Conflict' });
    });
  });
});
