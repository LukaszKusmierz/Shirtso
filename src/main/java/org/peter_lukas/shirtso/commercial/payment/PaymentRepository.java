package org.peter_lukas.shirtso.commercial.payment;

import org.peter_lukas.shirtso.commercial.cart.dto.CartDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    Optional<Payment> findByOrderOrderId(Integer orderId);
}
