ALTER TABLE product
    ADD CONSTRAINT unique_product_constraint
        UNIQUE (product_name, description, currency, image_id, category_id, supplier, stock, size)