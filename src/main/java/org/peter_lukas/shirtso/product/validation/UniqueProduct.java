package org.peter_lukas.shirtso.product.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.peter_lukas.shirtso.messages.Alerts;

import java.lang.annotation.*;

import static org.peter_lukas.shirtso.messages.Alerts.DUPLICATE_PRODUCT;

@Documented
@Constraint(validatedBy = UniqueProductValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueProduct {
    String message() default DUPLICATE_PRODUCT;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
