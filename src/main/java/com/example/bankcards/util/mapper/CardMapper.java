package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.card.CardCreateRequestDTO;
import com.example.bankcards.dto.card.CardResponseDTO;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.util.encryptors.PanMasker;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


/**
 * Utility class responsible for mapping between {@link CardEntity} and
 * its corresponding Data Transfer Objects (DTOs).
 * <p>
 * Provides static conversion methods used by service-layer classes to
 * transform card data between persistence and API representations.
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *   <li>Convert {@link CardCreateRequestDTO} into a {@link CardEntity} ready for persistence.</li>
 *   <li>Convert {@link CardEntity} into a {@link CardResponseDTO} for API responses.</li>
 *   <li>Ensure that sensitive information such as the card number (PAN)
 *       is masked before being exposed via API endpoints.</li>
 * </ul>
 *
 * @see com.example.bankcards.service.CardService
 * @see com.example.bankcards.dto.card.CardCreateRequestDTO
 * @see com.example.bankcards.dto.card.CardResponseDTO
 * @see com.example.bankcards.util.encryptors.PanMasker
 */
public final class CardMapper {

    /** Formatter for converting {@link LocalDate} expiration dates to {@code MM/yy} format. */
    private static final DateTimeFormatter MM_YY = DateTimeFormatter.ofPattern("MM/yy");

    /** Private constructor to prevent instantiation (utility class). */
    private CardMapper() {}


    /**
     * Converts a {@link CardCreateRequestDTO} into a new {@link CardEntity}.
     * <p>
     * This method does not set the card owner; ownership is assigned later
     * in the service layer depending on whether the card is created by
     * the current user or an admin.
     *
     * <p>Default values:
     * <ul>
     *   <li>{@code status} — initialized as {@code "ACTIVE"}</li>
     *   <li>{@code balance} — taken directly from the DTO (may be {@code null})</li>
     * </ul>
     *
     * @param dto the card creation request containing user-provided data
     * @return a new {@link CardEntity} instance initialized with the DTO fields
     */
    public static CardEntity toEntity(CardCreateRequestDTO dto) {
        CardEntity c = new CardEntity();
        c.setCardNumber(dto.pan());
        c.setExpirationDate(dto.parseExpiry());
        c.setPin(dto.pin());
        c.setCvv(dto.cvv());
        c.setBalance(dto.balance());        // default
        c.setStatus("ACTIVE");              // default
        return c;
    }


    /**
     * Converts a {@link CardEntity} into a {@link CardResponseDTO} suitable for API output.
     * <p>
     * The card number is masked using {@link PanMasker#mask(String)} to prevent exposure
     * of sensitive data. Owner information is included if available.
     *
     * @param card the {@link CardEntity} to convert
     * @return a {@link CardResponseDTO} representing the card for API responses
     */
    public static CardResponseDTO toResponse(CardEntity card) {
        var user = card.getUser(); // owner
        return new CardResponseDTO(
                card.getId(),
                PanMasker.mask(card.getCardNumber()),
                card.getExpirationDate().format(MM_YY),
                card.getStatus(),
                card.getBalance(),
                (user != null ? user.getId() : null),
                (user != null ? user.getUsername() : null)
        );
    }
}
