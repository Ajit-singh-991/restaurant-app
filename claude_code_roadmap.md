# 🗺️ Restaurant App - Claude Code Project Roadmap
**Step-by-Step Prompts to Build Your Application**

---

## How to Use This Roadmap

1. Follow the phases in order
2. Copy each prompt and paste it into Claude Code
3. Review what Claude Code creates
4. Test each feature before moving to the next
5. Save your progress with Git commits

---

## 🎯 PHASE 1: Project Setup (Day 1)

### Step 1.1: Create Project Structure

**Prompt for Claude Code:**
```
Create a complete project structure for a restaurant management system with these requirements:

PROJECT STRUCTURE:
restaurant-app/
├── frontend/          (Angular workspace)
├── backend/           (Spring Boot)
├── database/          (SQL scripts)
└── docs/              (Documentation)

FRONTEND SETUP:
- Angular 17+ with Angular CLI
- Workspace with 4 applications:
  1. customer-app (public website & ordering)
  2. waiter-app (tablet interface for servers)
  3. kitchen-app (kitchen display system)
  4. admin-app (back-office management)
- Shared libraries folder for common components
- Angular Material for UI
- Environment configuration files

BACKEND SETUP:
- Spring Boot 3.x with Java 17
- Multi-module Maven project:
  1. restaurant-common (shared entities, DTOs)
  2. restaurant-api (REST controllers)
  3. restaurant-service (business logic)
  4. restaurant-repository (database layer)
- application.yml for configuration
- Separate dev and prod profiles

DATABASE:
- PostgreSQL connection configuration
- Initial schema.sql file with basic tables:
  - users
  - restaurants
  - menu_categories
  - menu_items
  - tables
  - orders
  - order_items

Please create all folders, configuration files, and package.json / pom.xml files with all necessary dependencies.
```

**Expected Output:** Complete folder structure with config files

---

### Step 1.2: Setup Database

**Prompt for Claude Code:**
```
Create a complete PostgreSQL database setup with these requirements:

1. CREATE DATABASE script for 'restaurant_db'

2. Complete schema with these tables:

CORE TABLES:
- users (id, username, email, password_hash, role, created_at, updated_at)
- roles (id, name, permissions)
- restaurants (id, name, address, phone, email, settings)

MENU TABLES:
- menu_categories (id, restaurant_id, name, display_order, is_active)
- menu_items (id, category_id, name, description, price, image_url, is_available, created_at)
- item_ingredients (id, item_id, ingredient_id, quantity)

TABLE MANAGEMENT:
- restaurant_tables (id, restaurant_id, table_number, capacity, status)
- table_reservations (id, table_id, customer_name, customer_phone, reservation_time, party_size, status)

ORDER TABLES:
- orders (id, restaurant_id, table_id, customer_id, order_type, status, total_amount, created_at)
- order_items (id, order_id, menu_item_id, quantity, price, special_instructions)
- order_status_history (id, order_id, status, changed_by, changed_at)

PAYMENT TABLES:
- payments (id, order_id, amount, payment_method, transaction_id, status, created_at)

INVENTORY TABLES:
- ingredients (id, restaurant_id, name, unit, current_stock, reorder_level)
- suppliers (id, name, contact_person, phone, email)
- purchase_orders (id, supplier_id, order_date, delivery_date, total_amount, status)

CUSTOMER TABLES:
- customers (id, name, email, phone, address, loyalty_points, created_at)
- customer_addresses (id, customer_id, address_line1, city, postal_code, is_default)

3. Include proper:
- Primary keys
- Foreign keys with ON DELETE CASCADE/RESTRICT as appropriate
- Indexes for performance
- Default values
- Check constraints
- Timestamps

4. Add sample seed data for testing:
- 1 restaurant
- 2 admin users, 2 waiters, 1 kitchen staff
- 5 menu categories
- 20 menu items
- 10 restaurant tables
- 5 sample customers

Create the schema.sql file in the database folder.
```

**Expected Output:** Complete SQL schema file with all tables and relationships

---

### Step 1.3: Setup Spring Boot Backend

**Prompt for Claude Code:**
```
Setup the Spring Boot backend with these specifications:

1. UPDATE pom.xml with all required dependencies:
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- Spring Boot Starter WebSocket
- PostgreSQL Driver
- JWT libraries (jjwt-api, jjwt-impl, jjwt-jackson)
- Lombok
- Spring Boot Starter Validation
- Spring Boot Starter Mail (for notifications)
- Spring Boot DevTools

2. CREATE application.yml with:
- Database connection to PostgreSQL (localhost:5432/restaurant_db)
- JPA/Hibernate configuration (show-sql: true for dev)
- Server port: 8080
- JWT secret key configuration
- File upload configuration
- CORS configuration
- Profile-specific settings (dev, prod)

3. CREATE main package structure:
com.restaurant/
├── config/          (SecurityConfig, WebSocketConfig, CorsConfig)
├── controller/      (REST controllers)
├── dto/            (Data Transfer Objects)
├── entity/         (JPA entities)
├── repository/     (JPA repositories)
├── service/        (Business logic)
├── security/       (JWT utils, UserDetailsService)
├── exception/      (Custom exceptions, GlobalExceptionHandler)
└── RestaurantApplication.java (main class)

4. CREATE these configuration classes:
- SecurityConfig: Configure JWT authentication, disable CSRF, configure endpoints
- WebSocketConfig: Setup STOMP over WebSocket for real-time updates
- CorsConfig: Allow Angular frontend (http://localhost:4200)

5. CREATE basic entity classes with JPA annotations:
- User entity
- Restaurant entity
- MenuCategory entity
- MenuItem entity
- Order entity
- OrderItem entity

Include all necessary imports, annotations (@Entity, @Table, @Id, @GeneratedValue, etc.), and relationships (@ManyToOne, @OneToMany).
```

**Expected Output:** Complete Spring Boot project structure with configuration

---

### Step 1.4: Setup Angular Frontend

**Prompt for Claude Code:**
```
Setup the Angular frontend workspace with these specifications:

1. CREATE Angular workspace using Angular CLI 17+
- Workspace name: restaurant-frontend
- Routing: Yes
- Stylesheet: SCSS
- Standalone: No (use NgModules)

2. CREATE 4 applications in the workspace:
```bash
ng generate application customer-app --routing --style=scss
ng generate application waiter-app --routing --style=scss  
ng generate application kitchen-app --routing --style=scss
ng generate application admin-app --routing --style=scss
```

3. INSTALL Angular Material in each app:
```bash
ng add @angular/material
```
Choose Indigo/Pink theme

4. INSTALL additional dependencies:
```bash
npm install --save @auth0/angular-jwt rxjs socket.io-client ngx-socket-io
npm install --save-dev @types/socket.io-client
```

5. CREATE shared library:
```bash
ng generate library shared
```

In the shared library, create:
- models/ (TypeScript interfaces for all entities)
- services/ (HTTP services, Auth service, WebSocket service)
- components/ (Reusable UI components)
- guards/ (Auth guard, Role guard)
- interceptors/ (JWT interceptor, Error interceptor)

6. CREATE environment files for each app:
- environment.ts (dev): API_URL = 'http://localhost:8080/api'
- environment.prod.ts: Update with production URL

7. UPDATE tsconfig.json to include path mappings for the shared library

8. CREATE basic folder structure in each app:
customer-app/src/app/
├── core/           (singleton services)
├── features/       (feature modules)
│   ├── home/
│   ├── menu/
│   ├── cart/
│   ├── checkout/
│   └── orders/
├── shared/         (shared within this app)
└── app.component.ts

Please create all configuration files, folder structures, and update package.json with all dependencies.
```

