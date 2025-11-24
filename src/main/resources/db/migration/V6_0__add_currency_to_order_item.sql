-- Add currency column to order_item table
ALTER TABLE order_item
    ADD COLUMN currency VARCHAR(10);

-- Update existing order items with currency from their products
UPDATE order_item oi
SET currency = p.currency::VARCHAR
FROM product p
WHERE oi.product_id = p.product_id;

-- Make the column NOT NULL after populating existing data
ALTER TABLE order_item
    ALTER COLUMN currency SET NOT NULL;
