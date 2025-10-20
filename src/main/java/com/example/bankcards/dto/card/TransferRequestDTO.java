package com.example.bankcards.dto.card;

import java.math.BigDecimal;

public record TransferRequestDTO(Long fromCardId, Long toCardId, BigDecimal amount) {}