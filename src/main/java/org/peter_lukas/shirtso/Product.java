package org.peter_lukas.shirtso;

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
public class Product {

    @Id
    @EqualsAndHashCode.Include
    private UUID id = UUID.randomUUID();
    @NotBlank(message = "This field can't be empty.")
    @NotNull
    @Size(min = 3, max = 20, message = "Title must be between 3 and 20 characters")
    private String productName;
    @NotBlank(message = "Description cannot be empty")
    private String description;
    @NotNull(message = "Price cannot be empty")
    private BigDecimal price;
    @NotBlank(message = "Currency cannot be empty")
    private String currency;
    private String imageUrl;
    @NotBlank(message = "Category cannot be empty")
    private String category;
    @NotBlank(message = "Supplier field cannot be empty")
    private String supplier;
    @NotNull(message = "Quantity cannot be empty")
    private long quantity;
    @NotNull(message = "Size cannot be empty")
    private Sizes size;
    @Version
    private Integer version;

    public Product(String productName, String description, BigDecimal price, String currency, String imageUrl,
                   String category, String supplier, long quantity, Sizes size) {
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.imageUrl = imageUrl;
        this.category = category;
        this.supplier = supplier;
        this.quantity = quantity;
        this.size = size;
    }
}
