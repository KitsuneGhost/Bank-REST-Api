package com.example.bankcards.controller;

import com.example.bankcards.dto.card.*;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.mapper.CardMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * REST controller responsible for managing bank cards.
 * <p>
 * Provides endpoints for both regular users and administrators to:
 * <ul>
 *   <li>View and filter cards (paged and filtered search)</li>
 *   <li>Create cards (for themselves or for specific users)</li>
 *   <li>Update, activate, block, or delete cards</li>
 *   <li>Perform transfers between owned cards</li>
 * </ul>
 *
 * <p><b>Access control:</b>
 * <ul>
 *   <li>Regular users ({@code ROLE_USER}) can manage and view only their own cards.</li>
 *   <li>Administrators ({@code ROLE_ADMIN}) can view and manage all cards system-wide.</li>
 * </ul>
 *
 * <p>All endpoints require JWT authentication (configured under {@code bearerAuth}).</p>
 *
 * @see com.example.bankcards.service.CardService
 * @see com.example.bankcards.dto.card.CardCreateRequestDTO
 * @see com.example.bankcards.dto.card.CardResponseDTO
 * @see com.example.bankcards.dto.card.CardSummaryDTO
 * @see com.example.bankcards.dto.card.CardUpdateRequestDTO
 * @see com.example.bankcards.dto.card.TransferRequestDTO
 */
@Tag(name = "Cards", description = "Manage bank cards, filters, transfers")
@SecurityRequirement(name = "bearerAuth")
@Validated
@RestController
@RequestMapping("/cards")
public class CardController {
    private final CardService cardService;

    private static final java.util.Set<String> SORT_WHITELIST =
            java.util.Set.of("id","status","expirationDate","balance","createdAt","updatedAt");

    public CardController (CardService cardService) {
        this.cardService = cardService;
    }


    /* ========================= FILTERS ========================= */


    /**
     * Retrieves a paginated and filtered list of all cards (Admin only).
     * <p>
     * Supports filtering by user ID, balance range, expiration date range,
     * status, and full-text query (holder name or last 4 digits).
     *
     * @param page        page number (default: 0)
     * @param size        page size (default: 12, max: 100)
     * @param sort        sort criteria (e.g. "createdAt,desc"), supports whitelisted fields
     * @param userId      filter by owner user ID
     * @param minBalance  filter for minimum balance
     * @param maxBalance  filter for maximum balance
     * @param minDate     filter for minimum expiration date (yyyy-MM-dd)
     * @param maxDate     filter for maximum expiration date (yyyy-MM-dd)
     * @param status      filter by status (ACTIVE/BLOCKED/BLOCK_REQUESTED)
     * @param q           free-text search (holder name or last4)
     * @return paginated {@link PageResponse} of {@link CardResponseDTO} items
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Admin: list all cards (paged)",
            description = "Filter by userId/balance/date/status/q." +
                    " Sort supports a whitelist of fields using tokens like 'createdAt,desc;brand,asc'."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    public PageResponse<CardResponseDTO> listAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) BigDecimal minBalance,
            @RequestParam(required = false) BigDecimal maxBalance,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate minDate,  // yyyy-MM-dd
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate maxDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), parseSort(sort));
        Page<CardResponseDTO> pageRes = cardService
                .filterAll(q, userId, minBalance, maxBalance, minDate, maxDate, status, pageable)
                .map(CardMapper::toResponse);
        return PageResponse.from(pageRes, sort);
    }


    /**
     * Retrieves a paginated list of cards owned by the authenticated user.
     * <p>
     * Supports the same filters as admin listing, but scoped to the current user.
     *
     * @param filter   optional {@link CardFilter} defining query parameters
     * @param pageable Spring pageable definition (page, size, sort)
     * @return {@link Page} of {@link CardSummaryDTO} cards belonging to the user
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(
            summary = "My cards (paged)",
            description = "Returns the authenticated user's cards" +
                    " with standard Pageable (page/size/sort) and CardFilter."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public Page<CardSummaryDTO> myCards(CardFilter filter, Pageable pageable) {
        return cardService.filterMine(filter, pageable);
    }


    /* ========================= CREATE CARDS ========================= */


