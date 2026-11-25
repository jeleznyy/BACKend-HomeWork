ALTER TABLE bikes ADD COLUMN rented_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE bikes ADD CONSTRAINT bikes_rented_at_requires_customer
    CHECK (rented_at IS NULL OR customer_id IS NOT NULL);
