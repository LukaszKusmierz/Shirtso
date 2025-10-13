CREATE TABLE password_reset_token (
                                      token_id SERIAL PRIMARY KEY,
                                      token VARCHAR(255) NOT NULL UNIQUE,
                                      user_id UUID NOT NULL,
                                      expiry_date TIMESTAMP NOT NULL,
                                      used BOOLEAN NOT NULL DEFAULT FALSE,
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
