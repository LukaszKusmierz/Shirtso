package org.peter_lukas.shirtso.commercial.product.image;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.peter_lukas.shirtso.commercial.product.Product;

import java.util.UUID;

@Entity
@Table(name = "product_image_mapping")
@Getter
@Setter
@NoArgsConstructor
public class ProductImageMapping {

    @EmbeddedId
    private ProductImageMappingId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", insertable = false, updatable = false)
    private ProductImage image;

    private boolean isPrimary;

    private int displayOrder;

    public ProductImageMapping(Product product, ProductImage image, boolean isPrimary, int displayOrder) {
        this.id = new ProductImageMappingId(product.getProductId(), image.getImageId());
        this.product = product;
        this.image = image;
        this.isPrimary = isPrimary;
        this.displayOrder = displayOrder;
    }

    public static ProductImageMappingId createId(UUID productId, Long imageId) {
        return new ProductImageMappingId(productId, imageId);
    }
}
