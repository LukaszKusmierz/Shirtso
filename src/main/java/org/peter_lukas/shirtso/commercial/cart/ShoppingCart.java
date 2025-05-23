package org.peter_lukas.shirtso.commercial.cart;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.peter_lukas.shirtso.auth.user.User;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "shopping_cart")
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ShoppingCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer cartId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartItem> items = new HashSet<>();

    public ShoppingCart(User user) {
        this.user = user;
    }

    public void addItem(CartItem item) {
        if (item != null) {
            items.add(item);
            item.setCart(this);
        }
    }

    public void removeItem(CartItem item) {
        if (item != null) {
            items.remove(item);
            item.setCart(null);
        }
    }
}
