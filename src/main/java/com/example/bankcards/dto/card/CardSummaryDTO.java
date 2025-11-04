package com.example.bankcards.dto.card;

import com.example.bankcards.entity.CardEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;


/**
 * Data Transfer Object providing a summarized view of a {@link com.example.bankcards.entity.CardEntity}.
 * <p>
 * Used primarily in paginated responses (e.g., {@code /cards/me}, {@code /cards/filter})
 * to deliver concise card information without exposing sensitive data such as the full PAN.
 * <p>
 * Includes key details like cardholder name, last four digits, expiration date,
 * balance, and creation timestamp.
 *
 * <p><b>Example JSON representation:</b>
 * <pre>
 * {
 *   "id": 12,
 *   "holderName": "John Doe",
 *   "last4": "1234",
 *   "expirationDate": "2027-09-01",
 *   "status": "ACTIVE",
 *   "balance": 350.75,
 *   "createdAt": "2025-11-04T10:15:30Z"
 * }
 * </pre>
 *
 * @param id              unique identifier of the card
 * @param holderName      full name of the cardholder
 * @param last4           last four digits of the card number (masked PAN)
 * @param expirationDate  expiration date of the card
 * @param status          current card status ({@code ACTIVE}, {@code BLOCKED}, or {@code EXPIRED})
 * @param balance         current card balance
 * @param createdAt       timestamp of when the card record was created
 *
 * @see com.example.bankcards.entity.CardEntity
 * @see com.example.bankcards.service.CardService
 */
public record CardSummaryDTO(
        Long id,
        String holderName,
        String last4,
        LocalDate expirationDate,
        String status,
        BigDecimal balance,
        Instant createdAt
) {
    public static CardSummaryDTO from(CardEntity e) {
        return new CardSummaryDTO(
                e.getId(),
                e.getHolderName(),
                e.getLast4(),
                e.getExpirationDate(),
                e.getStatus(),
                e.getBalance(),
                e.getCreatedAt()
        );
    }
}

