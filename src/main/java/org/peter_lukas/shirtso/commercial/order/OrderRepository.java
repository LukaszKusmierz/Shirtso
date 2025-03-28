package org.peter_lukas.shirtso.commercial.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("SELECT o FROM Order o WHERE o.user.userId = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.orderId = :orderId AND o.user.userId = :userId")
    Optional<Order> findByOrderIdAndUserIdWithItems(Integer orderId, UUID userId);

    @Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.orderId = :orderId")
    Optional<Order> findByOrderIdWithItems(Integer orderId);
}
