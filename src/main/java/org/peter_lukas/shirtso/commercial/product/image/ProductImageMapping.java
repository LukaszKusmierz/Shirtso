package org.peter_lukas.shirtso.commercial.product.image;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.peter_lukas.shirtso.commercial.product.Product;

@Entity
@Table(name = "product_image_mapping")
@Getter
@Setter
@NoArgsConstructor
@IdClass(ProductImageMappingId.class)
public class ProductImageMapping {

    @Id
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Id
    @ManyToOne
    @JoinColumn(name = "image_id")
    private ProductImage image;

    private boolean isPrimary;

    private int displayOrder;

    public ProductImageMapping(Product product, ProductImage image, boolean isPrimary, int displayOrder) {
        this.product = product;
        this.image = image;
        this.isPrimary = isPrimary;
        this.displayOrder = displayOrder;
    }
}
