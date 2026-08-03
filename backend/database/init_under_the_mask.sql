-- Under The Mask MySQL database setup.
-- This full schema script is for manual provisioning outside the normal Flyway startup flow.
-- If you run the app with Flyway enabled, run database/create_database.sql instead and let Flyway create tables.
-- Lobbies, players, reconnect tokens, settings, and connection state are intentionally not stored in MySQL.
-- The backend persists only permanent word content: categories, word entries, and associations.

CREATE DATABASE IF NOT EXISTS under_the_mask
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'underthemask'@'localhost'
    IDENTIFIED BY 'underthemask';

GRANT ALL PRIVILEGES ON under_the_mask.* TO 'underthemask'@'localhost';
FLUSH PRIVILEGES;

USE under_the_mask;

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_categories_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS word_entries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    value VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    KEY idx_word_entries_category_id (category_id),
    KEY idx_word_entries_active (active),
    CONSTRAINT uk_word_entries_category_value UNIQUE (category_id, value),
    CONSTRAINT fk_word_entries_category FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS associations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    word_entry_id BIGINT NOT NULL,
    value VARCHAR(120) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_associations_word_entry_id (word_entry_id),
    CONSTRAINT uk_associations_word_value UNIQUE (word_entry_id, value),
    CONSTRAINT fk_associations_word_entry FOREIGN KEY (word_entry_id) REFERENCES word_entries (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
