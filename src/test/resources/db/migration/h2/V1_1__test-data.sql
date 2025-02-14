----------------------------
-- 1. Tabela USERS
----------------------------
INSERT INTO users (user_id, user_name, email, password, created_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'john_doe', 'john@example.com', 'password1', CURRENT_TIMESTAMP),
    ('22222222-2222-2222-2222-222222222222', 'jane_doe', 'jane@example.com', 'password2', CURRENT_TIMESTAMP),
    ('33333333-3333-3333-3333-333333333333', 'bob_smith', 'bob@example.com', 'password3', CURRENT_TIMESTAMP);

----------------------------
-- 2. Tabela ROLE
----------------------------
INSERT INTO role (role_id, name)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'USER'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'ADMIN'),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'MODERATOR');

----------------------------
-- 3. Tabela USER_ROLE
----------------------------
-- Upewnij się, że tutaj odwołania do użytkowników i ról istnieją
INSERT INTO user_role (user_id, role_id)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
    ('22222222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'),
    ('33333333-3333-3333-3333-333333333333', 'cccccccc-cccc-cccc-cccc-cccccccccccc');

----------------------------
-- 4. Tabela CATEGORY
----------------------------
-- Zakładamy, że kategoria "Odzież Męska" to 1, "Sportowa" to 2, a "Formalna" to 3.
INSERT INTO category (category_id, category_name)
VALUES
    (1, 'Odzież Męska'),
    (2, 'Sportowa'),
    (3, 'Formalna');

----------------------------
-- 5. Tabela SUBCATEGORY
----------------------------
-- Przykładowe podkategorie: dla "Odzież Męska" – "Koszule", dla "Sportowa" – "Bluzy", dla "Formalna" – "Garnitury"
INSERT INTO subcategory (subcategory_id, subcategory_name, category_id)
VALUES
    (1, 'Koszule', 1),
    (2, 'Bluzy', 2),
    (3, 'Garnitury', 3);

----------------------------
-- 6. Tabela PRODUCT
----------------------------
-- Upewnij się, że wartość subcategory_id odpowiada istniejącym podkategoriom.
INSERT INTO product (product_id, product_name, description, price, currency, image_id, subcategory_id, supplier, stock, size, version)
VALUES
    ('11111111-aaaa-aaaa-aaaa-111111111111', 'Koszula Elegancka', 'Elegancka koszula męska', 150.00, 'PLN', 1, 1, 'Vistula', 20, 'L', 1),
    ('22222222-bbbb-bbbb-bbbb-222222222222', 'Bluza Sportowa', 'Wygodna bluza sportowa', 120.00, 'PLN', 2, 2, 'Nike', 30, 'M', 1),
    ('33333333-cccc-cccc-cccc-333333333333', 'Garnitur Klasyczny', 'Klasyczny garnitur męski', 800.00, 'PLN', 3, 3, 'SuitCo', 10, 'XL', 1);

----------------------------
-- 7. Tabela PRODUCT_IMAGE
----------------------------
-- Każdy produkt ma przypisane jedno zdjęcie.
INSERT INTO product_image (image_id, product_id, image_url)
VALUES
    (1, '11111111-aaaa-aaaa-aaaa-111111111111', 'https://example.com/images/koszula.jpg'),
    (2, '22222222-bbbb-bbbb-bbbb-222222222222', 'https://example.com/images/bluza.jpg'),
    (3, '33333333-cccc-cccc-cccc-333333333333', 'https://example.com/images/garnitur.jpg');

----------------------------
-- 8. Tabela ORDERS
----------------------------
INSERT INTO orders (order_id, user_id, order_status, total_amount, created_at)
VALUES
    (1, '11111111-1111-1111-1111-111111111111', 'NEW', 150.00, CURRENT_TIMESTAMP),
    (2, '22222222-2222-2222-2222-222222222222', 'PROCESSING', 120.00, CURRENT_TIMESTAMP),
    (3, '33333333-3333-3333-3333-333333333333', 'COMPLETED', 800.00, CURRENT_TIMESTAMP);

----------------------------
-- 9. Tabela ORDER_ITEM
----------------------------
-- Przykładowe pozycje zamówień
INSERT INTO order_item (order_item_id, order_id, product_id, quantity, price)
VALUES
    (1, 1, '11111111-aaaa-aaaa-aaaa-111111111111', 1, 150.00),
    (2, 2, '22222222-bbbb-bbbb-bbbb-222222222222', 1, 120.00),
    (3, 3, '33333333-cccc-cccc-cccc-333333333333', 1, 800.00);

----------------------------
-- 10. Tabela SHOPPING_CART
----------------------------
INSERT INTO shopping_cart (cart_id, user_id, created_at)
VALUES
    (1, '11111111-1111-1111-1111-111111111111', CURRENT_TIMESTAMP),
    (2, '22222222-2222-2222-2222-222222222222', CURRENT_TIMESTAMP),
    (3, '33333333-3333-3333-3333-333333333333', CURRENT_TIMESTAMP);

----------------------------
-- 11. Tabela CART_ITEM
----------------------------
-- Pozycje w koszyku
INSERT INTO cart_item (cart_item_id, cart_id, product_id, quantity, total_amount)
VALUES
    (1, 1, '11111111-aaaa-aaaa-aaaa-111111111111', 2, 300.00),
    (2, 2, '22222222-bbbb-bbbb-bbbb-222222222222', 1, 120.00),
    (3, 3, '33333333-cccc-cccc-cccc-333333333333', 1, 800.00);

----------------------------
-- 12. Tabela REVIEW
----------------------------
INSERT INTO review (review_id, product_id, user_id, rating, comment, created_at)
VALUES
    (1, '11111111-aaaa-aaaa-aaaa-111111111111', '11111111-1111-1111-1111-111111111111', 5, 'Świetna koszula!', CURRENT_TIMESTAMP),
    (2, '22222222-bbbb-bbbb-bbbb-222222222222', '22222222-2222-2222-2222-222222222222', 4, 'Bardzo wygodna bluza.', CURRENT_TIMESTAMP),
    (3, '33333333-cccc-cccc-cccc-333333333333', '33333333-3333-3333-3333-333333333333', 3, 'Garnitur mógłby być lepszy.', CURRENT_TIMESTAMP);
