import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { MenuService } from './menu.service';
import { Category, MenuItem, CreateMenuItemRequest } from '../models/menu.model';

describe('MenuService', () => {
  let service: MenuService;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api/menu';

  const mockCategories: Category[] = [
    { id: 1, name: 'Appetizers', description: 'Start your meal', displayOrder: 1, active: true },
    { id: 2, name: 'Main Course', description: 'Entrees', displayOrder: 2, active: true },
    { id: 3, name: 'Desserts', description: 'Sweet treats', displayOrder: 3, active: true }
  ];

  const mockMenuItem: MenuItem = {
    id: 1,
    name: 'Margherita Pizza',
    description: 'Classic pizza with fresh mozzarella and basil',
    price: 12.99,
    categoryId: 2,
    categoryName: 'Main Course',
    imageUrl: 'https://example.com/pizza.jpg',
    available: true,
    preparationTimeMinutes: 20,
    vegetarian: true,
    vegan: false,
    glutenFree: false
  };

  const mockMenuItems: MenuItem[] = [
    mockMenuItem,
    {
      id: 2,
      name: 'Caesar Salad',
      description: 'Fresh romaine with caesar dressing',
      price: 8.50,
      categoryId: 1,
      categoryName: 'Appetizers',
      imageUrl: 'https://example.com/salad.jpg',
      available: true,
      preparationTimeMinutes: 10,
      vegetarian: true,
      vegan: false,
      glutenFree: true
    }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [MenuService]
    });
    service = TestBed.inject(MenuService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getCategories', () => {
    it('should send a GET request to /menu/categories', () => {
      service.getCategories().subscribe(categories => {
        expect(categories).toEqual(mockCategories);
        expect(categories.length).toBe(3);
      });

      const req = httpMock.expectOne(`${apiUrl}/categories`);
      expect(req.request.method).toBe('GET');
      req.flush(mockCategories);
    });

    it('should return an empty array when no categories exist', () => {
      service.getCategories().subscribe(categories => {
        expect(categories).toEqual([]);
        expect(categories.length).toBe(0);
      });

      const req = httpMock.expectOne(`${apiUrl}/categories`);
      req.flush([]);
    });
  });

  describe('getMenuItems', () => {
    it('should send a GET request to /menu/items', () => {
      service.getMenuItems().subscribe(items => {
        expect(items).toEqual(mockMenuItems);
        expect(items.length).toBe(2);
      });

      const req = httpMock.expectOne(`${apiUrl}/items`);
      expect(req.request.method).toBe('GET');
      req.flush(mockMenuItems);
    });

    it('should return an empty array when no items exist', () => {
      service.getMenuItems().subscribe(items => {
        expect(items).toEqual([]);
      });

      const req = httpMock.expectOne(`${apiUrl}/items`);
      req.flush([]);
    });
  });

  describe('getItemsByCategory', () => {
    it('should send a GET request to /menu/items/category/{id}', () => {
      const categoryId = 2;
      const mainCourseItems: MenuItem[] = [mockMenuItem];

      service.getItemsByCategory(categoryId).subscribe(items => {
        expect(items).toEqual(mainCourseItems);
        expect(items.length).toBe(1);
        expect(items[0].categoryId).toBe(2);
      });

      const req = httpMock.expectOne(`${apiUrl}/items/category/${categoryId}`);
      expect(req.request.method).toBe('GET');
      req.flush(mainCourseItems);
    });

    it('should handle different category IDs', () => {
      service.getItemsByCategory(1).subscribe();

      const req = httpMock.expectOne(`${apiUrl}/items/category/1`);
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });

    it('should return an empty array for a category with no items', () => {
      service.getItemsByCategory(99).subscribe(items => {
        expect(items).toEqual([]);
      });

      const req = httpMock.expectOne(`${apiUrl}/items/category/99`);
      req.flush([]);
    });
  });

  describe('getItemById', () => {
    it('should send a GET request to /menu/items/{id}', () => {
      service.getItemById(1).subscribe(item => {
        expect(item).toEqual(mockMenuItem);
        expect(item.name).toBe('Margherita Pizza');
      });

      const req = httpMock.expectOne(`${apiUrl}/items/1`);
      expect(req.request.method).toBe('GET');
      req.flush(mockMenuItem);
    });

    it('should propagate 404 for non-existent item', () => {
      service.getItemById(999).subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.status).toBe(404);
        }
      });

      const req = httpMock.expectOne(`${apiUrl}/items/999`);
      req.flush('Not found', { status: 404, statusText: 'Not Found' });
    });
  });

  describe('searchItems', () => {
    it('should send a GET request to /menu/items/search with query param', () => {
      const query = 'pizza';

      service.searchItems(query).subscribe(items => {
        expect(items).toEqual([mockMenuItem]);
        expect(items.length).toBe(1);
      });

      const req = httpMock.expectOne(r =>
        r.url === `${apiUrl}/items/search` && r.params.get('q') === 'pizza'
      );
      expect(req.request.method).toBe('GET');
      req.flush([mockMenuItem]);
    });

    it('should return empty array when no results found', () => {
      service.searchItems('nonexistent').subscribe(items => {
        expect(items).toEqual([]);
      });

      const req = httpMock.expectOne(r =>
        r.url === `${apiUrl}/items/search` && r.params.get('q') === 'nonexistent'
      );
      req.flush([]);
    });

    it('should handle special characters in search query', () => {
      service.searchItems('caesar salad').subscribe();

      const req = httpMock.expectOne(r =>
        r.url === `${apiUrl}/items/search` && r.params.get('q') === 'caesar salad'
      );
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });
  });

  describe('createItem', () => {
    it('should send a POST request to /menu/items', () => {
      const createRequest: CreateMenuItemRequest = {
        name: 'New Pizza',
        description: 'A new pizza',
        price: 14.99,
        categoryId: 2,
        preparationTimeMinutes: 25,
        vegetarian: true
      };

      const createdItem: MenuItem = {
        ...mockMenuItem,
        id: 10,
        name: 'New Pizza',
        description: 'A new pizza',
        price: 14.99
      };

      service.createItem(createRequest).subscribe(item => {
        expect(item).toEqual(createdItem);
      });

      const req = httpMock.expectOne(`${apiUrl}/items`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(createRequest);
      req.flush(createdItem);
    });
  });

  describe('updateItem', () => {
    it('should send a PUT request to /menu/items/{id}', () => {
      const updateRequest: CreateMenuItemRequest = {
        name: 'Updated Pizza',
        price: 15.99,
        categoryId: 2
      };

      const updatedItem: MenuItem = { ...mockMenuItem, name: 'Updated Pizza', price: 15.99 };

      service.updateItem(1, updateRequest).subscribe(item => {
        expect(item).toEqual(updatedItem);
      });

      const req = httpMock.expectOne(`${apiUrl}/items/1`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(updateRequest);
      req.flush(updatedItem);
    });
  });

  describe('deleteItem', () => {
    it('should send a DELETE request to /menu/items/{id}', () => {
      service.deleteItem(1).subscribe();

      const req = httpMock.expectOne(`${apiUrl}/items/1`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });

  describe('toggleAvailability', () => {
    it('should send a PATCH request to /menu/items/{id}/availability', () => {
      const toggledItem: MenuItem = { ...mockMenuItem, available: false };

      service.toggleAvailability(1).subscribe(item => {
        expect(item.available).toBeFalse();
      });

      const req = httpMock.expectOne(`${apiUrl}/items/1/availability`);
      expect(req.request.method).toBe('PATCH');
      req.flush(toggledItem);
    });
  });

  describe('error handling', () => {
    it('should propagate HTTP errors from getCategories', () => {
      service.getCategories().subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.status).toBe(500);
        }
      });

      const req = httpMock.expectOne(`${apiUrl}/categories`);
      req.flush('Server error', { status: 500, statusText: 'Internal Server Error' });
    });

    it('should propagate HTTP errors from searchItems', () => {
      service.searchItems('test').subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.status).toBe(500);
        }
      });

      const req = httpMock.expectOne(r =>
        r.url === `${apiUrl}/items/search` && r.params.get('q') === 'test'
      );
      req.flush('Server error', { status: 500, statusText: 'Internal Server Error' });
    });
  });
});
