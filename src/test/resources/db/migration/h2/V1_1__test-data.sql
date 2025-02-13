INSERT INTO users (user_id, user_name, email, password, created_at) VALUES
      ('11111111-1111-1111-1111-111111111111', 'alice', 'alice@example.com', 'passAlice', CURRENT_TIMESTAMP),
      ('22222222-2222-2222-2222-222222222222', 'bob',   'bob@example.com',   'passBob',   CURRENT_TIMESTAMP),
      ('33333333-3333-3333-3333-333333333333', 'carol', 'carol@example.com', 'passCarol', CURRENT_TIMESTAMP);

INSERT INTO role (role_id, name) VALUES
      ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'ADMIN'),
      ('aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'USER'),
      ('aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'MANAGER');

INSERT INTO user_role (user_id, role_id) VALUES
      ('11111111-1111-1111-1111-111111111111', 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1'),
      ('22222222-2222-2222-2222-222222222222', 'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2'),
      ('33333333-3333-3333-3333-333333333333', 'aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3');

INSERT INTO category (category_name) VALUES
      ('Electronics'),
      ('Books'),
      ('Clothing');

INSERT INTO product (product_id, product_name, description, price, currency, image_id, category_id, supplier, stock, size, version) VALUES
      ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'Laptop',  'High performance laptop', 1200.00, 'USD', 1, 1, 'Dell', 10,    '15-inch', 1),
      ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'Novel',   'Interesting novel',       20.00,  'USD', 2, 2, 'Penguin', 50,    'Paperback', 1),
      ('ffffffff-ffff-ffff-ffff-ffffffffffff', 'T-Shirt', 'Comfortable t-shirt',     15.00,  'USD', 3, 3, 'H&M',     100,   'M',         1);

INSERT INTO product_image (product_id, image_url) VALUES
      ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'http://example.com/laptop.jpg'),
      ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'http://example.com/novel.jpg'),
      ('ffffffff-ffff-ffff-ffff-ffffffffffff', 'http://example.com/tshirt.jpg');

INSERT INTO orders (user_id, product_id, quantity, price, order_status, total_amount, created_at) VALUES
      ('11111111-1111-1111-1111-111111111111', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 1, 1200.00, 'NEW',        1200.00, CURRENT_TIMESTAMP),
      ('22222222-2222-2222-2222-222222222222', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 2, 20.00,  'PROCESSING', 40.00,  CURRENT_TIMESTAMP),
      ('33333333-3333-3333-3333-333333333333', 'ffffffff-ffff-ffff-ffff-ffffffffffff', 3, 15.00,  'SHIPPED',    45.00,  CURRENT_TIMESTAMP);

INSERT INTO shopping_cart (user_id, product_id, quantity, total_amount, created_at) VALUES
        ('11111111-1111-1111-1111-111111111111', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 1, 1200.00, CURRENT_TIMESTAMP),
        ('22222222-2222-2222-2222-222222222222', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 1, 20.00,   CURRENT_TIMESTAMP),
        ('33333333-3333-3333-3333-333333333333', 'ffffffff-ffff-ffff-ffff-ffffffffffff', 2, 30.00,   CURRENT_TIMESTAMP);

INSERT INTO review (product_id, user_id, rating, comment, created_at) VALUES
      ('dddddddd-dddd-dddd-dddd-dddddddddddd', '11111111-1111-1111-1111-111111111111', 5, 'Excellent laptop',  CURRENT_TIMESTAMP),
      ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '22222222-2222-2222-2222-222222222222', 4, 'Good novel',        CURRENT_TIMESTAMP),
      ('ffffffff-ffff-ffff-ffff-ffffffffffff', '33333333-3333-3333-3333-333333333333', 3, 'Average t-shirt',   CURRENT_TIMESTAMP);
