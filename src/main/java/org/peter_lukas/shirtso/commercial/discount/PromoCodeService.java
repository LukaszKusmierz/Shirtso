package org.peter_lukas.shirtso.commercial.discount;

import org.peter_lukas.shirtso.commercial.discount.dto.*;
import org.peter_lukas.shirtso.commercial.product.validation.InvalidPromoCodeException;
import org.peter_lukas.shirtso.commercial.product.validation.PromoCodeNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.peter_lukas.shirtso.messages.Alerts.EXPIRED_PROMO_CODE;
import static org.peter_lukas.shirtso.messages.Alerts.INVALID_PROMO_CODE;

@Service
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeMapper promoCodeMapper;

    public PromoCodeService(PromoCodeRepository promoCodeRepository, PromoCodeMapper promoCodeMapper) {
        this.promoCodeRepository = promoCodeRepository;
        this.promoCodeMapper = promoCodeMapper;
    }

    @Transactional(readOnly = true)
    public List<PromoCodeDto> getAllPromoCodes() {
        return promoCodeRepository.findAll().stream()
                .map(promoCodeMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PromoCodeDto> getActivePromoCodes() {
        LocalDateTime now = LocalDateTime.now();
        return promoCodeRepository.findActivePromoCodes(now).stream()
                .map(promoCodeMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PromoCodeDto getPromoCode(Integer promoCodeId) {
        PromoCode promoCode = promoCodeRepository.findById(promoCodeId)
                .orElseThrow(() -> new PromoCodeNotFoundException(INVALID_PROMO_CODE));

        return promoCodeMapper.mapToDto(promoCode);
    }

    @Transactional
    public PromoCodeDto createPromoCode(CreatePromoCodeDto createDto) {
        if (promoCodeRepository.existsByCodeIgnoreCase(createDto.code())) {
            throw new IllegalArgumentException("Promo code with this code already exists");
        }

        PromoCode promoCode = promoCodeMapper.createEntityFromDto(createDto);
        PromoCode savedPromoCode = promoCodeRepository.save(promoCode);

        return promoCodeMapper.mapToDto(savedPromoCode);
    }

    @Transactional
    public PromoCodeDto updatePromoCode(Integer promoCodeId, UpdatePromoCodeDto updateDto) {
        PromoCode promoCode = promoCodeRepository.findById(promoCodeId)
                .orElseThrow(() -> new PromoCodeNotFoundException(INVALID_PROMO_CODE));

        promoCodeMapper.updateEntityFromDto(promoCode, updateDto);
        promoCode.setUpdatedAt(LocalDateTime.now());

        PromoCode updatedPromoCode = promoCodeRepository.save(promoCode);
        return promoCodeMapper.mapToDto(updatedPromoCode);
    }

    @Transactional
    public void deletePromoCode(Integer promoCodeId) {
        if (!promoCodeRepository.existsById(promoCodeId)) {
            throw new PromoCodeNotFoundException(INVALID_PROMO_CODE);
        }

        promoCodeRepository.deleteById(promoCodeId);
    }

    @Transactional
    public PromoCodeValidationResultDto validatePromoCode(ValidatePromoCodeDto validateDto) {
        try {
            PromoCode promoCode = promoCodeRepository.findByCodeIgnoreCase(validateDto.code())
                    .orElseThrow(() -> new InvalidPromoCodeException(INVALID_PROMO_CODE));

            if (!promoCode.isValid(validateDto.orderValue())) {
                LocalDateTime now = LocalDateTime.now();

                if (now.isBefore(promoCode.getStartDate()) || now.isAfter(promoCode.getEndDate())) {
                    throw new InvalidPromoCodeException(EXPIRED_PROMO_CODE);
                }

                if (promoCode.getMinimumOrderValue() != null &&
                        validateDto.orderValue().compareTo(promoCode.getMinimumOrderValue()) < 0) {
                    throw new InvalidPromoCodeException(
                            "Order value must be at least " + promoCode.getMinimumOrderValue());
                }

                if (promoCode.getUsageLimit() != null && promoCode.getUsageCount() >= promoCode.getUsageLimit()) {
                    throw new InvalidPromoCodeException("Promo code has reached its usage limit");
                }

                if (!promoCode.isActive()) {
                    throw new InvalidPromoCodeException(INVALID_PROMO_CODE);
                }
            }

            BigDecimal discountAmount = promoCode.calculateDiscount(validateDto.orderValue());

            return new PromoCodeValidationResultDto(
                    true,
                    "Promo code applied successfully",
                    promoCodeMapper.mapToDto(promoCode),
                    discountAmount
            );
        } catch (InvalidPromoCodeException | PromoCodeNotFoundException e) {
            return new PromoCodeValidationResultDto(
                    false,
                    e.getMessage(),
                    null,
                    BigDecimal.ZERO
            );
        }
    }

    @Transactional
    public void incrementPromoCodeUsage(String code) {
        PromoCode promoCode = promoCodeRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new PromoCodeNotFoundException(INVALID_PROMO_CODE));

        promoCode.incrementUsageCount();
        promoCodeRepository.save(promoCode);
    }
}
