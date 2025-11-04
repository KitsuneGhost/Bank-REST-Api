package com.example.bankcards.dto.card;

import java.math.BigDecimal;


/**
 * Data Transfer Object representing a fund transfer request between two cards.
 * <p>
 * Used by endpoints such as {@code POST /cards/transfer} to initiate
 * a balance transfer between cards owned by the same user.
 * <p>
 * The service layer validates ownership, card status, and sufficient balance
 * before performing the transfer.
 *
 * <p><b>Example JSON payload:</b>
 * <pre>
 * {
 *   "fromCardId": 101,
 *   "toCardId": 205,
 *   "amount": 250.00
 * }
 * </pre>
 *
 * @param fromCardId ID of the source card (must belong to the current user)
 * @param toCardId   ID of the destination card (must belong to the current user)
 * @param amount     amount to transfer; must be positive and not exceed the source balance
 *
 * @see com.example.bankcards.service.CardService#transferBetweenMyCards(Long, Long, java.math.BigDecimal)
 * @see com.example.bankcards.entity.TransferEntity
 */
public record TransferRequestDTO(Long fromCardId, Long toCardId, BigDecimal amount) {}