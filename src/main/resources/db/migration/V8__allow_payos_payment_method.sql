-- Keep the database constraint aligned with PaymentMethod.PAYOS.
ALTER TABLE orders
    DROP CONSTRAINT IF EXISTS orders_payment_method_check;

ALTER TABLE orders
    ADD CONSTRAINT orders_payment_method_check
        CHECK (payment_method IN ('COD', 'BANK_TRANSFER', 'CREDIT_CARD', 'PAYOS'));
