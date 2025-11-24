-- Add currency column to orders table
ALTER TABLE orders
    ADD COLUMN currency VARCHAR(10);

-- Update existing orders with currency from their first order item
UPDATE orders o
SET currency = (
    SELECT oi.currency
    FROM order_item oi
    WHERE oi.order_id = o.order_id
    LIMIT 1
);

-- Make the column NOT NULL after populating existing data
ALTER TABLE orders
    ALTER COLUMN currency SET NOT NULL;
