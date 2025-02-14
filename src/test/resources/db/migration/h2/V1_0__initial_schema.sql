CREATE TABLE users (
                       user_id UUID DEFAULT random_uuid() PRIMARY KEY,
                       user_name VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE role (
                      role_id   UUID DEFAULT random_uuid() PRIMARY KEY,
                      name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE user_role (
                           user_id UUID,
                           role_id UUID,
                           FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
                           FOREIGN KEY (role_id) REFERENCES role (role_id) ON DELETE CASCADE,
                           UNIQUE (user_id, role_id)
);

CREATE TABLE category (
                          category_id IDENTITY PRIMARY KEY,
                          category_name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE subcategory (
                             subcategory_id IDENTITY PRIMARY KEY,
                             subcategory_name VARCHAR(100) NOT NULL UNIQUE,
                             category_id INT NOT NULL,
                             FOREIGN KEY (category_id) REFERENCES category(category_id) ON DELETE CASCADE
);

CREATE TABLE product (
                         product_id UUID DEFAULT random_uuid() PRIMARY KEY,
                         product_name VARCHAR(100) NOT NULL,
                         description TEXT,
                         price NUMERIC(10, 2) NOT NULL,
                         currency VARCHAR NOT NULL,
                         image_id INT,
                         subcategory_id INT,
                         supplier VARCHAR(50),
                         stock INT DEFAULT 0,
                         size VARCHAR(255),
                         version BIGINT,
                         FOREIGN KEY (subcategory_id) REFERENCES subcategory(subcategory_id)
);

CREATE TABLE product_image (
                                image_id IDENTITY PRIMARY KEY,
                                product_id uuid,
                                image_url VARCHAR(255) NOT NULL,
                                FOREIGN KEY (product_id) REFERENCES product (product_id) ON DELETE CASCADE
);

CREATE TABLE orders (
                        order_id IDENTITY PRIMARY KEY,
                        user_id UUID NOT NULL,
                        order_status VARCHAR(50) NOT NULL,
                        total_amount NUMERIC(10, 2) NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE order_item (
                            order_item_id IDENTITY PRIMARY KEY,
                            order_id INT NOT NULL,
                            product_id UUID NOT NULL,
                            quantity INT NOT NULL CHECK (quantity > 0),
                            price NUMERIC(10, 2) NOT NULL,
                            FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
                            FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE
);

CREATE TABLE shopping_cart (
                               cart_id IDENTITY PRIMARY KEY,
                               user_id UUID NOT NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE cart_item (
                           cart_item_id IDENTITY PRIMARY KEY,
                           cart_id INT NOT NULL,
                           product_id UUID NOT NULL,
                           quantity INT NOT NULL CHECK (quantity > 0),
                           total_amount NUMERIC(10, 2) NOT NULL,
                           FOREIGN KEY (cart_id) REFERENCES shopping_cart(cart_id) ON DELETE CASCADE,
                           FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE
);

CREATE TABLE review (
                         review_id  IDENTITY PRIMARY KEY,
                         product_id uuid,
                         user_id    uuid,
                         rating     INT CHECK (rating >= 1 AND rating <= 5),
                         comment    TEXT,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (product_id) REFERENCES product (product_id),
                         FOREIGN KEY (user_id) REFERENCES users(user_id)
);

ALTER TABLE product
    ADD CONSTRAINT unique_product_constraint
        UNIQUE (product_name, description, currency, image_id, subcategory_id, supplier, stock, size);