    /**
     * Creates a new card for the currently authenticated user.
     *
     * @param req card creation request
     * @return {@link CardResponseDTO} representing the created card
     */
    @PostMapping("/me/create")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(
            summary = "Create a card for me",
            description = "Creates a new card bound to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Created",
                    content = @Content(schema = @Schema(implementation = com.example.bankcards.dto.card.CardResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    })
    public CardResponseDTO createForMe(@Valid @RequestBody CardCreateRequestDTO req) {
        CardEntity saved = cardService.createForCurrentUser(req);
        return CardMapper.toResponse(saved);
    }


    /**
     * Creates a new card for a specific user (Admin only).
     *
     * @param userId target user's ID
     * @param req    card creation request
     * @return {@link CardResponseDTO} representing the created card
     */
    @PostMapping("/users/{userId}/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Admin: create a card for a specific user",
            description = "Creates a card for the target userId."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Created",
                    content = @Content(schema = @Schema(implementation = com.example.bankcards.dto.card.CardResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    public CardResponseDTO createForUser(@PathVariable Long userId, @Valid @RequestBody CardCreateRequestDTO req) {
        CardEntity saved = cardService.createForUser(userId, req);
        return CardMapper.toResponse(saved);
    }


    /**
     * Transfers money between the authenticated user's own cards.
     *
     * @param req transfer request containing source, target, and amount
     */
    @PostMapping("/me/transfers")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Transfer between my cards",
            description = "Creates an internal transfer for the authenticated user. Business rules and limits apply."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid transfer request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public void transfer(@RequestBody @Valid TransferRequestDTO req) {
        cardService.transferBetweenMyCards(req.fromCardId(), req.toCardId(), req.amount());
    }


    /**
     * Blocks a card by ID (Admin only).
     *
     * @param id card ID
     * @return 204 No Content
     */
    @PatchMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Block card", description = "Switches card status to 'BLOCKED' by Admins;")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    public ResponseEntity<Void> blockCard(@PathVariable Long id) {
        cardService.updateStatus(id, "BLOCKED");
        return ResponseEntity.noContent().build();
    }


    /**
     * Activates a card by ID (Admin only).
     *
     * @param id card ID
     * @return 204 No Content
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate card", description = "Switches card status to 'ACTIVE' by Admins;")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    public ResponseEntity<Void> activateCard(@PathVariable Long id) {
        cardService.updateStatus(id, "ACTIVE");
        return ResponseEntity.noContent().build();
    }


    /**
     * Requests card block (for user-owned cards).
     *
     * @param id card ID
     * @return 204 No Content
     */
    @PatchMapping("/me/{id}/request-block")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Request to block a card", description = "Switches card status to 'BLOCK_REQUESTED';")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    public ResponseEntity<Void> requestBlock(@PathVariable Long id) {
        cardService.requestBlock(id);
        return ResponseEntity.noContent().build();
    }


    /* ========================= BASIC CRUD ========================= */


    /**
     * Retrieves a card by ID.
     * <p>
     * Admins can access any card; users can only access their own.
     *
     * @param id card ID
     * @return {@link CardResponseDTO} representing the requested card
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Get card by id", description = "Card owner or ADMIN can view.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = CardResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    public CardResponseDTO getById(@PathVariable Long id) {
        CardEntity c = cardService.getByIdAuthorized(id);
        return CardMapper.toResponse(c);
    }


    /**
     * Updates card attributes (Admin only).
     *
     * @param id  card ID
     * @param req update request
     * @return {@link CardResponseDTO} representing the updated card
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: update card", description = "Updates allowed fields on the card.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = CardResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    public CardResponseDTO updateCard(
            @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody CardUpdateRequestDTO req) {
        CardEntity updated = cardService.updateCard(id, req); // service applies changes safely
        return CardMapper.toResponse(updated);
    }


    /**
     * Deletes a card by ID (Admin only).
     *
     * @param id card ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Admin: delete card", description = "Deletes the card by id.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "No Content", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    public void deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
    }


    /* ========================= HELPERS ========================= */


    /**
     * Parses and validates the sort query parameter, ensuring only safe fields are sortable.
     *
     * @param sort the raw sort parameter (e.g. "createdAt,desc;balance,asc")
     * @return a {@link Sort} object containing validated sort fields
     */
    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Order.desc("createdAt"));
        }
        java.util.List<Sort.Order> orders = new java.util.ArrayList<>();
        for (String token : sort.split(";")) {
            String[] parts = token.split(",", 2);
            String field = parts[0].trim();
            if (!SORT_WHITELIST.contains(field)) continue; // ignore unknown/unsafe fields
            Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            orders.add(new Sort.Order(dir, field));
        }
        return orders.isEmpty() ? Sort.by(Sort.Order.desc("createdAt")) : Sort.by(orders);
    }
}
