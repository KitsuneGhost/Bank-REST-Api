package com.example.bankcards.dto.card;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

public record CardFilter(
        String q,                     // optional free-text: holder name (via join), last4 (derived)
        Set<String> status,           // ACTIVE,BLOCKED,CLOSED (strings if you keep it as String)
        LocalDate expiryFrom,         // inclusive
        LocalDate expiryTo,           // inclusive
        BigDecimal balanceMin,        // inclusive
        BigDecimal balanceMax,        // inclusive
        Instant createdFrom,          // inclusive
        Instant createdTo             // inclusive
) {
    public boolean hasAny() {
        return (q != null && !q.isBlank())
                || (status != null && !status.isEmpty())
                || expiryFrom != null || expiryTo != null
                || balanceMin != null || balanceMax != null
                || createdFrom != null || createdTo != null;
    }
}

