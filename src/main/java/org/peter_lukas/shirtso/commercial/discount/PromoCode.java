package org.peter_lukas.shirtso.commercial.discount;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promo_code")
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer promoCodeId;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;

    @Column(name = "minimum_order_value")
    private BigDecimal minimumOrderValue;

    @Column(name = "maximum_discount_amount")
    private BigDecimal maximumDiscountAmount;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public PromoCode(String code, String description, DiscountType discountType,
                     BigDecimal discountValue, BigDecimal minimumOrderValue,
                     BigDecimal maximumDiscountAmount, LocalDateTime startDate,
                     LocalDateTime endDate, Integer usageLimit) {
        this.code = code;
        this.description = description;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minimumOrderValue = minimumOrderValue;
        this.maximumDiscountAmount = maximumDiscountAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.usageLimit = usageLimit;
    }

    public boolean isValid(BigDecimal orderValue) {
        LocalDateTime now = LocalDateTime.now();

        return isActive &&
                now.isAfter(startDate) &&
                now.isBefore(endDate) &&
                (minimumOrderValue == null || orderValue.compareTo(minimumOrderValue) >= 0) &&
                (usageLimit == null || usageCount < usageLimit);
    }

    public BigDecimal calculateDiscount(BigDecimal orderValue) {
        BigDecimal discount;

        if (discountType == DiscountType.PERCENTAGE) {
            discount = orderValue.multiply(discountValue.divide(new BigDecimal("100")));
        } else {
            discount = discountValue;
        }

        if (maximumDiscountAmount != null && discount.compareTo(maximumDiscountAmount) > 0) {
            discount = maximumDiscountAmount;
        }

        return discount;
    }

    public void incrementUsageCount() {
        this.usageCount++;
    }
}
