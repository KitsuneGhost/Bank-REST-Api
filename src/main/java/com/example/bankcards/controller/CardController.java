package com.example.bankcards.controller;

import com.example.bankcards.dto.card.*;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.mapper.CardMapper;
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
import java.util.List;

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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
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
    public Page<CardSummaryDTO> myCards(CardFilter filter, Pageable pageable) {
        return cardService.filterMine(filter, pageable);
    }

    // Create card for the authenticated user
    @PostMapping("/me/create")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public CardResponseDTO createForMe(@Valid @RequestBody CardCreateRequestDTO req) {
        CardEntity saved = cardService.createForCurrentUser(req);
        return CardMapper.toResponse(saved);
    }

    // Admin: create a card for a specific user
    @PostMapping("/users/{userId}/create")
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponseDTO createForUser(@PathVariable Long userId, @Valid @RequestBody CardCreateRequestDTO req) {
        CardEntity saved = cardService.createForUser(userId, req);
        return CardMapper.toResponse(saved);
    }

    @PostMapping("/me/transfers")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void transfer(@RequestBody @Valid TransferRequestDTO req) {
        cardService.transferBetweenMyCards(req.fromCardId(), req.toCardId(), req.amount());
    }

    @PatchMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> blockCard(@PathVariable Long id) {
        cardService.updateStatus(id, "BLOCKED");
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateCard(@PathVariable Long id) {
        cardService.updateStatus(id, "ACTIVE");
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/{id}/request-block")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Void> requestBlock(@PathVariable Long id) {
        cardService.requestBlock(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public CardResponseDTO getById(@PathVariable Long id) {
        CardEntity c = cardService.getByIdAuthorized(id);
        return CardMapper.toResponse(c);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CardResponseDTO updateCard(
            @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody CardUpdateRequestDTO req) {
        CardEntity updated = cardService.updateCard(id, req); // service applies changes safely
        return CardMapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
    }

    @GetMapping("/filter")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<CardResponseDTO> filterCards(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) BigDecimal minBalance,
            @RequestParam(required = false) BigDecimal maxBalance,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") LocalDate minDate,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") LocalDate maxDate,
            @RequestParam(required = false) String status) {

        List<CardEntity> cards = cardService.filterCards(userId, minBalance, maxBalance, minDate, maxDate, status);
        return cards.stream().map(CardMapper::toResponse).toList();
    }

    /** Accepts "field,asc" or "field,desc" and also multiple: "status,asc;expirationDate,desc" */
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
