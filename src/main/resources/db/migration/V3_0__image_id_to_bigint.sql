-- Create a new sequence for BIGINT values
CREATE SEQUENCE IF NOT EXISTS product_image_id_seq_bigint;

-- Alter the column to use BIGINT type
ALTER TABLE product_image ALTER COLUMN image_id TYPE BIGINT;

-- Set the new sequence as the default value
ALTER TABLE product_image ALTER COLUMN image_id SET DEFAULT nextval('product_image_id_seq_bigint');

-- Update all references to this column in other tables
ALTER TABLE product_image_mapping ALTER COLUMN image_id TYPE BIGINT;