package com.example.bankcards.repository.spec;

import com.example.bankcards.dto.card.CardFilter;
import com.example.bankcards.entity.CardEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

/**
 * Utility class providing reusable {@link Specification} definitions for {@link CardEntity}.
 * <p>
 * Each static method returns a {@link Specification} representing a filtering condition,
 * allowing dynamic and type-safe query construction using Spring Data JPA.
 * <p>
 * The specifications are primarily used by {@code CardService} and {@code CardRepository}
 * to build complex search and filtering logic for cards based on user, balance, status,
 * expiration date, and other attributes.
 * <p>
 * This class is not meant to be instantiated.
 */
public final class CardSpecs {

    /** Private constructor to prevent instantiation. */
    private CardSpecs() {}

    /**
     * Returns a specification that matches cards owned by the specified user.
     *
     * @param userId ID of the card owner
     * @return specification filtering cards by owner ID
     */
    public static Specification<CardEntity> ownerIs(Long userId) {
        return (r, q, cb) -> cb.equal(r.get("user").get("id"), userId);
    }

    /**
     * Returns a specification that matches cards whose status is included in the given set.
     * If the set is null or empty, the specification is {@code null}.
     *
     * @param statuses set of allowed statuses
     * @return specification filtering by status, or {@code null} if none provided
     */
    public static Specification<CardEntity> statusIn(Set<String> statuses) {
        if (statuses == null || statuses.isEmpty()) return null;
        return (r, q, cb) -> r.get("status").in(statuses);
    }

    /**
     * Returns a specification that matches cards expiring on or after the given date.
     *
     * @param from earliest expiration date (inclusive)
     * @return specification filtering by minimum expiration date, or {@code null} if {@code from} is null
     */
    public static Specification<CardEntity> expiryFrom(LocalDate from) {
        if (from == null) return null;
        return (r, q, cb) -> cb.greaterThanOrEqualTo(r.get("expirationDate"), from);
    }

    /**
     * Returns a specification that matches cards expiring on or before the given date.
     *
     * @param to latest expiration date (inclusive)
     * @return specification filtering by maximum expiration date, or {@code null} if {@code to} is null
     */
    public static Specification<CardEntity> expiryTo(LocalDate to) {
        if (to == null) return null;
        return (r, q, cb) -> cb.lessThanOrEqualTo(r.get("expirationDate"), to);
    }

    /**
     * Returns a specification that matches cards with balance greater than or equal to the given value.
     *
     * @param min minimum balance (inclusive)
     * @return specification filtering by minimum balance, or {@code null} if {@code min} is null
     */
    public static Specification<CardEntity> balanceMin(BigDecimal min) {
        if (min == null) return null;
        return (r, q, cb) -> cb.ge(r.get("balance"), min);
    }

    /**
     * Returns a specification that matches cards with balance less than or equal to the given value.
     *
     * @param max maximum balance (inclusive)
     * @return specification filtering by maximum balance, or {@code null} if {@code max} is null
     */
    public static Specification<CardEntity> balanceMax(BigDecimal max) {
        if (max == null) return null;
        return (r, q, cb) -> cb.le(r.get("balance"), max);
    }

    /**
     * Returns a specification that matches cards created on or after the specified timestamp.
     *
     * @param from earliest creation time (inclusive)
     * @return specification filtering by creation start time, or {@code null} if {@code from} is null
     */
    public static Specification<CardEntity> createdFrom(Instant from) {
        if (from == null) return null;
        return (r, q, cb) -> cb.greaterThanOrEqualTo(r.get("createdAt"), from);
    }

    /**
     * Returns a specification that matches cards created on or before the specified timestamp.
     *
     * @param to latest creation time (inclusive)
     * @return specification filtering by creation end time, or {@code null} if {@code to} is null
     */
    public static Specification<CardEntity> createdTo(Instant to) {
        if (to == null) return null;
        return (r, q, cb) -> cb.lessThanOrEqualTo(r.get("createdAt"), to);
    }


    /**
     * Returns a specification that performs a case-insensitive search by owner's full name
     * or the last four digits of the card number.
     * <p>
     * This search avoids using the full PAN since it is stored in encrypted form.
     *
     * @param qStr search query (partial text)
     * @return specification for name or last4 lookup, or {@code null} if {@code qStr} is blank
     */
    public static Specification<CardEntity> qLike(String qStr) {
        if (qStr == null || qStr.isBlank()) return null;
        return (r, q, cb) -> {
            String like = "%" + qStr.trim().toLowerCase() + "%";
            var userJoin = r.join("user");
            return cb.or(
                    cb.like(cb.lower(userJoin.get("fullName")), like),
                    cb.like(cb.lower(r.get("last4")), like) // last4 as string column
            );
        };
    }

    /**
     * Builds a combined specification using the provided user ID and {@link CardFilter}.
     * <p>
     * The resulting specification includes filters for user ownership, status, balance range,
     * expiration range, creation timestamps, and text search.
     *
     * @param userId ID of the card owner
     * @param f      filter parameters
     * @return composite specification combining all applicable filters
     */
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
