ALTER TABLE product
    ALTER COLUMN stock TYPE bigint USING stock::bigint;
