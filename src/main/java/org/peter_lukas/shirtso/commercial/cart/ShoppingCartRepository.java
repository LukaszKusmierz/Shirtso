package org.peter_lukas.shirtso.commercial.cart;

import org.peter_lukas.shirtso.auth.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Integer> {

    @Query("SELECT sc FROM ShoppingCart sc LEFT JOIN FETCH sc.items WHERE sc.user.userId = :userId")
    Optional<ShoppingCart> findByUserIdWithItems(UUID userId);

    Optional<ShoppingCart> findByUser(User user);
}
