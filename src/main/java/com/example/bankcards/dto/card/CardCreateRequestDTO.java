package com.example.bankcards.dto.card;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public record CardCreateRequestDTO(
        @NotBlank
        @Pattern(regexp = "^\\d{16}$", message = "PAN must be 16 digits")
        String pan,

        @NotBlank
        @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "expiry must be MM/yy")
        String expiry,

        @NotBlank @Pattern(regexp="^\\d{4}$", message="PIN must be 4 digits")
        String pin,

        // Optional; if omitted, we’ll default to 0.00
        @Digits(integer = 18, fraction = 2)
        @DecimalMin(value = "0.00", inclusive = true, message = "balance must be >= 0.00")
        BigDecimal balance,

        // If CVV is required by DB, keep this; otherwise remove.
        @NotBlank @Pattern(regexp="^\\d{3}$", message="CVV must be 3 digits")
        String cvv
) {
        private static final DateTimeFormatter MM_YY_TO_LOCALDATE =
                new DateTimeFormatterBuilder()
                        .parseCaseInsensitive()
                        .appendPattern("MM/yy")
                        .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                        .toFormatter();

        public LocalDate parseExpiry() {
                return LocalDate.parse(expiry, MM_YY_TO_LOCALDATE);
        }
}
