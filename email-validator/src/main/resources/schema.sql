-- Création de la table emails
CREATE TABLE IF NOT EXISTS emails (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      address VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Index pour améliorer les performances
CREATE INDEX IF NOT EXISTS idx_email_address ON emails(address);
CREATE INDEX IF NOT EXISTS idx_email_status ON emails(status);
CREATE INDEX IF NOT EXISTS idx_email_active ON emails(is_active);