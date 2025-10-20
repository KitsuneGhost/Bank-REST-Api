package com.example.bankcards.dto.card;

import com.example.bankcards.entity.CardEntity;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record CardSummaryDTO(
        Long id,
        String holderName,
        String last4,                // "1234"
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

