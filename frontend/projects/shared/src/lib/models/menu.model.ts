export interface Category {
  id: number;
  name: string;
  description: string;
  imageUrl?: string;
  displayOrder: number;
  active: boolean;
}

export interface MenuItem {
  id: number;
  name: string;
  description: string;
  price: number;
  categoryId: number;
  categoryName: string;
  imageUrl: string;
  available: boolean;
  preparationTimeMinutes: number;
  vegetarian: boolean;
  vegan: boolean;
  glutenFree: boolean;
  allergens?: string;
  stationId?: number;
}

export interface CreateMenuItemRequest {
  name: string;
  description?: string;
  price: number;
  categoryId: number;
  imageUrl?: string;
  preparationTimeMinutes?: number;
  vegetarian?: boolean;
  vegan?: boolean;
  glutenFree?: boolean;
  allergens?: string;
  stationId?: number;
}

export interface KitchenStation {
  id: number;
  name: string;
  displayOrder: number;
}

export interface BulkUploadResult {
  created: number;
  failed: number;
  errors: string[];
}

export interface CartItem extends MenuItem {
  quantity: number;
  subtotal: number;
  specialRequests?: string;
}
