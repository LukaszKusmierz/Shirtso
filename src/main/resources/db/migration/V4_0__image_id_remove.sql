ALTER TABLE product DROP COLUMN image_id;

ALTER TABLE product DROP CONSTRAINT IF EXISTS uk_product_unique;

ALTER TABLE product ADD CONSTRAINT uk_product_unique UNIQUE (
                                                             product_name,
                                                             description,
                                                             price,
                                                             currency,
                                                             subcategory_id,
                                                             supplier,
                                                             stock,
                                                             size
);