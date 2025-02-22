INSERT INTO users(user_id, user_name, email, password)
VALUES ('160a6c2b-b07e-4422-b0e9-c0d6ac0b6fa6', 'Paweł Bombel', 'user@mail.com', '$2a$10$0t3pb1qlwKc4uyN2cCNLmej63F5XFvZMGW12i7IjWixy29shJ6s6O'), -- usersecret
       ('9400a854-1fea-4262-b2fb-a4e0b30d2faf', 'Maryna Serweta', 'admin@mail.com', '$2a$10$6nIzvs8lTPZSScCzPjWzYu61N9Sz1BnOL9AOAt37AdtAtrYd1aOte'); -- adminsecret

INSERT INTO role(role_id, name)
VALUES ('7014b753-b963-4f50-81d8-def00f550386', 'USER_READ'),
       ('4c6a158f-b360-49c4-b6f2-c26b3d1ed7df', 'USER_WRITE');

INSERT INTO user_role(user_id, role_id)
VALUES ('160a6c2b-b07e-4422-b0e9-c0d6ac0b6fa6', '7014b753-b963-4f50-81d8-def00f550386'),
       ('9400a854-1fea-4262-b2fb-a4e0b30d2faf', '7014b753-b963-4f50-81d8-def00f550386'),
       ('9400a854-1fea-4262-b2fb-a4e0b30d2faf', '4c6a158f-b360-49c4-b6f2-c26b3d1ed7df');