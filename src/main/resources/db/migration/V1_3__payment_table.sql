-- Payment table
CREATE TABLE payment (
                         payment_id SERIAL PRIMARY KEY,
                         order_id INT NOT NULL UNIQUE,
                         amount NUMERIC(10, 2) NOT NULL,
                         payment_status VARCHAR(20) NOT NULL,
                         payment_method VARCHAR(20) NOT NULL,
                         transaction_id VARCHAR(255),
                         payment_date TIMESTAMP,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);
