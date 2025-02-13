DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
                       user_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                       user_name VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE role (
                       role_id   UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE user_role (
                       user_id UUID,
                       role_id UUID,
                       FOREIGN KEY (user_id) REFERENCES users (user_id),
                       FOREIGN KEY (role_id) REFERENCES role (role_id),
                       UNIQUE (user_id, role_id)
);