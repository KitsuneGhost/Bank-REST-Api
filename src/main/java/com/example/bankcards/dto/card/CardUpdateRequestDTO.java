package com.example.bankcards.dto.card;

import jakarta.validation.constraints.Pattern;

public record CardUpdateRequestDTO(
        @Pattern(regexp = "^(0[1-9]|1[0-2])\\/\\d{2}$", message = "expiry must be MM/yy")
        String expiry,
        @Pattern(regexp = "ACTIVE|BLOCKED|EXPIRED", message = "status must be ACTIVE|BLOCKED|EXPIRED")
        String status
) {}
