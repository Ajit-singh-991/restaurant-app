-- Seed Data for Development/Testing
-- Passwords are BCrypt-hashed version of 'password123'

-- =============================================
-- USERS
-- =============================================
INSERT INTO users (username, password, full_name, email, phone, role) VALUES
('admin', '$2a$10$xZdYMYi26Ei7rYYu8S/0gO/cufrmbA24DKRq3v/jBQKomOoASS/ne', 'System Admin', 'admin@restaurant.com', '1234567890', 'ADMIN'),
('manager1', '$2a$10$xZdYMYi26Ei7rYYu8S/0gO/cufrmbA24DKRq3v/jBQKomOoASS/ne', 'Rahul Sharma', 'rahul@restaurant.com', '1234567891', 'MANAGER'),
('waiter1', '$2a$10$xZdYMYi26Ei7rYYu8S/0gO/cufrmbA24DKRq3v/jBQKomOoASS/ne', 'Priya Patel', 'priya@restaurant.com', '1234567892', 'WAITER'),
('waiter2', '$2a$10$xZdYMYi26Ei7rYYu8S/0gO/cufrmbA24DKRq3v/jBQKomOoASS/ne', 'Amit Kumar', 'amit@restaurant.com', '1234567893', 'WAITER'),
('kitchen1', '$2a$10$xZdYMYi26Ei7rYYu8S/0gO/cufrmbA24DKRq3v/jBQKomOoASS/ne', 'Chef Vikram', 'vikram@restaurant.com', '1234567894', 'KITCHEN'),
('customer1', '$2a$10$xZdYMYi26Ei7rYYu8S/0gO/cufrmbA24DKRq3v/jBQKomOoASS/ne', 'Anita Desai', 'anita@gmail.com', '9876543210', 'CUSTOMER'),
('customer2', '$2a$10$xZdYMYi26Ei7rYYu8S/0gO/cufrmbA24DKRq3v/jBQKomOoASS/ne', 'Rohan Mehta', 'rohan@gmail.com', '9876543211', 'CUSTOMER'),
('customer3', '$2a$10$xZdYMYi26Ei7rYYu8S/0gO/cufrmbA24DKRq3v/jBQKomOoASS/ne', 'Sneha Reddy', 'sneha@gmail.com', '9876543212', 'CUSTOMER'),
('customer4', '$2a$10$xZdYMYi26Ei7rYYu8S/0gO/cufrmbA24DKRq3v/jBQKomOoASS/ne', 'Karan Singh', 'karan@gmail.com', '9876543213', 'CUSTOMER'),
('customer5', '$2a$10$xZdYMYi26Ei7rYYu8S/0gO/cufrmbA24DKRq3v/jBQKomOoASS/ne', 'Meera Joshi', 'meera@gmail.com', '9876543214', 'CUSTOMER');

-- =============================================
-- CATEGORIES
-- =============================================
INSERT INTO categories (name, description, display_order, active) VALUES
('Starters', 'Appetizers and small bites to begin your meal', 1, TRUE),
('Main Course', 'Hearty main dishes', 2, TRUE),
('Breads', 'Fresh baked breads from our tandoor', 3, TRUE),
('Rice & Biryani', 'Fragrant rice dishes', 4, TRUE),
('Desserts', 'Sweet endings to your meal', 5, TRUE),
('Beverages', 'Refreshing drinks', 6, TRUE);

-- =============================================
-- MENU ITEMS
-- =============================================
INSERT INTO menu_items (name, description, price, category_id, available, preparation_time_minutes, vegetarian, vegan, gluten_free) VALUES
-- Starters
('Paneer Tikka', 'Marinated cottage cheese grilled in tandoor', 249.00, 1, TRUE, 15, TRUE, FALSE, TRUE),
('Chicken Seekh Kebab', 'Minced chicken kebab with aromatic spices', 299.00, 1, TRUE, 20, FALSE, FALSE, TRUE),
('Samosa (2 pcs)', 'Crispy pastry filled with spiced potatoes', 99.00, 1, TRUE, 10, TRUE, TRUE, FALSE),
('Tandoori Chicken', 'Half chicken marinated in yogurt and spices', 349.00, 1, TRUE, 25, FALSE, FALSE, TRUE),

