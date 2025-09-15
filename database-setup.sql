-- Mini Banking Database Setup
-- PostgreSQL Database

-- Create database
CREATE DATABASE mini_banking;

-- Connect to database
\c mini_banking;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create tables
CREATE TABLE customers (
    customer_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_code VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    date_of_birth DATE,
    address TEXT,
    city VARCHAR(100),
    country VARCHAR(100) DEFAULT 'VN',
    id_type VARCHAR(20),
    id_number VARCHAR(50) UNIQUE,
    kyc_status VARCHAR(20) DEFAULT 'PENDING',
    risk_level VARCHAR(20) DEFAULT 'LOW',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE accounts (
    account_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_number VARCHAR(20) UNIQUE NOT NULL,
    customer_id UUID NOT NULL REFERENCES customers(customer_id),
    account_type VARCHAR(20) NOT NULL,
    currency VARCHAR(3) DEFAULT 'VND',
    balance DECIMAL(18,2) DEFAULT 0.00,
    available_balance DECIMAL(18,2) DEFAULT 0.00,
    credit_limit DECIMAL(18,2) DEFAULT 0.00,
    interest_rate DECIMAL(5,4) DEFAULT 0.0000,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    opened_date DATE DEFAULT CURRENT_DATE,
    closed_date DATE,
    last_transaction_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transactions (
    transaction_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_code VARCHAR(30) UNIQUE NOT NULL,
    from_account_id UUID REFERENCES accounts(account_id),
    to_account_id UUID REFERENCES accounts(account_id),
    amount DECIMAL(18,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'VND',
    transaction_type VARCHAR(30) NOT NULL,
    description TEXT,
    reference_number VARCHAR(50),
    status VARCHAR(20) DEFAULT 'PENDING',
    failure_reason TEXT,
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_phone ON customers(phone);
CREATE INDEX idx_customers_id_number ON customers(id_number);
CREATE INDEX idx_customers_status ON customers(status);

CREATE INDEX idx_accounts_customer_id ON accounts(customer_id);
CREATE INDEX idx_accounts_account_number ON accounts(account_number);
CREATE INDEX idx_accounts_status ON accounts(status);

CREATE INDEX idx_transactions_from_account ON transactions(from_account_id);
CREATE INDEX idx_transactions_to_account ON transactions(to_account_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_type ON transactions(transaction_type);

-- Create composite indexes
CREATE INDEX idx_transactions_account_date ON transactions(from_account_id, created_at);
CREATE INDEX idx_transactions_status_date ON transactions(status, created_at);

-- Create triggers for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_customers_updated_at BEFORE UPDATE ON customers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_accounts_updated_at BEFORE UPDATE ON accounts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_transactions_updated_at BEFORE UPDATE ON transactions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert sample data
INSERT INTO customers (customer_code, first_name, last_name, email, phone, id_type, id_number, kyc_status) VALUES
('CUST001', 'Nguyen', 'Van A', 'nguyenvana@email.com', '0901234567', 'CCCD', '123456789', 'VERIFIED'),
('CUST002', 'Tran', 'Thi B', 'tranthib@email.com', '0901234568', 'CCCD', '123456790', 'VERIFIED'),
('CUST003', 'Le', 'Van C', 'levanc@email.com', '0901234569', 'CCCD', '123456791', 'VERIFIED');

INSERT INTO accounts (account_number, customer_id, account_type, balance, available_balance) VALUES
('1234567890', (SELECT customer_id FROM customers WHERE customer_code = 'CUST001'), 'SAVINGS', 10000000.00, 10000000.00),
('1234567891', (SELECT customer_id FROM customers WHERE customer_code = 'CUST002'), 'SAVINGS', 5000000.00, 5000000.00),
('1234567892', (SELECT customer_id FROM customers WHERE customer_code = 'CUST003'), 'CHECKING', 2000000.00, 2000000.00);

-- Create views
CREATE VIEW customer_account_summary AS
SELECT 
    c.customer_id,
    c.customer_code,
    c.first_name,
    c.last_name,
    c.email,
    COUNT(a.account_id) as total_accounts,
    SUM(a.balance) as total_balance,
    SUM(a.available_balance) as total_available_balance
FROM customers c
LEFT JOIN accounts a ON c.customer_id = a.customer_id AND a.status = 'ACTIVE'
GROUP BY c.customer_id, c.customer_code, c.first_name, c.last_name, c.email;

CREATE VIEW transaction_summary AS
SELECT 
    DATE(created_at) as transaction_date,
    transaction_type,
    COUNT(*) as transaction_count,
    SUM(amount) as total_amount,
    AVG(amount) as average_amount
FROM transactions
WHERE status = 'COMPLETED'
GROUP BY DATE(created_at), transaction_type
ORDER BY transaction_date DESC;



