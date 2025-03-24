-- Set the schema to public explicitly
SET search_path TO public;

-- Users table
CREATE TABLE users (
                       user_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                       user_name VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Roles table
CREATE TABLE role (
                      role_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                      name VARCHAR(50) NOT NULL UNIQUE
);

-- User Roles (Many-to-Many)
CREATE TABLE user_role (
                           user_id UUID,
                           role_id UUID,
                           FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
                           FOREIGN KEY (role_id) REFERENCES role (role_id) ON DELETE CASCADE,
                           UNIQUE (user_id, role_id)
);

-- Categories table
CREATE TABLE category (
                          category_id SERIAL PRIMARY KEY,
                          category_name VARCHAR(100) NOT NULL UNIQUE
);

-- Subcategories table
CREATE TABLE subcategory (
                             subcategory_id SERIAL PRIMARY KEY,
                             subcategory_name VARCHAR(100) NOT NULL UNIQUE,
                             category_id INT NOT NULL,
                             FOREIGN KEY (category_id) REFERENCES category(category_id) ON DELETE CASCADE
);

-- Products table
CREATE TABLE product (
                         product_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                         product_name VARCHAR(100) NOT NULL,
                         description TEXT,
                         price NUMERIC(10, 2) NOT NULL,
                         currency VARCHAR(10) NOT NULL,
                         image_id BIGINT,
                         subcategory_id INT NOT NULL,
                         supplier VARCHAR(50),
                         stock BIGINT DEFAULT 0,
                         size VARCHAR(10),
                         version BIGINT,
                         FOREIGN KEY (subcategory_id) REFERENCES subcategory(subcategory_id)
);

-- Images table (independent of products)
CREATE TABLE product_image (
                               image_id SERIAL PRIMARY KEY,
                               image_url VARCHAR(255) NOT NULL,
                               alt_text VARCHAR(255)
);

-- Many-to-many mapping table with display order
CREATE TABLE product_image_mapping (
                                       product_id UUID,
                                       image_id INT,
                                       is_primary BOOLEAN DEFAULT FALSE,
                                       display_order INT DEFAULT 0,
                                       PRIMARY KEY (product_id, image_id),
                                       FOREIGN KEY (product_id) REFERENCES product (product_id) ON DELETE CASCADE,
                                       FOREIGN KEY (image_id) REFERENCES product_image (image_id) ON DELETE CASCADE
);

-- Orders table
CREATE TABLE orders (
                        order_id SERIAL PRIMARY KEY,
                        user_id UUID NOT NULL,
                        order_status VARCHAR(50) NOT NULL,
                        total_amount NUMERIC(10, 2) NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Order Items (Many-to-Many)
CREATE TABLE order_item (
                            order_item_id SERIAL PRIMARY KEY,
                            order_id INT NOT NULL,
                            product_id UUID NOT NULL,
                            quantity INT NOT NULL CHECK (quantity > 0),
                            price NUMERIC(10, 2) NOT NULL,
                            FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
                            FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE
);

-- Shopping Cart
CREATE TABLE shopping_cart (
                               cart_id SERIAL PRIMARY KEY,
                               user_id UUID NOT NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Shopping Cart Items
CREATE TABLE cart_item (
                           cart_item_id SERIAL PRIMARY KEY,
                           cart_id INT NOT NULL,
                           product_id UUID NOT NULL,
                           quantity INT NOT NULL CHECK (quantity > 0),
                           total_amount NUMERIC(10, 2) NOT NULL,
                           FOREIGN KEY (cart_id) REFERENCES shopping_cart(cart_id) ON DELETE CASCADE,
                           FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE
);

-- Reviews table
CREATE TABLE review (
                        review_id SERIAL PRIMARY KEY,
                        product_id UUID NOT NULL,
                        user_id UUID NOT NULL,
                        rating INT CHECK (rating >= 1 AND rating <= 5),
                        comment TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (product_id) REFERENCES product (product_id) ON DELETE CASCADE,
                        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Unique constraints for product details
ALTER TABLE product
    ADD CONSTRAINT unique_product_constraint
        UNIQUE (product_name, description, currency, image_id, subcategory_id, supplier, stock, size);
