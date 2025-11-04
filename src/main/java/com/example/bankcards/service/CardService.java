package com.example.bankcards.service;

import com.example.bankcards.dto.card.*;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.entity.TransferEntity;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.repository.spec.CardSpecs;
import com.example.bankcards.security.SecurityUtils;
import com.example.bankcards.util.mapper.CardMapper;
import jakarta.persistence.criteria.Join;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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


/**
 * Service layer responsible for managing {@link CardEntity} operations,
 * including creation, updates, deletion, transfers, and filtering.
 * <p>
 * This class acts as the core business logic layer between controllers
 * and repositories, ensuring proper authorization, validation, and
 * transactional consistency for all card-related actions.
 * <p>
 * Responsibilities include:
 * <ul>
 *   <li>Filtering cards by user, balance, status, or expiration date</li>
 *   <li>Creating and updating cards for users (admin and self-service)</li>
 *   <li>Handling fund transfers between user's own cards</li>
 *   <li>Processing block requests and status updates</li>
 *   <li>Enforcing security checks via {@link SecurityUtils} and {@link CurrentUserService}</li>
 * </ul>
 * <p>
 * Annotated with {@link org.springframework.stereotype.Service} and
 * {@link org.springframework.transaction.annotation.Transactional}
 * to ensure Spring-managed lifecycle and database transaction handling.
 *
 * @see CardRepository
 * @see TransferRepository
 * @see CardSpecs
 * @see CardMapper
 */
