package com.example.bankcards.dto.card;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;


/**
 * Data Transfer Object representing the request payload for creating a new bank card.
 * <p>
 * Used by endpoints such as {@code POST /cards/me/create} or {@code POST /cards/users/{userId}/create}.
 * <p>
 * Includes validation annotations to enforce formatting rules for PAN, expiry date, PIN, balance,
 * and CVV. Optional fields such as {@code balance} are defaulted to {@code 0.00} if omitted.
 *
 * <p>Example JSON payload:
 * <pre>
 * {
 *   "pan": "1234567812345678",
 *   "expiry": "09/27",
 *   "pin": "1234",
 *   "balance": 500.00,
 *   "cvv": "789"
 * }
 * </pre>
 *
 * @param pan     the 16-digit Primary Account Number (PAN); must match {@code ^\\d{16}$}
 * @param expiry  the expiration date in {@code MM/yy} format (e.g., {@code 09/27})
 * @param pin     the 4-digit personal identification number for the card
 * @param balance the initial balance of the card (optional; defaults to 0.00 if omitted)
 * @param cvv     the 3-digit card verification value (CVV)
 *
 * @see com.example.bankcards.entity.CardEntity
 * @see com.example.bankcards.service.CardService#createForCurrentUser(CardCreateRequestDTO)
 * @see jakarta.validation.constraints.Pattern
 * @see jakarta.validation.constraints.Digits
 * @see jakarta.validation.constraints.DecimalMin
 */
public record CardCreateRequestDTO(
        @NotBlank
        @Pattern(regexp = "^\\d{16}$", message = "PAN must be 16 digits")
        String pan,

        @NotBlank
        @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "expiry must be MM/yy")
        String expiry,

        @NotBlank @Pattern(regexp="^\\d{4}$", message="PIN must be 4 digits")
        String pin,

        @Digits(integer = 18, fraction = 2)
        @DecimalMin(value = "0.00", inclusive = true, message = "balance must be >= 0.00")
        BigDecimal balance,

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