**Expected Output:** Complete Angular workspace with all apps configured

---

## 🔐 PHASE 2: Authentication System (Day 2)

### Step 2.1: Backend - JWT Authentication

**Prompt for Claude Code:**
```
Create a complete JWT authentication system in Spring Boot:

1. CREATE JwtUtil class in security package:
- generateToken(UserDetails): String
- extractUsername(String token): String
- validateToken(String token, UserDetails): boolean
- extractAllClaims(String token)
- isTokenExpired(String token): boolean
- Token expiration: 24 hours

2. CREATE JwtRequestFilter extends OncePerRequestFilter:
- Extract JWT from Authorization header
- Validate token
- Set authentication in SecurityContext
- Handle token errors

3. CREATE CustomUserDetailsService implements UserDetailsService:
- Load user by username from database
- Map User entity to UserDetails

4. CREATE AuthController with endpoints:
- POST /api/auth/register (RegisterRequest -> User)
- POST /api/auth/login (LoginRequest -> JWT token + user info)
- POST /api/auth/refresh (Refresh token)
- GET /api/auth/me (Get current user info)

5. CREATE DTOs:
- LoginRequest (username, password)
- RegisterRequest (username, email, password, role)
- AuthResponse (token, tokenType, user info)

6. UPDATE SecurityConfig:
- Configure JWT filter
- Disable session management (stateless)
- Permit /api/auth/** endpoints
- Protect all other endpoints

7. CREATE PasswordEncoder bean (BCryptPasswordEncoder)

Include proper exception handling and validation.
```

**Expected Output:** Complete JWT authentication implementation

---

### Step 2.2: Frontend - Auth Service & Guards

**Prompt for Claude Code:**
```
Create authentication system in Angular shared library:

1. CREATE AuthService in shared/services/:
```typescript
Methods:
- login(username: string, password: string): Observable<AuthResponse>
- register(user: RegisterRequest): Observable<any>
- logout(): void
- getCurrentUser(): Observable<User>
- isAuthenticated(): boolean
- getToken(): string | null
- getRole(): string | null
- Store token in localStorage
- Emit user state changes via BehaviorSubject
```

2. CREATE JWT Interceptor:
- Add Authorization header to all requests
- Add 'Bearer ' + token
- Handle 401 responses (redirect to login)

3. CREATE Auth Guard:
- Implement CanActivate
- Check if user is authenticated
- Redirect to login if not
- Save attempted URL for redirect after login

4. CREATE Role Guard:
- Check user role matches required role
- Redirect to unauthorized page if not

5. CREATE models/auth.model.ts:
```typescript
- User interface
- LoginRequest interface
- RegisterRequest interface
- AuthResponse interface
```

6. CREATE Login Component in customer-app:
- Reactive form with username and password
- Email and password validation
- Show loading spinner during login
- Show error messages
- Redirect to dashboard after successful login
- Use Angular Material for UI (mat-card, mat-form-field, mat-button)

7. CREATE Register Component:
- Reactive form with username, email, password, confirm password
- Validation (email format, password strength, passwords match)
- Show error messages
- Link to login page

Please include all imports, proper error handling, and TypeScript types.
```

**Expected Output:** Complete Angular authentication system

---

## 📋 PHASE 3: Menu Management (Days 3-4)

### Step 3.1: Backend - Menu APIs

**Prompt for Claude Code:**
```
Create complete menu management system in Spring Boot:

1. CREATE MenuCategory entity (if not exists):
- id, restaurantId, name, description, displayOrder, isActive, createdAt, updatedAt

2. CREATE MenuItem entity (if not exists):
- id, categoryId, name, description, price, imageUrl, isAvailable, preparationTime, allergens, createdAt, updatedAt
- @ManyToOne relationship with MenuCategory

3. CREATE DTOs:
- MenuCategoryDTO (all fields)
- MenuItemDTO (all fields + category name)
- CreateMenuItemRequest
- UpdateMenuItemRequest

4. CREATE MenuCategoryRepository extends JpaRepository:
- findByRestaurantIdAndIsActiveTrue()
- findByRestaurantIdOrderByDisplayOrderAsc()

5. CREATE MenuItemRepository extends JpaRepository:
- findByCategoryIdAndIsAvailableTrue()
- findByNameContainingIgnoreCase()
- findByRestaurantId()

6. CREATE MenuService with methods:
CATEGORIES:
- getAllCategories(restaurantId)
- getCategoryById(id)
- createCategory(dto)
- updateCategory(id, dto)
- deleteCategory(id)
- reorderCategories(List<Long> ids)

ITEMS:
- getAllItems(restaurantId)
- getItemsByCategory(categoryId)
- getItemById(id)
- searchItems(query)
- createItem(dto)
- updateItem(id, dto)
- deleteItem(id)
- toggleAvailability(id)

7. CREATE MenuController with endpoints:
Categories:
- GET /api/menu/categories
- GET /api/menu/categories/{id}
- POST /api/menu/categories (admin only)
- PUT /api/menu/categories/{id} (admin only)
- DELETE /api/menu/categories/{id} (admin only)
- PUT /api/menu/categories/reorder (admin only)

Items:
- GET /api/menu/items
- GET /api/menu/items/{id}
- GET /api/menu/items/category/{categoryId}
- GET /api/menu/items/search?q=
- POST /api/menu/items (admin only)
- PUT /api/menu/items/{id} (admin only)
- DELETE /api/menu/items/{id} (admin only)
- PATCH /api/menu/items/{id}/availability (admin only)

8. ADD FileUploadController:
- POST /api/upload/menu-image
- Store images in static/images/menu/
- Return image URL
- Validate file type and size

Include proper validation, exception handling, and role-based security.
```

**Expected Output:** Complete menu management backend

---

### Step 3.2: Frontend - Menu Display (Customer App)

