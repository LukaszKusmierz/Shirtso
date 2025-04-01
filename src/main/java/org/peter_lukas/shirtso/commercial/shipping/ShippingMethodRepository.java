package org.peter_lukas.shirtso.commercial.shipping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShippingMethodRepository extends JpaRepository<ShippingMethod, Integer> {

    @Query("SELECT sm FROM ShippingMethod sm WHERE sm.isActive = :isActive ORDER BY sm.price ASC")
    List<ShippingMethod> findByIsActiveOrderByPriceAsc(boolean isActive);

    boolean existsByName(String name);
}
