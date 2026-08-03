-- Create the database and local MySQL user expected by the backend.
-- Leave the database empty when running the Spring Boot app with Flyway enabled.

CREATE DATABASE IF NOT EXISTS under_the_mask
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'underthemask'@'localhost'
    IDENTIFIED BY 'underthemask';

GRANT ALL PRIVILEGES ON under_the_mask.* TO 'underthemask'@'localhost';
FLUSH PRIVILEGES;
