package org.peter_lukas.shirtso.commercial.shipping;

import org.peter_lukas.shirtso.commercial.product.validation.ShippingMethodNotFoundException;
import org.peter_lukas.shirtso.commercial.shipping.dto.CreateShippingMethodDto;
import org.peter_lukas.shirtso.commercial.shipping.dto.ShippingMethodDto;
import org.peter_lukas.shirtso.commercial.shipping.dto.UpdateShippingMethodDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.peter_lukas.shirtso.messages.Alerts.SHIPPING_METHOD_NOT_FOUND;

@Service
public class ShippingMethodService {

    private final ShippingMethodRepository shippingMethodRepository;
    private final ShippingMethodMapper shippingMethodMapper;

    public ShippingMethodService(ShippingMethodRepository shippingMethodRepository,
                                 ShippingMethodMapper shippingMethodMapper) {
        this.shippingMethodRepository = shippingMethodRepository;
        this.shippingMethodMapper = shippingMethodMapper;
    }

    @Transactional(readOnly = true)
    public List<ShippingMethodDto> getAllShippingMethods() {
        return shippingMethodRepository.findAll().stream()
                .map(shippingMethodMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShippingMethodDto> getActiveShippingMethods() {
        return shippingMethodRepository.findByIsActiveOrderByPriceAsc(true).stream()
                .map(shippingMethodMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ShippingMethodDto getShippingMethod(Integer shippingMethodId) {
        ShippingMethod shippingMethod = shippingMethodRepository.findById(shippingMethodId)
                .orElseThrow(() -> new ShippingMethodNotFoundException(SHIPPING_METHOD_NOT_FOUND));

        return shippingMethodMapper.mapToDto(shippingMethod);
    }

    @Transactional
    public ShippingMethodDto createShippingMethod(CreateShippingMethodDto createDto) {
        if (shippingMethodRepository.existsByName(createDto.name())) {
            throw new IllegalArgumentException("Shipping method with this name already exists");
        }

        ShippingMethod shippingMethod = shippingMethodMapper.createEntityFromDto(createDto);
        ShippingMethod savedShippingMethod = shippingMethodRepository.save(shippingMethod);

        return shippingMethodMapper.mapToDto(savedShippingMethod);
    }

    @Transactional
    public ShippingMethodDto updateShippingMethod(Integer shippingMethodId, UpdateShippingMethodDto updateDto) {
        ShippingMethod shippingMethod = shippingMethodRepository.findById(shippingMethodId)
                .orElseThrow(() -> new ShippingMethodNotFoundException(SHIPPING_METHOD_NOT_FOUND));

        // Check if name is being changed and if it already exists
        if (!shippingMethod.getName().equals(updateDto.name())
                && shippingMethodRepository.existsByName(updateDto.name())) {
            throw new IllegalArgumentException("Shipping method with this name already exists");
        }

        shippingMethodMapper.updateEntityFromDto(shippingMethod, updateDto);
        shippingMethod.setUpdatedAt(LocalDateTime.now());

        ShippingMethod updatedShippingMethod = shippingMethodRepository.save(shippingMethod);
        return shippingMethodMapper.mapToDto(updatedShippingMethod);
    }

    @Transactional
    public void deleteShippingMethod(Integer shippingMethodId) {
        if (!shippingMethodRepository.existsById(shippingMethodId)) {
            throw new ShippingMethodNotFoundException(SHIPPING_METHOD_NOT_FOUND);
        }

        shippingMethodRepository.deleteById(shippingMethodId);
    }

    @Transactional
    public ShippingMethodDto toggleShippingMethodStatus(Integer shippingMethodId) {
        ShippingMethod shippingMethod = shippingMethodRepository.findById(shippingMethodId)
                .orElseThrow(() -> new ShippingMethodNotFoundException(SHIPPING_METHOD_NOT_FOUND));

        shippingMethod.setActive(!shippingMethod.isActive());
        shippingMethod.setUpdatedAt(LocalDateTime.now());

        ShippingMethod updatedShippingMethod = shippingMethodRepository.save(shippingMethod);
        return shippingMethodMapper.mapToDto(updatedShippingMethod);
    }
}
