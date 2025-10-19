package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.card.CardCreateRequestDTO;
import com.example.bankcards.dto.card.CardResponseDTO;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.util.encryptors.PanMasker;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class CardMapper {
    private static final DateTimeFormatter MM_YY = DateTimeFormatter.ofPattern("MM/yy");

    private CardMapper() {}

    // toEntity WITHOUT owner: owner is set in the service (current user or admin target)
    public static CardEntity toEntity(CardCreateRequestDTO dto) {
        CardEntity c = new CardEntity();
        c.setCardNumber(dto.pan());
        c.setExpirationDate(dto.parseExpiry());
        c.setPin(dto.pin());
        c.setCvv(dto.cvv());
        c.setBalance(dto.balance());       // default
        c.setStatus("ACTIVE"); // default
        return c;
    }

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
