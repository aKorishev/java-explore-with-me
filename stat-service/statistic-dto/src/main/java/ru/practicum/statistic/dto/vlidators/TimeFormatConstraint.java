package ru.practicum.statistic.dto.vlidators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TimeFormatValidator.class)
public @interface TimeFormatConstraint {
    String message() default "Формат времени ожитдается \"yyyy-MM-dd HH:mm:ss\"";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
