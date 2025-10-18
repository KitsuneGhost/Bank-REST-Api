package com.example.bankcards.dto.card;

import java.math.BigDecimal;

public record CardResponseDTO(
        Long id,
        String maskedPan,    // **** **** **** 1234
        String expiry,       // MM/YY
        String status,       // ACTIVE | BLOCKED | EXPIRED
        BigDecimal balance
) {}
