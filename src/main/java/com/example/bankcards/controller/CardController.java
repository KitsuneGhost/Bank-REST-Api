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
