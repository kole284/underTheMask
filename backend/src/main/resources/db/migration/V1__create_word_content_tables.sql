CREATE TABLE categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_categories_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE word_entries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    value VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uk_word_entries_category_value UNIQUE (category_id, value),
    CONSTRAINT fk_word_entries_category FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_word_entries_category_id ON word_entries (category_id);
CREATE INDEX idx_word_entries_active ON word_entries (active);

CREATE TABLE associations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    word_entry_id BIGINT NOT NULL,
    value VARCHAR(120) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_associations_word_value UNIQUE (word_entry_id, value),
    CONSTRAINT fk_associations_word_entry FOREIGN KEY (word_entry_id) REFERENCES word_entries (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_associations_word_entry_id ON associations (word_entry_id);
