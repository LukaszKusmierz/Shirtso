package org.peter_lukas.shirtso.commercial.product.image;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductImageMappingId implements Serializable {

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "image_id")
    private int imageId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductImageMappingId that = (ProductImageMappingId) o;
        return imageId == that.imageId &&
                Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, imageId);
    }
}
