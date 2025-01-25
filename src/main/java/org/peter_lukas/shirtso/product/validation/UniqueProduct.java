package org.peter_lukas.shirtso.product.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueProductValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueProduct {
    String message() default "A product with the same attributes already exists!!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
