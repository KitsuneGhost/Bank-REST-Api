package com.example.bankcards.service;

import com.example.bankcards.dto.card.*;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.repository.spec.CardSpecs;
import com.example.bankcards.security.SecurityUtils;
import com.example.bankcards.util.mapper.CardMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final SecurityUtils security;
    private final UserService userService;
    private final CurrentUserService currentUserService; // reads SecurityContext

    private static final DateTimeFormatter MM_YY_TO_LOCALDATE =
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("MM/yy")
                    .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                    .toFormatter();

    public CardService(CardRepository cardRepository,
                       UserRepository userRepository,
                       SecurityUtils securityUtils,
                       UserService userService,
                       CurrentUserService currentUserService) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.security = securityUtils;
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    /** ADMIN: filter across ALL users */
    public Page<CardEntity> filterAll(
            String q,
            Long userId,
            BigDecimal minBalance,
            BigDecimal maxBalance,
            LocalDate minDate,
            LocalDate maxDate,
            String statusCsv,
            Pageable pageable
    ) {
        Specification<CardEntity> spec = (root, query, cb) -> cb.conjunction();

        // q: holder name or last4 (if you added last4). Avoid encrypted PAN.
        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim().toLowerCase() + "%";
            spec = spec.and((r, qy, cb2) -> {
                var userJoin = r.join("user");
                // If you do NOT have last4 column yet, remove the second part of OR
                return cb2.or(
                        cb2.like(cb2.lower(userJoin.get("fullName")), like),
                        cb2.like(cb2.lower(r.get("last4")), like)
                );
            });
        }

        if (userId != null) {
            spec = spec.and((r, qy, cb2) -> cb2.equal(r.get("user").get("id"), userId));
        }

        if (minBalance != null) {
            spec = spec.and((r, qy, cb2) -> cb2.ge(r.get("balance"), minBalance));
        }
        if (maxBalance != null) {
            spec = spec.and((r, qy, cb2) -> cb2.le(r.get("balance"), maxBalance));
        }

        if (minDate != null) {
            spec = spec.and((r, qy, cb2) -> cb2.greaterThanOrEqualTo(r.get("expirationDate"), minDate));
        }
        if (maxDate != null) {
            spec = spec.and((r, qy, cb2) -> cb2.lessThanOrEqualTo(r.get("expirationDate"), maxDate));
        }

        if (statusCsv != null && !statusCsv.isBlank()) {
            Set<String> statuses = Arrays.stream(statusCsv.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
            spec = spec.and((r, qy, cb2) -> r.get("status").in(statuses));
        }

        return cardRepository.findAll(spec, pageable);
    }

    public Page<CardEntity> filterMine(
            String q,
            BigDecimal minBalance,
            BigDecimal maxBalance,
            LocalDate minDate,
            LocalDate maxDate,
            String statusCsv,
            Pageable pageable
    ) {
        Long currentUserId = userService.getCurrentUserEntity().getId();
        Specification<CardEntity> spec = (r, qy, cb) -> cb.equal(r.get("user").get("id"), currentUserId);

        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim().toLowerCase() + "%";
            // If no last4 column, you can drop to only holder name via join
            spec = spec.and((r, qqq, cb2) -> cb2.like(cb2.lower(r.get("last4")), like));
        }
        if (minBalance != null) spec = spec.and((r, qy, cb2) -> cb2.ge(r.get("balance"), minBalance));
        if (maxBalance != null) spec = spec.and((r, qy, cb2) -> cb2.le(r.get("balance"), maxBalance));
        if (minDate != null) spec = spec.and((r, qy, cb2) -> cb2.greaterThanOrEqualTo(r.get("expirationDate"), minDate));
        if (maxDate != null) spec = spec.and((r, qy, cb2) -> cb2.lessThanOrEqualTo(r.get("expirationDate"), maxDate));
        if (statusCsv != null && !statusCsv.isBlank()) {
            Set<String> statuses = Arrays.stream(statusCsv.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
            spec = spec.and((r, qy, cb2) -> r.get("status").in(statuses));
        }
        return cardRepository.findAll(spec, pageable);
    }

    public Page<CardSummaryDTO> filterForUser(Long userId, CardFilter filter, Pageable pageable) {
        Specification<CardEntity> spec = CardSpecs.build(userId, filter);
        return cardRepository.findAll(spec, pageable).map(CardSummaryDTO::from);
    }

    /* ========================= READ ========================= */

    @Transactional(readOnly = true)
    public List<CardResponseDTO> getAllCards() {
        final List<CardEntity> cards = security.isAdmin()
                ? cardRepository.findAll()
                : cardRepository.findAllByUser_Id(security.currentUserId());

        return cards.stream()
                .map(CardMapper::toResponse)
                .toList();
    }

    // Used by GET /cards/{id}
    @Transactional(readOnly = true)
    public CardEntity getByIdAuthorized(Long cardId) {
        if (security.isAdmin()) {
            return cardRepository.findById(cardId)
                    .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardId));
        }
        Long me = security.currentUserId();
        return cardRepository.findByIdAndUser_Id(cardId, me)
                .orElseThrow(() -> new AccessDeniedException("Forbidden"));
    }

    // (Controller uses this to map to DTO)
    @Transactional(readOnly = true)
    public CardResponseDTO getOwnedCard(Long id) {
        UserEntity me = userService.getCurrentUserEntity();
        boolean isAdmin = "ADMIN".equals(me.getRoles());
        CardEntity card = isAdmin
                ? cardRepository.findById(id).orElseThrow(() -> notFound(id))
                : cardRepository.findByIdAndUser_Id(id, me.getId()).orElseThrow(() -> notFound(id));
        return CardMapper.toResponse(card);
    }

    /* ========================= CREATE ========================= */

    // POST /cards/me/create
    @Transactional
    public CardEntity createForCurrentUser(CardCreateRequestDTO dto) {
        UserEntity me = currentUserService.requireCurrentUser();
        CardEntity card = CardMapper.toEntity(dto);

        // Satisfy NOT NULLs
        if (card.getBalance() == null) card.setBalance(BigDecimal.ZERO);
        if (card.getStatus() == null)  card.setStatus("ACTIVE");

        card.setUser(me);
        return cardRepository.save(card);
    }

    // POST /cards/users/{userId}/create (ADMIN)
    @Transactional
    public CardEntity createForUser(Long userId, CardCreateRequestDTO dto) {
        UserEntity owner = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        CardEntity card = CardMapper.toEntity(dto);

        // Satisfy NOT NULLs
        if (card.getBalance() == null) card.setBalance(BigDecimal.ZERO);
        if (card.getStatus() == null)  card.setStatus("ACTIVE");

        card.setUser(owner);
        return cardRepository.save(card);
    }

    /* ========================= UPDATE ========================= */

    // PUT /cards/{id}  (ADMIN by controller, but keep owner-or-admin guard for safety)
    @Transactional
    public CardEntity updateCard(Long id, CardUpdateRequestDTO req) {
        CardEntity c = cardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + id));

        // Extra guard (controller already restricts to ADMIN, but safe to keep)
        if (!security.isAdmin()) {
            Long me = security.currentUserId();
            if (!c.getUserId().equals(me)) {
                throw new AccessDeniedException("Forbidden");
            }
        }

        if (req.expiry() != null) {
            c.setExpirationDate(LocalDate.parse(req.expiry(), MM_YY_TO_LOCALDATE));
        }
        if (req.status() != null) {
            c.setStatus(req.status());
        }
        return cardRepository.save(c);
    }

    /* ========================= DELETE ========================= */

    // DELETE /cards/{id}
    @Transactional
    public void deleteCard(Long id) {
        CardEntity c = cardRepository.findById(id)
                .orElseThrow(() -> notFound(id));

        if (!security.isAdmin()) {
            Long me = security.currentUserId();
            if (!c.getUserId().equals(me)) {
                throw new AccessDeniedException("Forbidden");
            }
        }
        cardRepository.delete(c);
    }

    /* ========================= FILTER ========================= */

    // GET /cards/filter (controller already handles roles)
    @Transactional(readOnly = true)
    public List<CardEntity> filterCards(Long userId,
                                        BigDecimal minBalance,
                                        BigDecimal maxBalance,
                                        LocalDate minDate,
                                        LocalDate maxDate,
                                        String status) {

        // If caller is not admin, force their own user scope
        Long effectiveUserId = security.isAdmin() ? userId : security.currentUserId();

        if (effectiveUserId != null && status != null) {
            return cardRepository.findByUser_IdAndStatus(effectiveUserId, status);
        }

        if (effectiveUserId != null) {
            if (minDate != null && maxDate != null)
                return cardRepository.findByUser_IdAndExpirationDateBetween(effectiveUserId, minDate, maxDate);
            if (minDate != null)
                return cardRepository.findByUser_IdAndExpirationDateAfter(effectiveUserId, minDate);
            if (maxDate != null)
                return cardRepository.findByUser_IdAndExpirationDateBefore(effectiveUserId, maxDate);

            if (minBalance != null && maxBalance != null)
                return cardRepository.findByUser_IdAndBalanceBetween(effectiveUserId, minBalance, maxBalance);
            if (minBalance != null)
                return cardRepository.findByUser_IdAndBalanceGreaterThan(effectiveUserId, minBalance);
            if (maxBalance != null)
                return cardRepository.findByUser_IdAndBalanceLessThan(effectiveUserId, maxBalance);

            return cardRepository.findAllByUser_Id(effectiveUserId);
        }

        // Admin-only broader filters
        if (minBalance != null && maxBalance != null)
            return cardRepository.findByBalanceBetween(minBalance, maxBalance);
        if (minBalance != null)
            return cardRepository.findByBalanceGreaterThan(minBalance);
        if (maxBalance != null)
            return cardRepository.findByBalanceLessThan(maxBalance);

        if (minDate != null && maxDate != null)
            return cardRepository.findByExpirationDateBetween(minDate, maxDate);
        if (minDate != null)
            return cardRepository.findByExpirationDateAfter(minDate);
        if (maxDate != null)
            return cardRepository.findByExpirationDateBefore(maxDate);

        if (status != null)
            return cardRepository.findAllByStatus(status);

        return cardRepository.findAll();
    }

    /* ========================= Helpers ========================= */

    private ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Card %d not found".formatted(id));
    }
}
