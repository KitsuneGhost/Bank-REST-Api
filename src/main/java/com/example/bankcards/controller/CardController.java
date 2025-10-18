package com.example.bankcards.controller;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.service.CardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
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

    @PostMapping("/users/{userId}")
    public CardEntity createCardForUser(
            @PathVariable Long userId,
            @RequestBody CardEntity card) {
        return cardService.createCardForUser(userId, card);
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
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth minDate,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth maxDate,
            @RequestParam(required = false) String status) {

        return cardService.filterCards(userId, minBalance, maxBalance, minDate, maxDate, status);
    }
}
