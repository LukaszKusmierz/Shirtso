package org.peter_lukas.shirtso.commercial.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.peter_lukas.shirtso.commercial.product.image.ProductImage;
import org.peter_lukas.shirtso.commercial.product.image.ProductImageMapping;
import org.peter_lukas.shirtso.commercial.subcategory.Subcategory;

import java.math.BigDecimal;
import java.util.*;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Table(name = "product", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "product_name",
                "description",
                "price",
                "currency",
                "subcategory_id",
                "supplier",
                "stock",
                "size"
        })
})
public class Product {

    @Id
    @EqualsAndHashCode.Include
    private UUID productId = UUID.randomUUID();

    @NotBlank(message = "Product name can not be empty.")
    @NotNull
    @Size(min = 3, max = 20, message = "Title must be between 3 and 20 characters")
    private String productName;

    @NotBlank(message = "Description cannot be empty")
    private String description;

    @NotNull(message = "Price cannot be empty")
    private BigDecimal price;

    @NotNull(message = "Currency cannot be empty")
    @Enumerated(EnumType.STRING)
    private Currencies currency;

    @NotNull(message = "Subcategory cannot be empty")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id", nullable = false)
    private Subcategory subcategory;

    @NotBlank(message = "Supplier cannot be empty")
    private String supplier;

    @NotNull(message = "Quantity cannot be empty")
    private long stock;

    @NotNull(message = "Size cannot be empty")
    @Enumerated(EnumType.STRING)
    private Sizes size;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductImageMapping> imageMappings = new HashSet<>();

    @Version
    private Long version;

    public Product(String productName, String description, BigDecimal price, Currencies currency,
                   Subcategory subcategory, String supplier, long stock, Sizes size) {
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.subcategory = subcategory;
        this.supplier = supplier;
        this.stock = stock;
        this.size = size;
    }

    public int getCategoryId() {
        return subcategory.getCategory().getCategoryId();
    }

    public int getSubcategoryId() {
        return subcategory.getSubcategoryId();
    }

    public void addImage(ProductImage image, boolean isPrimary, int displayOrder) {
        if (isPrimary) {
            imageMappings.forEach(mapping -> mapping.setPrimary(false));
        }
        ProductImageMapping mapping = new ProductImageMapping(this, image, isPrimary, displayOrder);
        imageMappings.add(mapping);
        image.getProductMappings().add(mapping);
    }

    public Optional<ProductImage> getPrimaryImage() {
        return imageMappings.stream()
                .filter(ProductImageMapping::isPrimary)
                .map(ProductImageMapping::getImage)
                .findFirst();
    }

    public List<ProductImage> getAllImages() {
        return imageMappings.stream()
                .sorted(Comparator.comparing(ProductImageMapping::getDisplayOrder))
                .map(ProductImageMapping::getImage)
                .toList();
    }
}
