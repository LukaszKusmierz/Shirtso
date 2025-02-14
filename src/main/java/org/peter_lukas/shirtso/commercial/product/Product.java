package org.peter_lukas.shirtso.commercial.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Table(name = "product", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "productName",
                "description",
                "price",
                "currency",
                "imageId",
                "subcategoryId",
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

    private int imageId;

    @NotNull(message = "Category Id cannot be empty")
    private int subcategoryId;

    @NotBlank(message = "Supplier cannot be empty")
    private String supplier;

    @NotNull(message = "Quantity cannot be empty")
    private long stock;

    @NotNull(message = "Size cannot be empty")
    @Enumerated(EnumType.STRING)
    private Sizes size;

    @Version
    private Integer version;

    public Product(String productName, String description, BigDecimal price, Currencies currency, int imageId,
                   int subcategoryId, String supplier, long stock, Sizes size) {
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.imageId = imageId;
        this.subcategoryId = subcategoryId;
        this.supplier = supplier;
        this.stock = stock;
        this.size = size;
    }
}
