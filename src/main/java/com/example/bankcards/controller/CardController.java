package com.example.bankcards.controller;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.service.CardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/cards")
public class CardController {
    private final CardService cardService;

    public CardController (CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public List<CardEntity> getAllCards() {
        return cardService.getAllCards();
    }

    @PostMapping
    public CardEntity createCard(@RequestBody CardEntity card) {
        return cardService.createCard(card);
    }

    @PutMapping("/{id}")
    public CardEntity updateCard(@PathVariable Long id, @RequestBody CardEntity card) {
        return cardService.updateCard(id, card);
    }

    @DeleteMapping("/{id}")
    public void deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
    }

    @GetMapping("/{id}")
    public CardEntity getCardById(@PathVariable Long id) {
        return cardService.findById(id);
    }

    @GetMapping("/search/{cardNumber}")
    public CardEntity getCardByNumber(@PathVariable String cardNumber) {
        return cardService.findByCardNumber(cardNumber);
    }

    @GetMapping("/filter")
    public List<CardEntity> filterCards(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Float minBalance,
            @RequestParam(required = false) Float maxBalance,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date minDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date maxDate,
            @RequestParam(required = false) String status) {

        // User + Status
        if (userId != null && status != null)
            return cardService.findByUserIdAndStatus(userId, status);

        // User + Date
        if (userId != null) {
            if (minDate != null && maxDate != null)
                return cardService.findByUserIdAndExpirationDateBetween(userId, minDate, maxDate);
            if (minDate != null)
                return cardService.findByUserIdAndExpirationDateAfter(userId, minDate);
            if (maxDate != null)
                return cardService.findByUserIdAndExpirationDateBefore(userId, maxDate);
        }

        // User + Balance
        if (userId != null) {
            if (minBalance != null && maxBalance != null)
                return cardService.findByUserIdAndBalanceBetween(userId, minBalance, maxBalance);
            if (minBalance != null)
                return cardService.findByUserIdAndBalanceGreaterThan(userId, minBalance);
            if (maxBalance != null)
                return cardService.findByUserIdAndBalanceLessThan(userId, maxBalance);
            return cardService.findAllByUserId(userId);
        }

        // Balance only
        if (minBalance != null && maxBalance != null)
            return cardService.findByBalanceBetween(minBalance, maxBalance);
        if (minBalance != null)
            return cardService.findByBalanceGreaterThan(minBalance);
        if (maxBalance != null)
            return cardService.findByBalanceLessThan(maxBalance);

        // Date only
        if (minDate != null && maxDate != null)
            return cardService.findByExpirationDateBetween(minDate, maxDate);
        if (minDate != null)
            return cardService.findByExpirationDateAfter(minDate);
        if (maxDate != null)
            return cardService.findByExpirationDateBefore(maxDate);

        // Status only
        if (status != null)
            return cardService.findAllByStatus(status);

        // Default
        return cardService.getAllCards();
    }
}
