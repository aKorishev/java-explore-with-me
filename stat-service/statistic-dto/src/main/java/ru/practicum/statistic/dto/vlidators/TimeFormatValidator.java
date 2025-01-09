package ru.practicum.statistic.dto.vlidators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class TimeFormatValidator implements ConstraintValidator<TimeFormatConstraint, String> {
    public static final String PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        var format = new SimpleDateFormat(PATTERN);

        try {
            var date = format.parse(value);

            var reverse = format.format(date);

            return reverse.equals(value);
        } catch (ParseException e) {
            return false;
        }
    }
}
