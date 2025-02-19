package org.peter_lukas.shirtso.commercial.subcategory;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.peter_lukas.shirtso.commercial.category.Category;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "subcategory")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Subcategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int subcategoryId;

    @NotBlank(message = "Subcategory name can not be empty")
    @Column(name = "subcategory_name", nullable = false, unique = true)
    private String subcategoryName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public Subcategory(String subcategoryName, Category category) {
        this.subcategoryName = subcategoryName;
        this.category = category;
    }
}