-- Main Course
('Butter Chicken', 'Tender chicken in creamy tomato gravy', 399.00, 2, TRUE, 20, FALSE, FALSE, TRUE),
('Paneer Butter Masala', 'Cottage cheese in rich buttery gravy', 349.00, 2, TRUE, 15, TRUE, FALSE, TRUE),
('Dal Makhani', 'Slow-cooked black lentils in cream', 249.00, 2, TRUE, 15, TRUE, FALSE, TRUE),
('Mutton Rogan Josh', 'Kashmiri style slow-cooked mutton', 499.00, 2, TRUE, 30, FALSE, FALSE, TRUE),
('Chole Bhature', 'Spiced chickpeas with fried bread', 199.00, 2, TRUE, 15, TRUE, TRUE, FALSE),
('Fish Curry', 'Fresh fish in coconut-based curry', 449.00, 2, TRUE, 20, FALSE, FALSE, TRUE),

-- Breads
('Butter Naan', 'Soft bread brushed with butter', 59.00, 3, TRUE, 5, TRUE, FALSE, FALSE),
('Garlic Naan', 'Naan topped with garlic and cilantro', 69.00, 3, TRUE, 5, TRUE, FALSE, FALSE),
('Tandoori Roti', 'Whole wheat bread from tandoor', 39.00, 3, TRUE, 5, TRUE, TRUE, FALSE),
('Laccha Paratha', 'Layered flaky bread', 59.00, 3, TRUE, 8, TRUE, FALSE, FALSE),

-- Rice & Biryani
('Chicken Biryani', 'Fragrant basmati rice with spiced chicken', 349.00, 4, TRUE, 25, FALSE, FALSE, TRUE),
('Veg Biryani', 'Fragrant basmati rice with mixed vegetables', 249.00, 4, TRUE, 25, TRUE, TRUE, TRUE),
('Jeera Rice', 'Cumin-flavored basmati rice', 149.00, 4, TRUE, 10, TRUE, TRUE, TRUE),

-- Desserts
('Gulab Jamun (2 pcs)', 'Deep-fried milk dumplings in sugar syrup', 129.00, 5, TRUE, 5, TRUE, FALSE, FALSE),
('Rasmalai', 'Soft paneer patties in sweetened milk', 149.00, 5, TRUE, 5, TRUE, FALSE, TRUE),
('Kulfi', 'Traditional Indian ice cream', 99.00, 5, TRUE, 5, TRUE, FALSE, TRUE),

-- Beverages
('Masala Chai', 'Spiced Indian tea', 49.00, 6, TRUE, 5, TRUE, TRUE, TRUE),
('Mango Lassi', 'Yogurt-based mango smoothie', 99.00, 6, TRUE, 5, TRUE, FALSE, TRUE),
('Fresh Lime Soda', 'Refreshing lime drink', 69.00, 6, TRUE, 3, TRUE, TRUE, TRUE);

-- =============================================
-- RESTAURANT TABLES
-- =============================================
INSERT INTO restaurant_tables (table_number, capacity, status, section) VALUES
(1, 2, 'AVAILABLE', 'Indoor'),
(2, 2, 'AVAILABLE', 'Indoor'),
(3, 4, 'AVAILABLE', 'Indoor'),
(4, 4, 'AVAILABLE', 'Indoor'),
(5, 6, 'AVAILABLE', 'Indoor'),
(6, 6, 'AVAILABLE', 'Indoor'),
(7, 4, 'AVAILABLE', 'Outdoor'),
(8, 4, 'AVAILABLE', 'Outdoor'),
(9, 8, 'AVAILABLE', 'Private'),
(10, 10, 'AVAILABLE', 'Private');