**Prompt for Claude Code:**
```
Create menu display feature in customer-app:

1. CREATE models in shared library:
```typescript
- MenuCategory interface
- MenuItem interface
```

2. CREATE MenuService in shared library:
```typescript
Methods:
- getCategories(): Observable<MenuCategory[]>
- getMenuItems(): Observable<MenuItem[]>
- getItemsByCategory(categoryId): Observable<MenuItem[]>
- searchItems(query: string): Observable<MenuItem[]>
```

3. CREATE menu feature module in customer-app:
```
ng generate module features/menu --routing
ng generate component features/menu/menu-list
ng generate component features/menu/menu-item-card
ng generate component features/menu/menu-filter
ng generate component features/menu/menu-search
```

4. CREATE menu-list component:
LAYOUT:
- Header with restaurant name and search bar
- Category tabs (horizontal scrollable)
- Grid of menu items (responsive: 1 col mobile, 2 tablet, 3+ desktop)
- Filter sidebar (price range, dietary filters, availability)

FEATURES:
- Load all categories and items on init
- Filter items by selected category
- Search functionality with debounce (300ms)
- Show "Add to Cart" button on each item
- Show item details modal on click
- Show "Out of Stock" for unavailable items
- Loading skeleton while fetching data
- Empty state when no items found

5. CREATE menu-item-card component:
DISPLAY:
- Item image (fallback image if not available)
- Item name
- Item description (truncated, show more on click)
- Price (formatted with currency)
- Allergen badges
- Preparation time
- "Add to Cart" button with quantity selector
- "Unavailable" overlay if not available

STYLING:
- Use Angular Material cards
- Hover effects
- Responsive image sizing
- Price in accent color

6. CREATE menu-filter component:
FILTERS:
- Price range slider
- Dietary preferences (vegetarian, vegan, gluten-free)
- Allergen exclusions
- "Clear All" button
- Show active filter count

7. CREATE menu-search component:
- Search input with icon
- Real-time search with debounce
- Clear button
- Show search results count

8. UPDATE menu routing:
```typescript
{ path: 'menu', component: MenuListComponent }
```

9. STYLING:
- Modern, clean design
- Use Angular Material theme
- Responsive grid layout
- Smooth animations
- Loading states

Use RxJS operators (map, debounceTime, distinctUntilChanged) for search and filtering.
Include error handling and loading states.
```

**Expected Output:** Complete menu display with filtering and search

---

### Step 3.3: Frontend - Menu Management (Admin App)

**Prompt for Claude Code:**
```
Create menu management interface in admin-app:

1. CREATE menu management feature module:
```
ng generate module features/menu-management --routing
ng generate component features/menu-management/menu-dashboard
ng generate component features/menu-management/category-list
ng generate component features/menu-management/category-form
ng generate component features/menu-management/item-list
ng generate component features/menu-management/item-form
ng generate component features/menu-management/item-details
```

2. CREATE menu-dashboard component:
LAYOUT:
- Statistics cards:
  - Total categories
  - Total items
  - Available items
  - Unavailable items
- Quick actions:
  - Add new category
  - Add new item
  - Bulk upload
- Recent activities list

3. CREATE category-list component:
FEATURES:
- Table with columns: Name, # Items, Display Order, Status, Actions
- Drag-and-drop to reorder categories
- Inline edit for quick changes
- Delete with confirmation
- Toggle active/inactive status
- Search and filter
- Add new category button

ACTIONS:
- Edit (open dialog)
- Delete (confirm dialog)
- Toggle status
- Reorder (drag handle)

4. CREATE category-form component (dialog):
FORM FIELDS:
- Name (required)
- Description
- Display order
- Is active (toggle)
- Image upload

FEATURES:
- Reactive form with validation
- Show/hide form based on create/edit mode
- Save and close
- Cancel button
- Success/error notifications

5. CREATE item-list component:
FEATURES:
- Data table with columns:
  - Image thumbnail
  - Name
  - Category
  - Price
  - Status (Available/Unavailable)
  - Actions
- Pagination (25 items per page)
- Sorting (by name, price, category)
- Filter by category
- Search by name
- Bulk actions (delete, toggle availability)

ACTIONS:
- View details
- Edit
- Delete (confirm)
- Toggle availability
- Duplicate item

6. CREATE item-form component:
FORM FIELDS:
- Name (required)
- Category (dropdown, required)
- Description (textarea)
- Price (number, required, min: 0.01)
- Preparation time (number, minutes)
- Image upload (with preview)
- Allergens (multi-select chips)
- Dietary tags (vegetarian, vegan, gluten-free, etc.)
- Is available (toggle)

FEATURES:
- Reactive form with validation
- Image upload with preview and crop
- Real-time price formatting
- Add/remove allergens dynamically
- Save as draft or publish
- Success/error handling

7. CREATE item-details component:
DISPLAY:
- Full item information
- Sales statistics
- Customer reviews/ratings
- Related items
- Edit button

8. CREATE ImageUploadComponent (reusable):
- Drag-and-drop file upload
- File preview
- Image cropping (optional)
- Progress bar
- File type and size validation
- Remove image button

9. UPDATE routing:
```typescript
{
  path: 'menu',
  children: [
    { path: '', component: MenuDashboardComponent },
    { path: 'categories', component: CategoryListComponent },
    { path: 'items', component: ItemListComponent },
    { path: 'items/new', component: ItemFormComponent },
    { path: 'items/:id', component: ItemDetailsComponent },
    { path: 'items/:id/edit', component: ItemFormComponent }
  ]
}
```

Use Angular Material components:
- mat-table for lists
- mat-dialog for forms
- mat-form-field for inputs
- mat-select for dropdowns
- mat-chip-list for tags
- mat-slide-toggle for toggles
- mat-card for containers

Include loading states, error handling, and success notifications using mat-snack-bar.
```

**Expected Output:** Complete admin menu management interface

---

## 🛒 PHASE 4: Shopping Cart & Orders (Days 5-6)

### Step 4.1: Backend - Order System

**Prompt for Claude Code:**
```
Create complete order management system:

1. CREATE Order entity:
- id, restaurantId, tableId, customerId, orderType (DINE_IN, TAKEOUT, DELIVERY)
- orderNumber (auto-generated, e.g., "ORD-20240211-0001")
- status (PENDING, CONFIRMED, PREPARING, READY, DELIVERED, COMPLETED, CANCELLED)
- subtotal, tax, discount, deliveryFee, totalAmount
- specialInstructions, createdAt, updatedAt
- @OneToMany relationship with OrderItem
- @ManyToOne relationship with User (customer)

2. CREATE OrderItem entity:
- id, orderId, menuItemId, quantity, unitPrice, totalPrice, specialInstructions
- @ManyToOne relationship with Order and MenuItem

3. CREATE OrderStatusHistory entity:
- id, orderId, status, changedBy (userId), changedAt, notes

4. CREATE DTOs:
- CreateOrderRequest (customerId, orderType, tableId, items[], specialInstructions)
- OrderItemRequest (menuItemId, quantity, specialInstructions)
- OrderDTO (full order details with items)
- OrderSummaryDTO (for lists)
- UpdateOrderStatusRequest (status, notes)

5. CREATE OrderRepository:
- findByRestaurantIdOrderByCreatedAtDesc()
- findByCustomerId()
- findByStatus()
- findByOrderNumber()
- findByCreatedAtBetween() (for date range)
- findByTableIdAndStatus() (for active table orders)

6. CREATE OrderService:
```java
Methods:
- createOrder(CreateOrderRequest): OrderDTO
  - Validate all menu items exist and are available
  - Calculate subtotal
  - Calculate tax (configurable %)
  - Apply discounts if any
  - Generate order number
  - Create order and order items
  - Deduct inventory (if configured)
  - Send notification via WebSocket

