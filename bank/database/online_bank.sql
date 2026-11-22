-- =============================================================
-- Online Bank Database
-- =============================================================

DROP DATABASE IF EXISTS online_bank;
CREATE DATABASE online_bank;

USE online_bank;

-- -------------------------------------------------------------
-- users: login credentials and basic profile
-- Store only password HASHES (e.g. bcrypt/argon2), never plain text.
-- -------------------------------------------------------------
CREATE TABLE users (
    user_id        INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    username       VARCHAR(50)   NOT NULL,
    password       VARCHAR(255)  NOT NULL,
    email          VARCHAR(255)  NOT NULL,
    first_name     VARCHAR(100)  NOT NULL,
    last_name      VARCHAR(100)  NOT NULL,
    role           ENUM('CUSTOMER', 'TELLER', 'ADMIN') NOT NULL DEFAULT 'CUSTOMER',
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_users_username (username),
    UNIQUE KEY uq_users_email (email)
);

-- -------------------------------------------------------------
-- accounts: each user can own multiple accounts
-- DECIMAL is used for money to avoid floating-point rounding errors.
-- -------------------------------------------------------------
CREATE TABLE accounts (
    account_id      INT UNSIGNED   NOT NULL AUTO_INCREMENT,
    user_id         INT UNSIGNED   NOT NULL,
    account_type    ENUM('CHECKING', 'SAVINGS') NOT NULL DEFAULT 'CHECKING',
    balance         DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (account_id),
    KEY idx_accounts_user (user_id),
    CONSTRAINT fk_accounts_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

-- -------------------------------------------------------------
-- transactions: history of every money movement on an account
-- amount is always positive; transaction_type gives the direction.
-- balance_after records the account balance once the row was applied.
-- related_account_id links the other side of a transfer.
-- -------------------------------------------------------------
CREATE TABLE transactions (
    transaction_id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    account_id          INT UNSIGNED    NOT NULL,
    transaction_type    ENUM('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT') NOT NULL,
    amount              DECIMAL(15, 2)  NOT NULL,
    description         VARCHAR(255)    NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (transaction_id),
    KEY idx_transactions_account_date (account_id, created_at),
    CONSTRAINT fk_transactions_account
        FOREIGN KEY (account_id) REFERENCES accounts (account_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_transactions_amount CHECK (amount > 0)
);

-- =============================================================
-- Sample data
-- The password hashes below are placeholders, not real hashes.
-- Generate real ones in your app (e.g. password_hash() / bcrypt).
-- =============================================================
INSERT INTO users (username, password, email, first_name, last_name, role) VALUES
    ('admin', '$2b$10$REPLACE_WITH_REAL_BCRYPT_HASH_admin000000000000000', 'admin@example.com',  'Site', 'Admin', 'ADMIN');

