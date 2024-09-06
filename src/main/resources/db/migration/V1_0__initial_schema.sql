CREATE TABLE users (
                       user_id SERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       first_name VARCHAR(50),
                       last_name VARCHAR(50),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
                            category_id SERIAL PRIMARY KEY,
                            category_name VARCHAR(100) NOT NULL UNIQUE,
                            parent_category_id INT,
                            FOREIGN KEY (parent_category_id) REFERENCES categories(category_id)
);

CREATE TABLE product (
                         product_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                         product_name VARCHAR(100) NOT NULL,
                         description TEXT,
                         price NUMERIC(10, 2) NOT NULL,
                         currency VARCHAR NOT NULL,
                         imageId INT,
                         category_id INT,
                         supplier VARCHAR(50),
                         stock INT DEFAULT 0,
                         size VARCHAR(255),
                         FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

CREATE TABLE product_images (
                                image_id SERIAL PRIMARY KEY,
                                product_id uuid,
                                image_url VARCHAR(255) NOT NULL,
                                FOREIGN KEY (product_id) REFERENCES product(product_id)
);

CREATE TABLE orders (
                        order_id SERIAL PRIMARY KEY,
                        user_id INT,
                        order_status VARCHAR(50) NOT NULL,
                        total_amount NUMERIC(10, 2) NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE order_items (
                             order_item_id SERIAL PRIMARY KEY,
                             order_id INT,
                             product_id uuid,
                             quantity INT NOT NULL,
                             price NUMERIC(10, 2) NOT NULL,
                             FOREIGN KEY (order_id) REFERENCES orders(order_id),
                             FOREIGN KEY (product_id) REFERENCES product(product_id)
);

CREATE TABLE shopping_cart (
                               cart_id SERIAL PRIMARY KEY,
                               user_id INT,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE cart_items (
                            cart_item_id SERIAL PRIMARY KEY,
                            cart_id INT,
                            product_id uuid,
                            quantity INT NOT NULL,
                            FOREIGN KEY (cart_id) REFERENCES shopping_cart(cart_id),
                            FOREIGN KEY (product_id) REFERENCES product(product_id)
);

CREATE TABLE reviews (
                         review_id SERIAL PRIMARY KEY,
                         product_id uuid,
                         user_id INT,
                         rating INT CHECK (rating >= 1 AND rating <= 5),
                         comment TEXT,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (product_id) REFERENCES product(product_id),
                         FOREIGN KEY (user_id) REFERENCES users(user_id)
);
