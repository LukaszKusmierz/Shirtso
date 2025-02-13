CREATE TABLE users (
                       user_id UUID DEFAULT random_uuid() PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       first_name VARCHAR(50),
                       last_name VARCHAR(50),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE category (
                          category_id IDENTITY PRIMARY KEY,
                          category_name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE product (
                         product_id UUID DEFAULT random_uuid() PRIMARY KEY,
                         product_name VARCHAR(100) NOT NULL,
                         description TEXT,
                         price NUMERIC(10, 2) NOT NULL,
                         currency VARCHAR NOT NULL,
                         image_id INT,
                         category_id INT,
                         supplier VARCHAR(50),
                         stock INT DEFAULT 0,
                         size VARCHAR(255),
                         FOREIGN KEY (category_id) REFERENCES category(category_id)
);

CREATE TABLE product_image (
                                image_id IDENTITY PRIMARY KEY,
                                product_id uuid,
                                image_url VARCHAR(255) NOT NULL,
                                FOREIGN KEY (product_id) REFERENCES product (product_id)
);

CREATE TABLE orders (
                        order_id IDENTITY PRIMARY KEY,
                        user_id uuid,
                        product_id uuid,
                        quantity INT NOT NULL,
                        price NUMERIC(10, 2) NOT NULL,
                        order_status VARCHAR(50) NOT NULL,
                        total_amount NUMERIC(10, 2) NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(user_id),
                        FOREIGN KEY (product_id) REFERENCES product (product_id)
);

CREATE TABLE shopping_cart (
                               cart_id IDENTITY PRIMARY KEY,
                               user_id uuid,
                               product_id uuid,
                               quantity INT NOT NULL,
                               total_amount NUMERIC(10, 2) NOT NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (user_id) REFERENCES users(user_id),
                               FOREIGN KEY (product_id) REFERENCES product (product_id)
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
        UNIQUE (product_name, description, currency, image_id, category_id, supplier, stock, size);

DROP TABLE IF EXISTS users CASCADE;

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
                           FOREIGN KEY (user_id) REFERENCES users (user_id),
                           FOREIGN KEY (role_id) REFERENCES role (role_id),
                           UNIQUE (user_id, role_id)
);

ALTER TABLE product
    ALTER COLUMN stock SET DATA TYPE BIGINT;

ALTER TABLE product
    ADD COLUMN version BIGINT;
