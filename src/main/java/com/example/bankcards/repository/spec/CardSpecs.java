package com.example.bankcards.repository.spec;

import com.example.bankcards.dto.card.CardFilter;
import com.example.bankcards.entity.CardEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

public final class CardSpecs {
    private CardSpecs() {}

    public static Specification<CardEntity> ownerIs(Long userId) {
        return (r, q, cb) -> cb.equal(r.get("user").get("id"), userId);
    }

    public static Specification<CardEntity> statusIn(Set<String> statuses) {
        if (statuses == null || statuses.isEmpty()) return null;
        return (r, q, cb) -> r.get("status").in(statuses);
    }

    public static Specification<CardEntity> expiryFrom(LocalDate from) {
        if (from == null) return null;
        return (r, q, cb) -> cb.greaterThanOrEqualTo(r.get("expirationDate"), from);
    }

    public static Specification<CardEntity> expiryTo(LocalDate to) {
        if (to == null) return null;
        return (r, q, cb) -> cb.lessThanOrEqualTo(r.get("expirationDate"), to);
    }

    public static Specification<CardEntity> balanceMin(BigDecimal min) {
        if (min == null) return null;
        return (r, q, cb) -> cb.ge(r.get("balance"), min);
    }

    public static Specification<CardEntity> balanceMax(BigDecimal max) {
        if (max == null) return null;
        return (r, q, cb) -> cb.le(r.get("balance"), max);
    }

    public static Specification<CardEntity> createdFrom(Instant from) {
        if (from == null) return null;
        return (r, q, cb) -> cb.greaterThanOrEqualTo(r.get("createdAt"), from);
    }

    public static Specification<CardEntity> createdTo(Instant to) {
        if (to == null) return null;
        return (r, q, cb) -> cb.lessThanOrEqualTo(r.get("createdAt"), to);
    }

    /** q: search holder name or last4. (Avoid PAN since encrypted) */
    public static Specification<CardEntity> qLike(String qStr) {
        if (qStr == null || qStr.isBlank()) return null;
        return (r, q, cb) -> {
            String like = "%" + qStr.trim().toLowerCase() + "%";
            var userJoin = r.join("user"); // lazy okay in JPA query
            return cb.or(
                    cb.like(cb.lower(userJoin.get("fullName")), like),
                    cb.like(cb.lower(r.get("last4")), like) // last4 as string column
            );
        };
    }

    public static Specification<CardEntity> build(Long userId, CardFilter f) {
        Specification<CardEntity> s = Specification.allOf(
                ownerIs(userId),
                statusIn(f.status()),
                balanceMin(f.balanceMin())
        );
        return s.and(qLike(f.q()))
                .and(statusIn(f.status()))
                .and(expiryFrom(f.expiryFrom()))
                .and(expiryTo(f.expiryTo()))
                .and(balanceMin(f.balanceMin()))
                .and(balanceMax(f.balanceMax()))
                .and(createdFrom(f.createdFrom()))
                .and(createdTo(f.createdTo()));
    }
}
