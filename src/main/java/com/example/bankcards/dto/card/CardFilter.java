package com.example.bankcards.dto.card;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

/**
 * Data Transfer Object representing a set of optional filters
 * for querying {@link com.example.bankcards.entity.CardEntity} records.
 * <p>
 * Used primarily by service-layer methods such as
 * {@link com.example.bankcards.service.CardService#filterForUser(Long, CardFilter,
 * org.springframework.data.domain.Pageable)}
 * and {@link com.example.bankcards.service.CardService#filterMine(CardFilter,
 * org.springframework.data.domain.Pageable)}.
 * <p>
 * All fields are optional; only non-null values are included in the
 * dynamic {@link org.springframework.data.jpa.domain.Specification} built
 * by {@link com.example.bankcards.repository.spec.CardSpecs#build(Long, CardFilter)}.
 * <p>
 * <b>Available filters:</b>
 * <ul>
 *   <li>{@code q} — free-text search (matches holder name or last 4 digits)</li>
 *   <li>{@code status} — set of allowed card statuses</li>
 *   <li>{@code expiryFrom}, {@code expiryTo} — expiration date range</li>
 *   <li>{@code balanceMin}, {@code balanceMax} — balance range</li>
 *   <li>{@code createdFrom}, {@code createdTo} — creation date range</li>
 * </ul>
 *
 * Example usage (as query parameters):
 * <pre>
 * GET /cards/filter?q=john&status=ACTIVE,BLOCKED&balanceMin=100&expiryTo=2026-01-01
 * </pre>
 *
 * @param q           optional text search query (e.g., part of name or last4)
 * @param status      set of allowed status values (e.g., {@code ACTIVE}, {@code BLOCKED})
 * @param expiryFrom  lower bound of expiration date (inclusive)
 * @param expiryTo    upper bound of expiration date (inclusive)
 * @param balanceMin  minimum balance (inclusive)
 * @param balanceMax  maximum balance (inclusive)
 * @param createdFrom lower bound of creation date/time (inclusive)
 * @param createdTo   upper bound of creation date/time (inclusive)
 *
 * @see com.example.bankcards.repository.spec.CardSpecs
 * @see com.example.bankcards.service.CardService
 */
public record CardFilter(
        String q,
        Set<String> status,
        LocalDate expiryFrom,
        LocalDate expiryTo,
        BigDecimal balanceMin,
        BigDecimal balanceMax,
        Instant createdFrom,
        Instant createdTo
) {

    /**
     * Determines whether any of the filter fields have been provided.
     * <p>
     * This is used to quickly check if the filter is empty
     * (i.e., if no constraints were specified by the user).
     *
     * @return {@code true} if at least one filter field is non-null or non-empty; {@code false} otherwise
     */
    public boolean hasAny() {
        return (q != null && !q.isBlank())
                || (status != null && !status.isEmpty())
                || expiryFrom != null || expiryTo != null
                || balanceMin != null || balanceMax != null
                || createdFrom != null || createdTo != null;
    }
}

