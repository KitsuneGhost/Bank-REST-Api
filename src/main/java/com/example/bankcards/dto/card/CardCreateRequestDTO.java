package com.example.bankcards.dto.card;

import jakarta.validation.constraints.*;

public record CardCreateRequestDTO(
        @NotBlank @Pattern(regexp="\\d{16}") String pan, // will be encrypted at rest
        @NotBlank @Pattern(regexp="\\d{2}/\\d{2}") String expiry, // MM/YY
        @NotBlank String holderName
) {}
