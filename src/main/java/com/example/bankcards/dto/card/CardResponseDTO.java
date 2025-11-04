package com.example.bankcards.dto.card;

import java.math.BigDecimal;


/**
 * Data Transfer Object representing a summarized view of a {@link com.example.bankcards.entity.CardEntity}.
 * <p>
 * Returned by card-related endpoints such as:
 * <ul>
 *   <li>{@code GET /cards}</li>
 *   <li>{@code GET /cards/{id}}</li>
 *   <li>Filtering and pagination endpoints ({@code /cards/filter}, {@code /cards/me})</li>
 * </ul>
 * <p>
 * Sensitive fields (like the full PAN) are never exposed — only a masked version
 * ({@code **** **** **** 1234}) is included to preserve confidentiality.
 *
 * <p><b>Example JSON response:</b>
 * <pre>
 * {
 *   "id": 42,
 *   "maskedPan": "**** **** **** 5678",
 *   "expiry": "09/27",
 *   "status": "ACTIVE",
 *   "balance": 1500.00,
 *   "ownerId": 7,
 *   "ownerUsername": "johndoe"
 * }
 * </pre>
 *
 * @param id             unique identifier of the card
 * @param maskedPan      masked card number, showing only the last four digits
 * @param expiry         card expiration date in {@code MM/yy} format
 * @param status         current card status ({@code ACTIVE}, {@code BLOCKED}, or {@code EXPIRED})
 * @param balance        current monetary balance of the card
 * @param ownerId        ID of the user who owns this card
 * @param ownerUsername  username of the card owner
 *
 * @see com.example.bankcards.entity.CardEntity
 * @see com.example.bankcards.service.CardService
 */
public record CardResponseDTO(
        Long id,
        String maskedPan,     // "**** **** **** 1234"
        String expiry,
        String status,        // ACTIVE/BLOCKED/EXPIRED
        BigDecimal balance,
        Long ownerId,
        String ownerUsername
) {}
