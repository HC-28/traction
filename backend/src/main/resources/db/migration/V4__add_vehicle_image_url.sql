-- V4: Add image_url column to vehicles table
-- Stores the Cloudinary secure_url for each vehicle's photo.

ALTER TABLE vehicles ADD COLUMN image_url VARCHAR(512);
