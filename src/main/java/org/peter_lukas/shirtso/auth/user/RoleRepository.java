package org.peter_lukas.shirtso.auth.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    @Query("SELECT r FROM Role r JOIN FETCH r.users WHERE r.name = :name")
    Optional<Role> findByName(String name);
}

