package org.peter_lukas.shirtso.commercial.cart;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    Optional<CartItem> findByCartCartIdAndProductProductId(Integer cartId, UUID productId);
}
