package org.peter_lukas.shirtso.commercial.discount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Integer> {

    Optional<PromoCode> findByCodeIgnoreCase(String code);

    @Query("SELECT p FROM PromoCode p WHERE p.isActive = true AND p.startDate <= :now AND p.endDate >= :now " +
            "AND (p.usageLimit IS NULL OR p.usageCount < p.usageLimit)")
    List<PromoCode> findActivePromoCodes(LocalDateTime now);

    boolean existsByCodeIgnoreCase(String code);
}
