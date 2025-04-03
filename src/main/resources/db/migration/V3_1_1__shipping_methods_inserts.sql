-- Standard Shipping
INSERT INTO shipping_method (name, description, price, estimated_delivery_days, is_active)
VALUES ('Standard Shipping', 'Regular delivery service with tracking. Typically arrives within 3-5 business days.', 9.99, 5, TRUE);

-- Economy Shipping
INSERT INTO shipping_method (name, description, price, estimated_delivery_days, is_active)
VALUES ('Economy Shipping', 'Budget-friendly shipping option with longer delivery time. No tracking provided.', 4.99, 7, TRUE);

-- Express Shipping
INSERT INTO shipping_method (name, description, price, estimated_delivery_days, is_active)
VALUES ('Express Shipping', 'Expedited delivery service with priority handling and full tracking. Arrives within 2-3 business days.', 14.99, 3, TRUE);

-- Next Day Delivery
INSERT INTO shipping_method (name, description, price, estimated_delivery_days, is_active)
VALUES ('Next Day Delivery', 'Premium shipping service guaranteed to arrive the next business day if ordered before 2 PM.', 24.99, 1, TRUE);

-- In-Store Pickup
INSERT INTO shipping_method (name, description, price, estimated_delivery_days, is_active)
VALUES ('In-Store Pickup', 'Collect your order from our store at your convenience. No shipping fees.', 0.00, 1, TRUE);

-- Courier Delivery
INSERT INTO shipping_method (name, description, price, estimated_delivery_days, is_active)
VALUES ('Courier Delivery', 'Local same-day delivery via courier service. Available only for select areas.', 19.99, 0, TRUE);

-- International Standard
INSERT INTO shipping_method (name, description, price, estimated_delivery_days, is_active)
VALUES ('International Standard', 'Standard international shipping with tracking. Delivery times vary by destination country.', 29.99, 14, TRUE);

-- International Express
INSERT INTO shipping_method (name, description, price, estimated_delivery_days, is_active)
VALUES ('International Express', 'Expedited international shipping with priority handling and full tracking capabilities.', 49.99, 7, TRUE);

-- Weekend Delivery
INSERT INTO shipping_method (name, description, price, estimated_delivery_days, is_active)
VALUES ('Weekend Delivery', 'Special service for Saturday/Sunday deliveries. Must be ordered by Friday noon.', 19.99, 2, TRUE);

-- Evening Delivery
INSERT INTO shipping_method (name, description, price, estimated_delivery_days, is_active)
VALUES ('Evening Delivery', 'Scheduled delivery during evening hours (6 PM - 9 PM) for customer convenience.', 17.99, 2, TRUE);