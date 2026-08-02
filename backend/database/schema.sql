-- MySQL Database Schema for CINJELLY PHP REST API Backend
-- Database target: zerolord_cinback

-- Users Table
CREATE TABLE IF NOT EXISTS `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(64) NOT NULL UNIQUE,
    `email` VARCHAR(128) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(128) DEFAULT '',
    `jellyfin_user_id` VARCHAR(64) DEFAULT NULL,
    `jellyfin_access_token` VARCHAR(255) DEFAULT NULL,
    `is_admin` TINYINT(1) DEFAULT 0,
    `is_premium` TINYINT(1) DEFAULT 0,
    `preferred_quality` VARCHAR(32) DEFAULT '4K Ultra HD',
    `subscription_expires_at` DATETIME DEFAULT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Subscriptions & Payments Table
CREATE TABLE IF NOT EXISTS `subscriptions` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `plan_name` VARCHAR(64) DEFAULT 'Cinode Premium Streamer',
    `amount_paid` DECIMAL(10,2) NOT NULL,
    `currency` VARCHAR(8) DEFAULT 'NGN',
    `payment_reference` VARCHAR(128) NOT NULL UNIQUE,
    `transaction_reference` VARCHAR(128) DEFAULT NULL,
    `status` VARCHAR(32) DEFAULT 'PENDING',
    `payment_gateway` VARCHAR(32) DEFAULT 'MONNIFY',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Payment Transactions Audit Log Table
CREATE TABLE IF NOT EXISTS `transactions` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT DEFAULT NULL,
    `payment_reference` VARCHAR(128) NOT NULL,
    `transaction_reference` VARCHAR(128) DEFAULT NULL,
    `amount` DECIMAL(10,2) NOT NULL,
    `status` VARCHAR(32) NOT NULL,
    `raw_payload` TEXT DEFAULT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Jellyfin Session Audit Table
CREATE TABLE IF NOT EXISTS `jellyfin_sessions` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `device_name` VARCHAR(128) DEFAULT 'Android App',
    `client_version` VARCHAR(32) DEFAULT '1.0.0',
    `token` VARCHAR(255) NOT NULL,
    `last_active_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- System Configuration Table
CREATE TABLE IF NOT EXISTS `system_config` (
    `config_key` VARCHAR(64) PRIMARY KEY,
    `config_value` TEXT DEFAULT NULL,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed Default Admin & System Settings
INSERT INTO `system_config` (`config_key`, `config_value`) VALUES
('STREAM_PRICE_NGN', '2500.00'),
('MONNIFY_CONTRACT_CODE', '1234567890'),
('ALLOW_PUBLIC_SIGNUP', '1')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`);
