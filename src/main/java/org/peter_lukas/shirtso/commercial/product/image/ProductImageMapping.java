package org.peter_lukas.shirtso.commercial.product.image;

import jakarta.persistence.*;
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
@IdClass(ProductImageMappingId.class)
public class ProductImageMapping {

    @Id
    @Column(name = "product_id", insertable = false, updatable = false)
    private UUID productId;

    @Id
    @Column(name = "image_id", insertable = false, updatable = false)
    private int imageId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "image_id")
    private ProductImage image;

    private boolean isPrimary;

    private int displayOrder;

    public ProductImageMapping(Product product, ProductImage image, boolean isPrimary, int displayOrder) {
        this.productId = product.getProductId();
        this.imageId = image.getImageId();
        this.product = product;
        this.image = image;
        this.isPrimary = isPrimary;
        this.displayOrder = displayOrder;
    }
}