- getOrderById(Long id): OrderDTO
- getAllOrders(Pageable): Page<OrderSummaryDTO>
- getOrdersByStatus(String status): List<OrderDTO>
- getCustomerOrders(Long customerId): List<OrderDTO>
- getActiveTableOrders(Long tableId): List<OrderDTO>
- getOrdersByDateRange(LocalDateTime start, LocalDateTime end): List<OrderDTO>

- updateOrderStatus(Long id, UpdateOrderStatusRequest): OrderDTO
  - Validate status transition
  - Update order status
  - Create status history entry
  - Send WebSocket notification
  - Send email/SMS notification (if configured)

- cancelOrder(Long id, String reason): OrderDTO
  - Can only cancel PENDING or CONFIRMED orders
  - Restore inventory (if deducted)
  - Update status to CANCELLED
  - Send notification

- updateOrderItems(Long id, List<OrderItemRequest>): OrderDTO
  - Can only update PENDING orders
  - Recalculate totals
  - Update items

- calculateOrderTotal(List<OrderItemRequest>): BigDecimal
```

7. CREATE OrderController:
```java
Endpoints:
- POST /api/orders (create new order)
- GET /api/orders (get all orders with pagination)
- GET /api/orders/{id}
- GET /api/orders/customer/{customerId}
- GET /api/orders/table/{tableId}/active
- GET /api/orders/status/{status}
- PUT /api/orders/{id}/status
- PUT /api/orders/{id}/items
- DELETE /api/orders/{id}/cancel
- GET /api/orders/stats (daily/weekly/monthly stats)
```

8. CREATE WebSocket configuration for real-time order updates:
- Configure STOMP endpoints
- Create OrderNotificationService
- Send notifications on:
  - New order created
  - Order status changed
  - Order cancelled

Include proper validation, exception handling, and transactional operations.
```

**Expected Output:** Complete order management backend

---

### Step 4.2: Frontend - Shopping Cart (Customer App)

**Prompt for Claude Code:**
```
Create shopping cart feature in customer-app:

1. CREATE CartService in shared library:
```typescript
Properties:
- cart$: BehaviorSubject<CartItem[]>
- cartTotal$: Observable<number>
- cartItemCount$: Observable<number>

Methods:
- addToCart(item: MenuItem, quantity: number): void
- removeFromCart(itemId: number): void
- updateQuantity(itemId: number, quantity: number): void
- clearCart(): void
- getCart(): CartItem[]
- getCartTotal(): number
- Store cart in localStorage
```

2. CREATE models:
```typescript
- CartItem interface (extends MenuItem with quantity, subtotal)
```

3. CREATE cart feature module:
```
ng generate module features/cart --routing
ng generate component features/cart/cart-view
ng generate component features/cart/cart-item
ng generate component features/cart/cart-summary
ng generate component features/cart/checkout
```

4. CREATE cart-view component:
LAYOUT:
- Header "Your Cart" with item count
- List of cart items
- Cart summary sidebar (sticky on desktop)
- Empty cart state with "Browse Menu" button
- Continue shopping button
- Checkout button

FEATURES:
- Display all cart items
- Remove item button with confirm
- Update quantity (+ / - buttons)
- Show subtotal for each item
- Show cart total
- Apply promo code
- Show estimated delivery/pickup time
- Clear all button with confirm

5. CREATE cart-item component:
DISPLAY:
- Item image
- Item name
- Unit price
- Quantity selector (- button, input, + button)
- Subtotal
- Remove button
- Special instructions (expandable)

FEATURES:
- Update quantity on +/- click
- Update quantity on input change
- Validate quantity (min: 1, max: 99)
- Show confirmation before remove
- Update subtotal automatically

6. CREATE cart-summary component:
DISPLAY:
- Subtotal
- Tax (calculated)
- Delivery fee (if delivery)
- Discount (if applied)
- Total
- Checkout button

7. CREATE checkout component:
FORM SECTIONS:
1. Order Type Selection (radio buttons):
   - Dine In (show table selector)
   - Takeout (show pickup time selector)
   - Delivery (show address form)

2. Customer Information:
   - Name (autofill if logged in)
   - Phone number
   - Email

3. Address (if delivery):
   - Address line 1
   - Address line 2
   - City
   - Postal code
   - Delivery instructions

4. Payment Method (radio buttons):
   - Pay at counter/table
   - Pay online (integrate payment gateway later)

5. Order Summary:
   - Review cart items
   - Total amount
   - Estimated time

6. Special Instructions (textarea)

7. Buttons:
   - Place Order
   - Back to Cart

FEATURES:
- Reactive form with validation
- Show/hide sections based on order type
- Calculate delivery fee based on distance
- Validate all fields
- Show loading during order placement
- Success modal with order number
- Redirect to order tracking page

8. CREATE floating cart icon:
- Fixed position button with cart icon
- Show item count badge
- Click to open cart sidebar/navigate to cart

9. UPDATE menu-item-card:
- Add "Add to Cart" button
- Show quantity selector on hover
- Add to cart with animation
- Show success snackbar

Use Angular Material:
- mat-list for cart items
- mat-card for summary
- mat-form-field for inputs
- mat-radio-button for order type
- mat-stepper for checkout process (optional)

Include loading states, error handling, and success animations.
```

**Expected Output:** Complete shopping cart and checkout flow

---

## 👨‍🍳 PHASE 5: Kitchen Display System (Days 7-8)

### Step 5.1: Backend - Kitchen APIs

**Prompt for Claude Code:**
```
Create kitchen display system backend:

1. CREATE KitchenStation entity:
- id, restaurantId, name, type (GRILL, SALAD, DESSERT, DRINKS, etc.)
- assignedItems (JSON array of menu item IDs or category IDs)

2. CREATE OrderPreparation entity:
- id, orderId, stationId, status (PENDING, IN_PROGRESS, COMPLETED)
- startedAt, completedAt, preparedBy (userId)

3. CREATE KitchenService:
```java
Methods:
- getActiveOrders(): List<OrderDTO>
  - Get orders with status CONFIRMED or PREPARING
  - Group by station if applicable
  - Sort by order time (oldest first)
  
- getOrdersByStation(Long stationId): List<OrderDTO>
  - Filter items for specific station
  
