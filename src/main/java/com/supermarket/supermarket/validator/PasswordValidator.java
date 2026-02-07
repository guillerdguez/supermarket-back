package com.supermarket.supermarket.validator;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PasswordValidator {

    private static final int MIN_LENGTH = 8;

    public List<String> validatePassword(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.length() < MIN_LENGTH) {
            errors.add("Password must be at least " + MIN_LENGTH + " characters long");
        }

        if (password != null) {
            if (!password.matches(".*[A-Z].*")) {
                errors.add("Password must contain at least one uppercase letter");
            }

            if (!password.matches(".*[a-z].*")) {
                errors.add("Password must contain at least one lowercase letter");
            }

            if (!password.matches(".*\\d.*")) {
                errors.add("Password must contain at least one digit");
            }

            if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
                errors.add("Password must contain at least one special character");
            }
        }

        return errors;
    }

    public boolean isValid(String password) {
        return validatePassword(password).isEmpty();
    }
}