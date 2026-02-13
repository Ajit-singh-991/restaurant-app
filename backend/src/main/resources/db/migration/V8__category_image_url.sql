-- Add optional image URL to categories for category icons/banners
ALTER TABLE categories ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);
