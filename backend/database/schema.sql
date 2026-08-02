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

-- Seed Default System Configuration Credentials & Secrets
INSERT INTO `system_config` (`config_key`, `config_value`) VALUES
('JELLYFIN_SERVER_URL', 'https://cinode.zerolord.com'),
('JELLYFIN_FALLBACK_URL', 'http://163.245.193.7:8096'),
('JELLYFIN_ADMIN_USER', 'duwit'),
('JELLYFIN_ADMIN_PASS', '@f33rinimi'),
('JELLYFIN_API_KEY', '79ee2e15ee1f47fd881188ef4da13391'),
('MONNIFY_API_KEY', 'MK_PROD_123456789'),
('MONNIFY_SECRET_KEY', 'SK_PROD_987654321'),
('MONNIFY_CONTRACT_CODE', '1234567890'),
('MONNIFY_USE_SANDBOX', 'true'),
('STREAM_PRICE_NGN', '600.00'),
('VIP_PASS_PRICE_NGN', '600.00'),
('JWT_SECRET', 'cinjelly_jwt_secret_key_2026_super_secure'),
('DB_HOST', '105.113.98.181'),
('DB_NAME', 'zerolord_cinback'),
('DB_USER', 'zerolord_cinback'),
('DB_PASS', '@f33rinimi'),
('ALLOW_PUBLIC_SIGNUP', '1')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`);

-- Seed Current Jellyfin Users into MySQL Database
INSERT INTO `users` (`username`, `email`, `password_hash`, `full_name`, `jellyfin_user_id`, `is_admin`, `is_premium`) VALUES
('duwit', 'duwit@cinode.zerolord.com', '$2y$10$wE1Vp2G8A4GkUj5m19J32uA8Xg2Kx5lE6D5T1Z0Y9X8W7V6U5T4S3', 'Duwit Admin', '8699065ad11d490894f712887ccc9ce1', 1, 1),
('Oyinpepper', 'oyinpepper@cinode.zerolord.com', '$2y$10$wE1Vp2G8A4GkUj5m19J32uA8Xg2Kx5lE6D5T1Z0Y9X8W7V6U5T4S3', 'Oyinpepper User', '94c997dad1fe4563bb2a9c7cabb42468', 0, 1),
('YungObalola', 'yungobalola@cinode.zerolord.com', '$2y$10$wE1Vp2G8A4GkUj5m19J32uA8Xg2Kx5lE6D5T1Z0Y9X8W7V6U5T4S3', 'YungObalola User', 'c462886abc8e4f8589cb9f4063176364', 0, 1)
ON DUPLICATE KEY UPDATE 
    `jellyfin_user_id` = VALUES(`jellyfin_user_id`),
    `is_admin` = VALUES(`is_admin`),
    `is_premium` = VALUES(`is_premium`);
