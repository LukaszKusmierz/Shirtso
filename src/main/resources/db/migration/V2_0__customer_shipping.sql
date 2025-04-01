-- Create address table
CREATE TABLE address (
                         address_id SERIAL PRIMARY KEY,
                         user_id UUID NOT NULL,
                         full_name VARCHAR(100) NOT NULL,
                         street_address VARCHAR(255) NOT NULL,
                         city VARCHAR(100) NOT NULL,
                         postal_code VARCHAR(20) NOT NULL,
                         country VARCHAR(100) NOT NULL,
                         phone VARCHAR(20),
                         is_default BOOLEAN NOT NULL DEFAULT FALSE,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Create shipping_method table
CREATE TABLE shipping_method (
                                 shipping_method_id SERIAL PRIMARY KEY,
                                 name VARCHAR(100) NOT NULL,
                                 description TEXT NOT NULL,
                                 price NUMERIC(10, 2) NOT NULL,
                                 estimated_delivery_days INT,
                                 is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create promo_code table
CREATE TABLE promo_code (
                            promo_code_id SERIAL PRIMARY KEY,
                            code VARCHAR(20) NOT NULL UNIQUE,
                            description TEXT NOT NULL,
                            discount_type VARCHAR(20) NOT NULL,
                            discount_value NUMERIC(10, 2) NOT NULL,
                            minimum_order_value NUMERIC(10, 2),
                            maximum_discount_amount NUMERIC(10, 2),
                            start_date TIMESTAMP NOT NULL,
                            end_date TIMESTAMP NOT NULL,
                            usage_limit INT,
                            usage_count INT NOT NULL DEFAULT 0,
                            is_active BOOLEAN NOT NULL DEFAULT TRUE,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Alter orders table to add shipping, discount, tax and shipping address columns
ALTER TABLE orders
    ADD COLUMN subtotal_amount NUMERIC(10, 2) NOT NULL DEFAULT 0,
    ADD COLUMN shipping_amount NUMERIC(10, 2) DEFAULT 0,
    ADD COLUMN discount_amount NUMERIC(10, 2) DEFAULT 0,
    ADD COLUMN tax_amount NUMERIC(10, 2) DEFAULT 0,
    ADD COLUMN promo_code VARCHAR(20),
    ADD COLUMN shipping_method_id INT,
    ADD COLUMN address_id INT,
    ADD FOREIGN KEY (shipping_method_id) REFERENCES shipping_method(shipping_method_id),
    ADD FOREIGN KEY (address_id) REFERENCES address(address_id);

-- Update existing orders to set subtotal_amount equal to total_amount
UPDATE orders SET subtotal_amount = total_amount;
