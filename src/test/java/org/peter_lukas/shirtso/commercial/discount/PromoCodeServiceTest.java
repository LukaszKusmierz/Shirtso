package org.peter_lukas.shirtso.commercial.discount;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peter_lukas.shirtso.commercial.discount.dto.*;
import org.peter_lukas.shirtso.commercial.product.validation.PromoCodeNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromoCodeServiceTest {

    @Mock
    private PromoCodeRepository promoCodeRepository;

    @Mock
    private PromoCodeMapper promoCodeMapper;

    @InjectMocks
    private PromoCodeService promoCodeService;

    private PromoCode testPromoCode;
    private PromoCodeDto testPromoCodeDto;

    @BeforeEach
    void setUp() {
        testPromoCode = createTestPromoCode();
        testPromoCodeDto = createTestPromoCodeDto();
    }

    private PromoCode createTestPromoCode() {
        PromoCode promo = new PromoCode(
                "TEST10",
                "10% off test promo",
                DiscountType.PERCENTAGE,
                new BigDecimal("10"),
                new BigDecimal("50"),
                new BigDecimal("20"),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(30),
                100
        );
        promo.setPromoCodeId(1);
        return promo;
    }

    private PromoCodeDto createTestPromoCodeDto() {
        return new PromoCodeDto(
                1, "TEST10", "10% off test promo",
                DiscountType.PERCENTAGE, new BigDecimal("10"),
                new BigDecimal("50"), new BigDecimal("20"),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(30),
                100, 0, true
        );
    }

    @Test
    void getAllPromoCodes_ReturnsAllCodes() {
        // given
        List<PromoCode> promoCodes = List.of(testPromoCode);
        when(promoCodeRepository.findAll()).thenReturn(promoCodes);
        when(promoCodeMapper.mapToDto(testPromoCode)).thenReturn(testPromoCodeDto);

        // when
        List<PromoCodeDto> result = promoCodeService.getAllPromoCodes();

        // then
        assertThat(result).hasSize(1);
        verify(promoCodeRepository).findAll();
    }

    @Test
    void getActivePromoCodes_ReturnsOnlyActiveAndValidCodes() {
        // given
        List<PromoCode> activeCodes = List.of(testPromoCode);
        when(promoCodeRepository.findActivePromoCodes(any(LocalDateTime.class)))
                .thenReturn(activeCodes);
        when(promoCodeMapper.mapToDto(testPromoCode)).thenReturn(testPromoCodeDto);

        // when
        List<PromoCodeDto> result = promoCodeService.getActivePromoCodes();

        // then
        assertThat(result).hasSize(1);
        verify(promoCodeRepository).findActivePromoCodes(any(LocalDateTime.class));
    }

    @Test
    void getPromoCode_WithValidId_ReturnsPromoCode() {
        // given
        when(promoCodeRepository.findById(1)).thenReturn(Optional.of(testPromoCode));
        when(promoCodeMapper.mapToDto(testPromoCode)).thenReturn(testPromoCodeDto);

        // when
        PromoCodeDto result = promoCodeService.getPromoCode(1);

        // then
        assertThat(result).isNotNull();
        assertThat(result.code()).isEqualTo("TEST10");
    }

    @Test
    void getPromoCode_WithInvalidId_ThrowsException() {
        // given
        when(promoCodeRepository.findById(999)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> promoCodeService.getPromoCode(999))
                .isInstanceOf(PromoCodeNotFoundException.class);
    }

    @Test
    void createPromoCode_WithUniqueCode_CreatesSuccessfully() {
        // given
        CreatePromoCodeDto createDto = new CreatePromoCodeDto(
                "NEW20", "20% off", DiscountType.PERCENTAGE,
                new BigDecimal("20"), null, null,
                LocalDateTime.now(), LocalDateTime.now().plusDays(30), null
        );

        when(promoCodeRepository.existsByCodeIgnoreCase("NEW20")).thenReturn(false);
        when(promoCodeMapper.createEntityFromDto(createDto)).thenReturn(testPromoCode);
        when(promoCodeRepository.save(testPromoCode)).thenReturn(testPromoCode);
        when(promoCodeMapper.mapToDto(testPromoCode)).thenReturn(testPromoCodeDto);

        // when
        PromoCodeDto result = promoCodeService.createPromoCode(createDto);

        // then
        assertThat(result).isNotNull();
        verify(promoCodeRepository).save(testPromoCode);
    }

    @Test
    void createPromoCode_WithDuplicateCode_ThrowsException() {
        // given
        CreatePromoCodeDto createDto = new CreatePromoCodeDto(
                "DUPLICATE", "Test", DiscountType.PERCENTAGE,
                new BigDecimal("10"), null, null,
                LocalDateTime.now(), LocalDateTime.now().plusDays(30), null
        );

        when(promoCodeRepository.existsByCodeIgnoreCase("DUPLICATE")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> promoCodeService.createPromoCode(createDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(promoCodeRepository, never()).save(any());
    }

    @Test
    void updatePromoCode_WithValidData_UpdatesSuccessfully() {
        // given
        UpdatePromoCodeDto updateDto = new UpdatePromoCodeDto(
                "Updated description", DiscountType.PERCENTAGE,
                new BigDecimal("15"), null, null,
                LocalDateTime.now(), LocalDateTime.now().plusDays(30),
                null, true
        );

        when(promoCodeRepository.findById(1)).thenReturn(Optional.of(testPromoCode));
        when(promoCodeRepository.save(testPromoCode)).thenReturn(testPromoCode);
        when(promoCodeMapper.mapToDto(testPromoCode)).thenReturn(testPromoCodeDto);

        // when
        PromoCodeDto result = promoCodeService.updatePromoCode(1, updateDto);

        // then
        assertThat(result).isNotNull();
        verify(promoCodeMapper).updateEntityFromDto(testPromoCode, updateDto);
        verify(promoCodeRepository).save(testPromoCode);
    }

    @Test
    void deletePromoCode_WithValidId_DeletesSuccessfully() {
        // given
        when(promoCodeRepository.existsById(1)).thenReturn(true);

        // when
        promoCodeService.deletePromoCode(1);

        // then
        verify(promoCodeRepository).deleteById(1);
    }

    @Test
    void deletePromoCode_WithInvalidId_ThrowsException() {
        // given
        when(promoCodeRepository.existsById(999)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> promoCodeService.deletePromoCode(999))
                .isInstanceOf(PromoCodeNotFoundException.class);
    }

    @Test
    void validatePromoCode_WithValidCode_ReturnsValidResult() {
        // given
        ValidatePromoCodeDto validateDto = new ValidatePromoCodeDto(
                "TEST10", new BigDecimal("100")
        );

        when(promoCodeRepository.findByCodeIgnoreCase("TEST10"))
                .thenReturn(Optional.of(testPromoCode));
        when(promoCodeMapper.mapToDto(testPromoCode)).thenReturn(testPromoCodeDto);

        // when
        PromoCodeValidationResultDto result = promoCodeService.validatePromoCode(validateDto);

        // then
        assertThat(result.valid()).isTrue();
        assertThat(result.discountAmount()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void validatePromoCode_WithInvalidCode_ReturnsInvalidResult() {
        // given
        ValidatePromoCodeDto validateDto = new ValidatePromoCodeDto(
                "INVALID", new BigDecimal("100")
        );

        when(promoCodeRepository.findByCodeIgnoreCase("INVALID"))
                .thenReturn(Optional.empty());

        // when
        PromoCodeValidationResultDto result = promoCodeService.validatePromoCode(validateDto);

        // then
        assertThat(result.valid()).isFalse();
        assertThat(result.discountAmount()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void validatePromoCode_WithExpiredCode_ReturnsInvalidResult() {
        // given
        PromoCode expiredPromo = new PromoCode(
                "EXPIRED",
                "Expired promo",
                DiscountType.PERCENTAGE,
                new BigDecimal("10"),
                null, null,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(1),
                100
        );

        ValidatePromoCodeDto validateDto = new ValidatePromoCodeDto(
                "EXPIRED", new BigDecimal("100")
        );

        when(promoCodeRepository.findByCodeIgnoreCase("EXPIRED"))
                .thenReturn(Optional.of(expiredPromo));

        // when
        PromoCodeValidationResultDto result = promoCodeService.validatePromoCode(validateDto);

        // then
        assertThat(result.valid()).isFalse();
        assertThat(result.message()).contains("expired");
    }

    @Test
    void validatePromoCode_WithInsufficientOrderValue_ReturnsInvalidResult() {
        // given
        ValidatePromoCodeDto validateDto = new ValidatePromoCodeDto(
                "TEST10", new BigDecimal("30") // Less than minimum order value of 50
        );

        when(promoCodeRepository.findByCodeIgnoreCase("TEST10"))
                .thenReturn(Optional.of(testPromoCode));

        // when
        PromoCodeValidationResultDto result = promoCodeService.validatePromoCode(validateDto);

        // then
        assertThat(result.valid()).isFalse();
        assertThat(result.message()).contains("at least");
    }

    @Test
    void incrementPromoCodeUsage_WithValidCode_IncrementsCount() {
        // given
        int initialUsageCount = testPromoCode.getUsageCount();
        when(promoCodeRepository.findByCodeIgnoreCase("TEST10"))
                .thenReturn(Optional.of(testPromoCode));
        when(promoCodeRepository.save(testPromoCode)).thenReturn(testPromoCode);

        // when
        promoCodeService.incrementPromoCodeUsage("TEST10");

        // then
        assertThat(testPromoCode.getUsageCount()).isEqualTo(initialUsageCount + 1);
        verify(promoCodeRepository).save(testPromoCode);
    }

    @Test
    void incrementPromoCodeUsage_WithInvalidCode_ThrowsException() {
        // given
        when(promoCodeRepository.findByCodeIgnoreCase("INVALID"))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> promoCodeService.incrementPromoCodeUsage("INVALID"))
                .isInstanceOf(PromoCodeNotFoundException.class);
    }
}