- startPreparation(Long orderId, Long stationId, Long userId): OrderPreparation
  - Update order status to PREPARING
  - Create OrderPreparation record
  - Send WebSocket update
  
- markItemComplete(Long orderId, Long itemId, Long stationId): void
  - Mark specific item as completed
  - If all items complete, mark order as READY
  - Send WebSocket update
  
- markOrderReady(Long orderId): OrderDTO
  - Update status to READY
  - Send notification to waiter
  - Send customer notification
  
- getPreparationTime(): Map<Long, Integer>
  - Calculate average prep time by station
  
- getKitchenStats(): KitchenStatsDTO
  - Active orders count
  - Average preparation time
  - Orders by status
```

4. CREATE KitchenController:
```java
Endpoints:
- GET /api/kitchen/orders/active
- GET /api/kitchen/orders/station/{stationId}
- POST /api/kitchen/orders/{orderId}/start
- POST /api/kitchen/orders/{orderId}/items/{itemId}/complete
- POST /api/kitchen/orders/{orderId}/ready
- GET /api/kitchen/stats
- GET /api/kitchen/stations
```

5. UPDATE WebSocket configuration:
- Add kitchen topics for real-time updates
- Send notifications to /topic/kitchen on:
  - New order
  - Order cancelled
  - Station assignment changes

Include proper error handling and validation.
```

**Expected Output:** Kitchen management backend APIs

---

### Step 5.2: Frontend - Kitchen Display (Kitchen App)

**Prompt for Claude Code:**
```
Create kitchen display system in kitchen-app:

1. CREATE KitchenService in shared library:
```typescript
Methods:
- getActiveOrders(): Observable<Order[]>
- getOrdersByStation(stationId): Observable<Order[]>
- startPreparation(orderId, stationId): Observable<any>
- markItemComplete(orderId, itemId): Observable<any>
- markOrderReady(orderId): Observable<any>
- subscribeToUpdates(): Observable<OrderUpdate>
```

2. CREATE WebSocket service for real-time updates:
- Connect to kitchen WebSocket topic
- Listen for order updates
- Emit events to components

3. CREATE kitchen feature module:
```
ng generate module features/kitchen --routing
ng generate component features/kitchen/kitchen-display
ng generate component features/kitchen/order-card
ng generate component features/kitchen/order-timer
ng generate component features/kitchen/station-selector
```

4. CREATE kitchen-display component:
LAYOUT:
- Header:
  - Restaurant name
  - Current time
  - Active orders count
  - Station selector (if multi-station)
  
- Main area (Kanban board):
  - Column: New Orders (CONFIRMED status)
  - Column: In Progress (PREPARING status)
  - Column: Ready for Pickup (READY status)
  
- Each column shows order cards

FEATURES:
- Real-time updates via WebSocket
- Auto-refresh every 30 seconds
- Sound alert for new orders
- Color-coded priority (based on wait time)
- Drag-and-drop between columns
- Filter by station
- Full-screen mode toggle
- Dark mode (better for kitchen lighting)

5. CREATE order-card component:
DISPLAY:
- Order number (large, bold)
- Order type badge (Dine-in/Takeout/Delivery)
- Table number (if dine-in)
- Order time (how long ago)
- Timer (time since order placed)
- List of items with quantities
- Special instructions (highlighted)
- Action buttons based on status:
  - New: "Start Preparing"
  - In Progress: "Mark Ready" + item checkboxes
  - Ready: "Complete Order" (if picked up)

COLOR CODING:
- Green: < 10 minutes
- Yellow: 10-20 minutes
- Orange: 20-30 minutes
- Red: > 30 minutes

FEATURES:
- Click to expand/collapse details
- Check off individual items
- Add preparation notes
- Bump order (remove from screen when complete)

6. CREATE order-timer component:
- Show elapsed time since order placed
- Update every second
- Change color based on thresholds
- Flash when exceeding time limit

7. CREATE station-selector component:
- Dropdown to filter by station
- Show all stations
- Show assigned orders count per station
- "All Stations" option

8. STYLING:
- Large, readable fonts
- High contrast for visibility from distance
- Touch-friendly buttons (large tap targets)
- Minimal UI, focus on orders
- Responsive grid layout
- Use Angular CDK drag-drop for reordering

9. ADD features:
- Print order button
- Notification sound toggle
- Screen timeout prevention
- Auto-scroll to new orders
- Order priority indicators
- Estimated completion time

Use Angular Material:
- mat-card for order cards
- mat-badge for counts
- mat-chip for status badges
- mat-checkbox for items
- mat-icon for actions

WebSocket integration:
- Connect on component init
- Disconnect on destroy
- Handle reconnection
- Show connection status indicator

Include error handling and offline mode indicator.
```

**Expected Output:** Complete kitchen display system with real-time updates

---

## 💳 PHASE 6: Payment & Billing (Day 9)

### Step 6.1: Backend - Payment System

**Prompt for Claude Code:**
```
Create payment and billing system:

1. CREATE Payment entity:
- id, orderId, amount, paymentMethod (CASH, CARD, UPI, WALLET)
- transactionId, status (PENDING, SUCCESS, FAILED, REFUNDED)
- paidAt, refundedAt, refundReason

2. CREATE Invoice entity:
- id, orderId, invoiceNumber (auto-generated)
- billTo (customer name, address)
- items (JSON), subtotal, tax, discount, total
- generatedAt, generatedBy (userId)

3. CREATE PaymentService:
```java
Methods:
- processPayment(Long orderId, PaymentRequest): Payment
  - Validate order exists and is not paid
  - Create payment record
  - Update order status to PAID
  - Generate invoice
  - Send receipt email
  
- getPaymentByOrderId(Long orderId): Payment
- getAllPayments(Pageable): Page<Payment>
- refundPayment(Long paymentId, String reason): Payment
  - Can only refund SUCCESS payments
  - Create refund record
  - Update payment status to REFUNDED
  
- generateInvoice(Long orderId): Invoice
  - Get order details
  - Calculate totals
  - Generate invoice number
  - Save as PDF
  
- getInvoiceByOrderId(Long orderId): Invoice
- downloadInvoicePdf(Long invoiceId): byte[]

- getDailySales(): BigDecimal
- getPaymentStats(): PaymentStatsDTO
```

4. CREATE PaymentController:
```java
Endpoints:
- POST /api/payments/process
- GET /api/payments/order/{orderId}
- GET /api/payments
- POST /api/payments/{id}/refund
- GET /api/invoices/order/{orderId}
- GET /api/invoices/{id}/download
- GET /api/payments/stats
- GET /api/payments/daily-sales
```

5. CREATE PDF generation service:
- Use iText or similar library
- Generate professional invoice PDF
- Include restaurant logo
- Customer details
- Itemized list
- Tax breakdown
- QR code for payment (optional)

6. CREATE Email service:
- Send receipt email after payment
- Include invoice PDF attachment
- Template with restaurant branding

Include proper transaction management and error handling.
```

