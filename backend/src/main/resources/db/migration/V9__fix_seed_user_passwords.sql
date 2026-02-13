-- Fix seed user passwords: set to BCrypt hash of 'password123' (generated with BCryptPasswordEncoder, 10 rounds).
-- The original V2 hash was for a different password, causing login to fail with admin/password123.
UPDATE users
SET password = '$2a$10$xZdYMYi26Ei7rYYu8S/0gO/cufrmbA24DKRq3v/jBQKomOoASS/ne'
WHERE username IN ('admin', 'manager1', 'waiter1', 'waiter2', 'kitchen1', 'customer1', 'customer2', 'customer3', 'customer4', 'customer5');
