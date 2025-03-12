package org.peter_lukas.shirtso.auth.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.email = :email OR u.userName = :email")
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.userName = :userName")
    Optional<User> findByUserName(String userName);
}