**Expected Output:** Complete payment and billing backend

---

### Step 6.2: Frontend - Billing Interface (Waiter App)

**Prompt for Claude Code:**
```
Create billing interface in waiter-app:

1. CREATE PaymentService in shared library:
```typescript
Methods:
- processPayment(orderId, paymentRequest): Observable<Payment>
- getPaymentByOrder(orderId): Observable<Payment>
- downloadInvoice(orderId): Observable<Blob>
```

2. CREATE billing feature module:
```
ng generate module features/billing --routing
ng generate component features/billing/bill-view
ng generate component features/billing/payment-dialog
ng generate component features/billing/split-bill-dialog
ng generate component features/billing/invoice-view
```

3. CREATE bill-view component:
DISPLAY:
- Order summary:
  - Order number
  - Table number
  - Customer name
  - Order date/time
  
- Itemized list:
  - Item name
  - Quantity
  - Unit price
  - Subtotal
  
- Calculations:
  - Subtotal
  - Tax breakdown (show %)
  - Service charge (if applicable)
  - Discount (show % or amount)
  - Total amount
  
- Payment status:
  - Unpaid / Partially paid / Fully paid
  - Payment history (if multiple)

ACTIONS:
- Process payment button
- Split bill button
- Apply discount button
- Print bill button
- Send to email button

4. CREATE payment-dialog component:
FORM:
- Amount to pay (default: remaining amount)
- Payment method (radio buttons):
  - Cash
  - Card
  - UPI
  - Wallet
- Transaction ID (for card/UPI)
- Customer phone/email (for receipt)

FEATURES:
- Calculate change (if cash and amount > total)
- Validate amount
- Process payment on submit
- Show success message
- Print receipt option
- Send email receipt option
- Close and refresh parent on success

5. CREATE split-bill-dialog component:
OPTIONS:
- Equal split:
  - Number of ways to split
  - Show amount per person
  
- Custom split:
  - Select items for each person
  - Calculate individual totals
  
- By percentage:
  - Assign % to each person

FEATURES:
- Add person button
- Assign items to persons
- Show running totals
- Validate totals equal bill amount
- Generate separate bills
- Process payments individually

6. CREATE invoice-view component:
DISPLAY:
- Restaurant header:
  - Logo
  - Name, address, phone, email
  - Tax ID / Registration number
  
- Invoice details:
  - Invoice number
  - Date/time
  - Order number
  - Table number
  
- Customer details:
  - Name
  - Phone
  - Address (if available)
  
- Itemized table:
  - Item, Qty, Rate, Amount columns
  
- Summary:
  - Subtotal
  - Tax breakdown
  - Discounts
  - Grand total
  
- Payment details:
  - Method
  - Amount paid
  - Transaction ID
  - Payment time
  
- Footer:
  - Thank you message
  - QR code for feedback

ACTIONS:
- Print button
- Download PDF button
- Email button
- Back button

7. ADD quick bill functionality to table view:
- Show total bill amount for each table
- Quick "Generate Bill" button
- Quick "Settle Payment" button

Use Angular Material:
- mat-dialog for payment dialogs
- mat-table for itemized lists
- mat-form-field for inputs
- mat-radio-group for payment methods
- Print CSS for invoice printing

Include receipt printer integration (if hardware available).
```

**Expected Output:** Complete billing and payment interface

---

## 📊 PHASE 7: Admin Dashboard & Analytics (Day 10)

### Step 7.1: Backend - Analytics APIs

**Prompt for Claude Code:**
```
Create analytics and reporting system:

1. CREATE AnalyticsService:
```java
Methods:
- getDashboardStats(LocalDate date): DashboardStatsDTO
  - Total orders
  - Total revenue
  - Average order value
  - Top-selling items
  - Orders by status
  - Orders by type (dine-in, takeout, delivery)
  
- getSalesReport(LocalDate startDate, LocalDate endDate): SalesReportDTO
  - Daily sales breakdown
  - Revenue by category
  - Revenue by payment method
  - Hour-by-hour sales (peak hours)
  
- getMenuAnalytics(): MenuAnalyticsDTO
  - Top 10 selling items
  - Bottom 10 selling items
  - Revenue by category
  - Average price by category
  
- getCustomerAnalytics(): CustomerAnalyticsDTO
  - New customers this month
  - Repeat customers
  - Customer lifetime value
  - Average order frequency
  
- getInventoryReport(): InventoryReportDTO
  - Low stock items
  - Stock value
  - Waste tracking
  
- getStaffPerformance(): StaffPerformanceDTO
  - Orders handled per staff
  - Average order time
  - Customer ratings

- exportReport(ReportType, format): byte[]
  - Export as PDF, Excel, or CSV
```

2. CREATE AnalyticsController:
```java
Endpoints:
- GET /api/analytics/dashboard
- GET /api/analytics/sales?start={date}&end={date}
- GET /api/analytics/menu
- GET /api/analytics/customers
- GET /api/analytics/inventory
- GET /api/analytics/staff
- GET /api/analytics/export?type={}&format={}
```

3. CREATE report generation:
- PDF reports using iText
- Excel reports using Apache POI
- CSV export
- Charts and graphs data for frontend

Include caching for frequently accessed analytics.
```

**Expected Output:** Complete analytics backend

---

### Step 7.2: Frontend - Admin Dashboard

