package com.example.bankcards.dto.card;

import jakarta.validation.constraints.Pattern;


/**
 * Data Transfer Object representing the payload for updating a card's expiration date
 * and/or status.
 * <p>
 * Used by administrative and owner-level endpoints (e.g., {@code PUT /cards/{id}})
 * to modify existing card details. Both fields are optional, allowing partial updates.
 * <p>
 * Validation ensures the expiration date follows the {@code MM/yy} format and that
 * the status value matches one of the allowed constants: {@code ACTIVE}, {@code BLOCKED},
 * or {@code EXPIRED}.
 *
 * <p><b>Example JSON payload:</b>
 * <pre>
 * {
 *   "expiry": "09/27",
 *   "status": "BLOCKED"
 * }
 * </pre>
 *
 * @param expiry the new card expiration date in {@code MM/yy} format (optional)
 * @param status the new card status, one of {@code ACTIVE}, {@code BLOCKED}, or {@code EXPIRED} (optional)
 *
 * @see com.example.bankcards.service.CardService#updateCard(Long, CardUpdateRequestDTO)
 * @see jakarta.validation.constraints.Pattern
 */
public record CardUpdateRequestDTO(
        @Pattern(regexp = "^(0[1-9]|1[0-2])\\/\\d{2}$", message = "expiry must be MM/yy")
        String expiry,
        @Pattern(regexp = "ACTIVE|BLOCKED|EXPIRED", message = "status must be ACTIVE|BLOCKED|EXPIRED")
        String status
) {}
