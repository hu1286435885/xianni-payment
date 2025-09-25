CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY,
    order_id VARCHAR(255) UNIQUE NOT NULL,
    channel VARCHAR(50),
    amount NUMERIC(18, 2) NOT NULL,
    account VARCHAR(100) NOT NULL,
    currency VARCHAR(10) DEFAULT 'IDR',
    payment_method VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    corebank_reference VARCHAR(255),
    biller_reference VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_transactions_order_id ON transactions(order_id);