**Prompt for Claude Code:**
```
Create comprehensive admin dashboard in admin-app:

1. CREATE AnalyticsService in shared library:
```typescript
Methods:
- getDashboardStats(): Observable<DashboardStats>
- getSalesReport(startDate, endDate): Observable<SalesReport>
- getMenuAnalytics(): Observable<MenuAnalytics>
- exportReport(type, format): Observable<Blob>
```

2. CREATE dashboard feature module:
```
ng generate module features/dashboard --routing
ng generate component features/dashboard/dashboard-home
ng generate component features/dashboard/stats-card
ng generate component features/dashboard/sales-chart
ng generate component features/dashboard/revenue-chart
ng generate component features/dashboard/top-items-chart
ng generate component features/dashboard/orders-timeline
ng generate component features/dashboard/quick-stats
```

3. CREATE dashboard-home component:
LAYOUT (Grid):
Row 1: Quick Stats Cards
- Total Orders Today (with % change)
- Total Revenue Today (with % change)
- Average Order Value
- Active Tables

Row 2: Charts
- Sales Overview (line chart, last 7 days)
- Revenue by Category (pie chart)

Row 3:
- Top Selling Items (horizontal bar chart)
- Recent Orders (table)

Row 4:
- Orders by Hour (bar chart showing peak times)
- Payment Methods Distribution (donut chart)

Sidebar:
- Date range selector
- Quick filters
- Export button
- Refresh button

4. CREATE stats-card component:
DISPLAY:
- Icon
- Title
- Main value (large number)
- Change indicator (↑↓ with %)
- Subtitle (comparison period)

VARIANTS:
- Success (green) - positive metrics
- Warning (orange) - attention needed
- Info (blue) - neutral
- Error (red) - negative metrics

5. CREATE sales-chart component:
- Line chart showing sales over time
- Use Chart.js or ng2-charts
- Multiple lines:
  - Total revenue
  - Number of orders
  - Average order value
- Interactive tooltips
- Zoom and pan
- Export chart as image

6. CREATE revenue-chart component:
- Pie or donut chart
- Revenue breakdown by:
  - Menu category
  - Order type (dine-in, takeout, delivery)
  - Payment method
- Interactive legend
- Click to drill down

7. CREATE top-items-chart component:
- Horizontal bar chart
- Top 10 selling items
- Show quantity sold and revenue
- Different colors for each item
- Click to view item details

8. CREATE orders-timeline component:
- Table showing recent orders:
  - Order ID
  - Time
  - Customer
  - Items count
  - Amount
  - Status badge
- Click to view order details
- Pagination
- Auto-refresh every 30 seconds

9. CREATE quick-stats component:
Additional metrics:
- Tables occupied / total
- Kitchen efficiency (avg prep time)
- Customer satisfaction (if reviews enabled)
- Inventory alerts count

10. ADD filtering:
- Date range picker (today, yesterday, last 7 days, last 30 days, custom)
- Restaurant selector (if multi-restaurant)
- Order type filter
- Export filtered data

11. CREATE reports page:
```
ng generate component features/dashboard/reports
```

FEATURES:
- Report type selector:
  - Sales report
  - Menu performance
  - Customer analytics
  - Inventory report
  - Staff performance
  
- Date range selector
- Export format (PDF, Excel, CSV)
- Generate report button
- Download reports
- Scheduled reports (email daily/weekly)

Use:
- Chart.js or ng2-charts for visualizations
- Angular Material for UI components
- RxJS for real-time updates
- Responsive grid layout

Include loading states, error handling, and empty states.
```

**Expected Output:** Complete admin dashboard with analytics

---

## 🔄 PHASE 8: Real-time Features & WebSocket (Day 11)

### Prompt for Claude Code:
```
Implement complete real-time functionality using WebSocket:

BACKEND (Spring Boot):

1. UPDATE WebSocket configuration:
- Configure STOMP over WebSocket
- Set application destination prefix: /app
- Set broker destination prefix: /topic
- Enable SockJS fallback

2. CREATE NotificationService:
```java
Methods:
- sendOrderUpdate(Order order, String event)
  - Send to /topic/orders/all
  - Send to /topic/orders/{restaurantId}
  - Send to /topic/kitchen/{restaurantId}
  - Send to /topic/customer/{customerId}
  
- sendTableUpdate(Table table)
  - Send to /topic/tables/{restaurantId}
  
- sendKitchenNotification(Order order)
  - Send to /topic/kitchen/{restaurantId}
  
- sendCustomerNotification(Long customerId, Notification notification)
  - Send to /topic/customer/{customerId}
```

3. UPDATE OrderService to send real-time updates:
- On order created → send to kitchen and customer
- On status change → send to waiter and customer
- On order ready → send to waiter

4. CREATE WebSocket controller:
```java
@MessageMapping("/orders")
@SendTo("/topic/orders")
```

FRONTEND (Angular):

1. CREATE WebSocketService in shared library:
```typescript
Methods:
- connect(): void
- disconnect(): void
- subscribeToOrders(): Observable<OrderUpdate>
- subscribeToKitchen(): Observable<OrderUpdate>
- subscribeToTables(): Observable<TableUpdate>
- subscribeToCustomerNotifications(customerId): Observable<Notification>
- sendMessage(destination, payload): void
```

2. INSTALL dependencies:
```bash
npm install @stomp/ng2-stompjs @stomp/stompjs sockjs-client
```

3. UPDATE components to use WebSocket:

KITCHEN APP:
- Subscribe to /topic/kitchen/{restaurantId}
- Auto-update order list when new orders arrive
- Show notification toast for new orders
- Play sound alert
- Flash order card when status changes

WAITER APP:
- Subscribe to /topic/orders/{restaurantId}
- Update table status in real-time
- Show notification when order is ready
- Auto-refresh order list

CUSTOMER APP:
- Subscribe to /topic/customer/{customerId}
- Show order status updates in real-time
- Track delivery location (if integrated)
- Show estimated time updates

ADMIN APP:
- Subscribe to /topic/orders/all
- Real-time dashboard updates
- Live sales counter
- Active orders count

4. ADD notification system:
- Toast notifications for important events
- Sound alerts (configurable)
- Badge counts for unread notifications
- Notification history

5. HANDLE connection states:
- Show "Connecting..." indicator
- Show "Connected" indicator (green dot)
- Show "Disconnected" warning (red dot)
- Auto-reconnect on connection loss
- Queue messages while disconnected

Include error handling and reconnection logic.
```

**Expected Output:** Complete real-time communication system

---

## 🚀 PHASE 9: Testing & Deployment (Days 12-14)

### Step 9.1: Testing

**Prompt for Claude Code:**
```
Create comprehensive testing suite:

BACKEND TESTING:

1. CREATE unit tests for services:
- MenuService test
- OrderService test
- AuthService test
- PaymentService test

Use JUnit 5 and Mockito:
```java
@ExtendWith(MockitoExtension.class)
class MenuServiceTest {
    @Mock
    private MenuRepository menuRepository;
    
    @InjectMocks
    private MenuService menuService;
    
    @Test
    void shouldCreateMenuItem() {
        // Test implementation
    }
}
```

2. CREATE integration tests:
- Test controller endpoints
- Test database operations
- Use @SpringBootTest
- Use TestRestTemplate
- Use H2 in-memory database for tests

3. CREATE test data:
- Create data.sql with test data
- Use @Sql annotation to load test data

FRONTEND TESTING:

1. CREATE unit tests for services:
- AuthService spec
- MenuService spec
- CartService spec

Use Jasmine and Karma:
```typescript
describe('MenuService', () => {
  let service: MenuService;
  let httpMock: HttpTestingController;
  
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [MenuService]
    });
    service = TestBed.inject(MenuService);
    httpMock = TestBed.inject(HttpTestingController);
  });
  
  it('should fetch menu items', () => {
    // Test implementation
  });
});
```

2. CREATE component tests:
- Test user interactions
- Test form validation
- Test navigation

3. CREATE E2E tests using Cypress:
- User login flow
- Browse menu and add to cart
- Checkout process
- Place order

Install Cypress:
```bash
npm install --save-dev cypress
```

Create tests:
```javascript
describe('Order Flow', () => {
  it('should complete checkout', () => {
    cy.visit('/menu');
    cy.get('.menu-item').first().click();
    cy.get('.add-to-cart').click();
    cy.get('.cart-icon').click();
    cy.get('.checkout-btn').click();
    // ... continue test
  });
});
```

Run tests:
```bash
# Backend
mvn test