@Service
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final TransferRepository transferRepository;
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
                       TransferRepository transferRepository,
                       SecurityUtils securityUtils,
                       UserService userService,
                       CurrentUserService currentUserService) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.transferRepository = transferRepository;
        this.security = securityUtils;
        this.userService = userService;
        this.currentUserService = currentUserService;
    }


    /**
     * Retrieves a paginated list of cards matching the given filtering criteria.
     * <p>
     * This method is intended for {@code ADMIN} users and allows filtering
     * across all users' cards using optional parameters for search queries,
     * balance range, expiration date range, and status list.
     * <p>
     * The filtering is implemented using a dynamic {@link Specification}
     * composed at runtime based on the non-null parameters.
     *
     * @param q          optional text query (matches holder full name or last 4 digits)
     * @param userId     optional user ID to restrict results to a specific owner
     * @param minBalance minimum card balance (inclusive)
     * @param maxBalance maximum card balance (inclusive)
     * @param minDate    minimum expiration date (inclusive)
     * @param maxDate    maximum expiration date (inclusive)
     * @param statusCsv  comma-separated list of statuses to include
     * @param pageable   pagination and sorting information
     * @return paginated list of {@link CardEntity} objects matching the filters
     */
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

        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim().toLowerCase() + "%";
            spec = spec.and((r, qy, cb2) -> {
                Join<Object, Object> userJoin = r.join("user");
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

    /**
     * Retrieves a paginated list of the current user's cards that match the specified filter.
     * <p>
     * Internally delegates to {@link #filterForUser(Long, CardFilter, Pageable)}
     * using the authenticated user's ID.
     *
     * @param filter   filter parameters provided by the user
     * @param pageable pagination and sorting information
     * @return page of summarized card DTOs owned by the current user
     */
    @Transactional(readOnly = true)
    public Page<CardSummaryDTO> filterMine(CardFilter filter, Pageable pageable) {
        return filterForUser(security.currentUserId(), filter, pageable);
    }

    /**
     * Retrieves a paginated list of cards for a specific user based on the given filter.
     * <p>
     * Builds a composite {@link org.springframework.data.jpa.domain.Specification}
     * via {@link CardSpecs#build(Long, CardFilter)}.
     *
     * @param userId   ID of the user whose cards should be retrieved
     * @param filter   filtering criteria
     * @param pageable pagination and sorting information
     * @return page of summarized card DTOs belonging to the specified user
     */
    public Page<CardSummaryDTO> filterForUser(Long userId, CardFilter filter, Pageable pageable) {
        Specification<CardEntity> spec = CardSpecs.build(userId, filter);
        return cardRepository.findAll(spec, pageable).map(CardSummaryDTO::from);
    }

    /**
     * Updates the status of an existing card.
     *
     * @param id        ID of the card to update
     * @param newStatus new status value
     * @throws ResponseStatusException if the card is not found
     */
    @Transactional
    public void updateStatus(Long id, String newStatus) {
        CardEntity card = cardRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));

        card.setStatus(newStatus);
        cardRepository.save(card);
    }

    /**
     * Submits a block request for one of the current user's cards.
     * <p>
     * The card's status is updated to {@code BLOCK_REQUESTED}.
     *
     * @param id ID of the card to block
     * @throws ResponseStatusException if the card does not exist or is not owned by the user
     */
    @Transactional
    public void requestBlock(Long id) {
        Long me = security.currentUserId();
        CardEntity card = cardRepository.findByIdAndUser_Id(id, me)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        card.setStatus("BLOCK_REQUESTED");
        cardRepository.save(card);
    }


    /**
     * Transfers a specified amount between two cards owned by the current user.
     * <p>
     * The method validates ownership, card statuses, and available balance
     * before performing the transaction. Both card balances are updated
     * atomically within a single transaction, and the operation is logged
     * in the {@link TransferRepository}.
     * <p>
     * Only cards with {@code ACTIVE} status are eligible for transfers.
     *
     * @param fromCardId ID of the source card
     * @param toCardId   ID of the target card
     * @param amount     transfer amount (must be positive)
     * @throws ResponseStatusException if validation fails (e.g., insufficient funds,
     *                                 non-existent card, same card transfer, or forbidden access)
     */
    @Transactional
    public void transferBetweenMyCards(Long fromCardId, Long toCardId, BigDecimal amount) {
        if (fromCardId == null || toCardId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both fromCardId and toCardId are required");
        }
        if (fromCardId.equals(toCardId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot transfer to the same card");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }

        Long currentUserId = security.currentUserId();

        CardEntity from = cardRepository.findByIdAndUser_Id(fromCardId, currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Source card not found or not yours"));
        CardEntity to = cardRepository.findByIdAndUser_Id(toCardId, currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Target card not found or not yours"));

        String fromStatus = from.getStatus();
        String toStatus = to.getStatus();
        if (!"ACTIVE".equalsIgnoreCase(fromStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source card is not ACTIVE");
        }
        if (!"ACTIVE".equalsIgnoreCase(toStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target card is not ACTIVE");
        }

        BigDecimal fromBalance = from.getBalance() != null ? from.getBalance() : BigDecimal.ZERO;
        BigDecimal toBalance = to.getBalance() != null ? to.getBalance() : BigDecimal.ZERO;

        if (fromBalance.compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }

        // Apply balance updates
        from.setBalance(fromBalance.subtract(amount));
        to.setBalance(toBalance.add(amount));

        // Save in a consistent order to reduce lock contention
        if (from.getId() < to.getId()) {
            cardRepository.save(from);
            cardRepository.save(to);
        } else {
            cardRepository.save(to);
            cardRepository.save(from);
        }

        TransferEntity tx = new TransferEntity();
        tx.setUserId(currentUserId);
        tx.setFromCardId(from.getId());
        tx.setToCardId(to.getId());
        tx.setAmount(amount);
        tx.setStatus("COMPLETED");
        // createdAt is auto-set in entity
        transferRepository.save(tx);
    }


    /* ========================= READ ========================= */

    /**
     * Retrieves all cards visible to the current user.
     * <p>
     * Admin users receive all cards; normal users receive only their own.
     *
     * @return list of {@link CardResponseDTO} objects visible to the current user
     */
    @Transactional(readOnly = true)
    public List<CardResponseDTO> getAllCards() {
        final List<CardEntity> cards = security.isAdmin()
                ? cardRepository.findAll()
                : cardRepository.findAllByUser_Id(security.currentUserId());

        return cards.stream()
                .map(CardMapper::toResponse)
                .toList();
    }


    /**
     * Retrieves a specific card by its ID while enforcing ownership or admin access.
     *
     * @param cardId ID of the card to fetch
     * @return the corresponding {@link CardEntity}
     * @throws IllegalArgumentException if the card does not exist
     * @throws AccessDeniedException    if the user lacks permission
     */
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


    /**
     * Retrieves a card by ID and maps it to a response DTO, checking ownership.
     * <p>
     * Admin users can access any card; normal users can access only their own.
     *
     * @param id ID of the card to fetch
     * @return mapped {@link CardResponseDTO}
     * @throws ResponseStatusException if the card is not found
     */
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

    /**
     * Creates a new card for the currently authenticated user.
     * <p>
     * Initializes default values for balance and status if not provided.
     *
     * @param dto data for the new card
     * @return persisted {@link CardEntity}
     */
    @Transactional
    public CardEntity createForCurrentUser(CardCreateRequestDTO dto) {
        UserEntity me = currentUserService.requireCurrentUser();
        CardEntity card = CardMapper.toEntity(dto);

        if (card.getBalance() == null) card.setBalance(BigDecimal.ZERO);
        if (card.getStatus() == null)  card.setStatus("ACTIVE");

        card.setUser(me);
        return cardRepository.save(card);
    }


    /**
     * Creates a new card for a specific user (admin-only operation).
     * <p>
     * Initializes default values for balance and status if not provided.
     *
     * @param userId ID of the target user
     * @param dto    data for the new card
     * @return persisted {@link CardEntity}
     * @throws IllegalArgumentException if the user does not exist
     */
    @Transactional
    public CardEntity createForUser(Long userId, CardCreateRequestDTO dto) {
        UserEntity owner = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        CardEntity card = CardMapper.toEntity(dto);

        if (card.getBalance() == null) card.setBalance(BigDecimal.ZERO);
        if (card.getStatus() == null)  card.setStatus("ACTIVE");

        card.setUser(owner);
        return cardRepository.save(card);
    }

    /* ========================= UPDATE ========================= */

    /**
     * Updates the expiration date and/or status of a card identified by its ID.
     * <p>
     * Although typically restricted to {@code ADMIN} users by the controller,
     * this method includes an additional ownership check for safety.
     * <p>
     * The expiration date string is parsed from the {@code MM/yy} format into
     * a {@link LocalDate} object using {@link #MM_YY_TO_LOCALDATE}.
     *
     * @param id  ID of the card to update
     * @param req request payload containing the new expiry and/or status values
     * @return updated {@link CardEntity} instance
     * @throws IllegalArgumentException if the card is not found
     * @throws AccessDeniedException    if the user is neither the card owner nor an admin
     */
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

    /**
     * Deletes a card by its ID.
     * <p>
     * Only the card owner or an admin can perform this operation.
     *
     * @param id ID of the card to delete
     * @throws ResponseStatusException if the card is not found
     * @throws AccessDeniedException   if the user lacks permission
     */
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

    /**
     * Filters cards based on various criteria such as user ID, balance,
     * expiration date, and status. Used by older controller endpoints.
     * <p>
     * For admin users, results can span all users; for regular users,
     * filters are automatically restricted to their own cards.
     *
     * @param userId     optional user ID filter
     * @param minBalance minimum balance
     * @param maxBalance maximum balance
     * @param minDate    minimum expiration date
     * @param maxDate    maximum expiration date
     * @param status     status filter
     * @return list of matching {@link CardEntity} objects
     */
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

    /**
     * Utility method that builds a {@link ResponseStatusException}
     * for consistent 404 responses.
     *
     * @param id ID of the missing card
     * @return a NOT_FOUND exception with formatted message
     */
    private ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Card %d not found".formatted(id));
    }
}
