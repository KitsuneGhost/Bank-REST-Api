package com.example.bankcards.dto.card;

import jakarta.validation.constraints.*;

import java.time.YearMonth;

public record CardCreateRequestDTO(
        @NotBlank
        @Pattern(regexp = "^\\d{16}$", message = "PAN must be 16 digits")
        String pan,

        @NotBlank
        @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "expiry must be MM/yy")
        String expiry
) {}
