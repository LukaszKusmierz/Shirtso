package org.peter_lukas.shirtso.commercial.discount;

import org.peter_lukas.shirtso.commercial.discount.dto.CreatePromoCodeDto;
import org.peter_lukas.shirtso.commercial.discount.dto.PromoCodeDto;
import org.peter_lukas.shirtso.commercial.discount.dto.UpdatePromoCodeDto;
import org.springframework.stereotype.Component;

@Component
public class PromoCodeMapper {

    public PromoCodeDto mapToDto(PromoCode promoCode) {
        return new PromoCodeDto(
                promoCode.getPromoCodeId(),
                promoCode.getCode(),
                promoCode.getDescription(),
                promoCode.getDiscountType(),
                promoCode.getDiscountValue(),
                promoCode.getMinimumOrderValue(),
                promoCode.getMaximumDiscountAmount(),
                promoCode.getStartDate(),
                promoCode.getEndDate(),
                promoCode.getUsageLimit(),
                promoCode.getUsageCount(),
                promoCode.isActive()
        );
    }

    public PromoCode createEntityFromDto(CreatePromoCodeDto dto) {
        return new PromoCode(
                dto.code().toUpperCase(),
                dto.description(),
                dto.discountType(),
                dto.discountValue(),
                dto.minimumOrderValue(),
                dto.maximumDiscountAmount(),
                dto.startDate(),
                dto.endDate(),
                dto.usageLimit()
        );
    }

    public void updateEntityFromDto(PromoCode promoCode, UpdatePromoCodeDto dto) {
        promoCode.setDescription(dto.description());
        promoCode.setDiscountType(dto.discountType());
        promoCode.setDiscountValue(dto.discountValue());
        promoCode.setMinimumOrderValue(dto.minimumOrderValue());
        promoCode.setMaximumDiscountAmount(dto.maximumDiscountAmount());
        promoCode.setStartDate(dto.startDate());
        promoCode.setEndDate(dto.endDate());
        promoCode.setUsageLimit(dto.usageLimit());
        promoCode.setActive(dto.isActive());
    }
}