# Frontend
ng test
ng e2e
```
```

**Expected Output:** Complete test suites

---

### Step 9.2: Production Build & Deployment

**Prompt for Claude Code:**
```
Prepare application for production deployment:

BACKEND:

1. UPDATE application.yml for production:
```yaml
spring:
  profiles: prod
  datasource:
    url: ${DATABASE_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate  # Don't auto-create in prod
    show-sql: false
  
server:
  port: ${PORT:8080}
  
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000
```

2. CREATE Dockerfile for backend:
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

3. CREATE production build script:
```bash
mvn clean package -DskipTests
```

FRONTEND:

1. UPDATE environment.prod.ts:
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://your-api-domain.com/api',
  wsUrl: 'wss://your-api-domain.com/ws'
};
```

2. CREATE production build:
```bash
# For each app
ng build customer-app --configuration production
ng build waiter-app --configuration production
ng build kitchen-app --configuration production
ng build admin-app --configuration production
```

3. CREATE Dockerfile for frontend:
```dockerfile
FROM nginx:alpine
COPY dist/customer-app /usr/share/nginx/html/customer
COPY dist/waiter-app /usr/share/nginx/html/waiter
COPY dist/kitchen-app /usr/share/nginx/html/kitchen
COPY dist/admin-app /usr/share/nginx/html/admin
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
```

4. CREATE nginx.conf:
```nginx
server {
  listen 80;
  
  location /customer {
    alias /usr/share/nginx/html/customer;
    try_files $uri $uri/ /customer/index.html;
  }
  
  location /waiter {
    alias /usr/share/nginx/html/waiter;
    try_files $uri $uri/ /waiter/index.html;
  }
  
  location /kitchen {
    alias /usr/share/nginx/html/kitchen;
    try_files $uri $uri/ /kitchen/index.html;
  }
  
  location /admin {
    alias /usr/share/nginx/html/admin;
    try_files $uri $uri/ /admin/index.html;
  }
}
```

DEPLOYMENT OPTIONS:

1. FREE HOSTING - Railway:
```bash
# Install Railway CLI
npm install -g railway

# Login
railway login

# Deploy backend
cd backend
railway init
railway up

# Deploy frontend
cd frontend
railway init
railway up
```

2. FREE HOSTING - Render:
- Create account at render.com
- Connect GitHub repository
- Create new Web Service
- Select Docker or Node.js
- Deploy!

3. DATABASE - Free PostgreSQL:
- Railway: Built-in PostgreSQL
- Render: Free PostgreSQL (limited)
- ElephantSQL: Free tier

4. CREATE docker-compose.yml for local testing:
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: restaurant_db
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin123
    ports:
      - "5432:5432"
  
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    depends_on:
      - postgres
    environment:
      DATABASE_URL: jdbc:postgresql://postgres:5432/restaurant_db
  
  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend
```

5. CREATE deployment scripts:

deploy.sh:
```bash
#!/bin/bash

echo "Building backend..."
cd backend
mvn clean package -DskipTests

echo "Building frontend..."
cd ../frontend
ng build customer-app --configuration production
ng build waiter-app --configuration production
ng build kitchen-app --configuration production
ng build admin-app --configuration production

echo "Building Docker images..."
docker-compose build

echo "Deploying..."
docker-compose up -d

echo "Deployment complete!"
```

6. CREATE CI/CD with GitHub Actions:

.github/workflows/deploy.yml:
```yaml
name: Deploy

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Setup Java
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      
      - name: Build backend
        run: |
          cd backend
          mvn clean package -DskipTests
      
      - name: Setup Node
        uses: actions/setup-node@v2
        with:
          node-version: '18'
      
      - name: Build frontend
        run: |
          cd frontend
          npm install
          npm run build:prod
      
      - name: Deploy
        run: |
          # Your deployment commands
```

SECURITY CHECKLIST:

- [ ] Change all default passwords
- [ ] Set strong JWT secret
- [ ] Enable HTTPS (Let's Encrypt)
- [ ] Configure CORS properly
- [ ] Enable rate limiting
- [ ] Set up firewall rules
- [ ] Regular backups
- [ ] Environment variables for secrets
- [ ] SQL injection prevention
- [ ] XSS protection

MONITORING:

1. Setup logging:
- Backend: Logback configuration
- Frontend: Sentry for error tracking

2. Setup monitoring:
- Uptime monitoring (UptimeRobot - free)
- Application performance monitoring
- Database monitoring

3. Setup alerts:
- Email alerts for errors
- Slack notifications for critical issues
```

**Expected Output:** Production-ready application with deployment configuration

---

## 📚 Additional Features (Optional - Days 15+)

### Prompt for Claude Code:
```
Add these advanced features:

1. MULTI-LANGUAGE SUPPORT:
- i18n implementation in Angular
- Language selector
- Translate menu items, UI labels
- Support for English, Hindi, Spanish (or your choice)

2. LOYALTY PROGRAM:
- Points accumulation on orders
- Rewards redemption
- Tier system (Silver, Gold, Platinum)
- Birthday rewards

3. TABLE QR CODES:
- Generate unique QR code for each table
- Scan QR to view menu
- Self-ordering from table
- Call waiter feature

4. DELIVERY INTEGRATION:
- Google Maps integration
- Real-time delivery tracking
- Delivery partner assignment
- Estimated delivery time

5. REVIEW & RATINGS:
- Customer reviews for menu items
- Overall restaurant rating
- Photo uploads
- Response from management

6. MOBILE APPS:
- Convert Angular apps to mobile using Ionic
- Push notifications
- Offline mode
- Camera integration for food photos

7. ADVANCED ANALYTICS:
- Predictive analytics (forecast sales)
- Inventory optimization
- Menu optimization (suggest removals/additions)
- Customer segmentation

8. MARKETING FEATURES:
- Email campaigns
- SMS marketing
- WhatsApp integration
- Social media integration


9. STAFF FEATURES:
- Time tracking
- Tip distribution
- Performance reviews
- Training modules

10. AI FEATURES:
- Chatbot for customer support
- AI-powered menu recommendations
- Voice ordering
- Image recognition for food
```

---

## 🎓 Learning Resources While Building

As you build, learn from:
- Official Angular docs: https://angular.io
- Official Spring Boot docs: https://spring.io
- PostgreSQL tutorial: https://www.postgresqltutorial.com/
- YouTube channels for tutorials
- Stack Overflow for problems

---

## ✅ Daily Checklist

**End of each day:**
- [ ] Commit code to Git
- [ ] Test what you built
- [ ] Document any issues
- [ ] Plan next day's tasks
- [ ] Take a break! 😊

---

**Remember**: Don't rush! It's better to build one feature well than to build everything poorly.

Good luck! 🚀