package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardCreateRequestDTO;
import com.example.bankcards.dto.card.CardResponseDTO;
import com.example.bankcards.dto.card.CardUpdateRequestDTO;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.mapper.CardMapper;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Validated
@RestController
@RequestMapping("/cards")
public class CardController {
    private final CardService cardService;

    public CardController (CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<CardResponseDTO> getAllCards() {
        return cardService.getAllCards();
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
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth minDate,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth maxDate,
            @RequestParam(required = false) String status) {

        var cards = cardService.filterCards(userId, minBalance, maxBalance, minDate, maxDate, status);
        return cards.stream().map(CardMapper::toResponse).toList();
    }
}
