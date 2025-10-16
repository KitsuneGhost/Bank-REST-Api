package com.example.bankcards.service;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.repository.CardRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CardService {
    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public List<CardEntity> getAllCards() {
        return cardRepository.findAll();
    }

    public CardEntity createCard(CardEntity card) {

        return cardRepository.save(card);
    }

    @Transactional
    public CardEntity updateCard(Long id, CardEntity updCard) {
        CardEntity existingCard = cardRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Card was Not Found")
        );

        BeanUtils.copyProperties(updCard, existingCard, "id", "cardNumber");

        return cardRepository.save(existingCard);
    }

    public void deleteCard(Long id) {
        cardRepository.deleteById(id);
    }

    public CardEntity findById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));
    }

    public List<CardEntity> findByUser(UserEntity user) {
        return cardRepository.findByUser(user);
    }

    public List<CardEntity> findAllByUserId(Long id) {
        return cardRepository.findAllByUserId(id);
    }

    public CardEntity findByCardNumber(String cardNumber) {
        return cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException("Card not found"));
    }

    public List<CardEntity> findByBalanceGreaterThan(Float balance) {
        return cardRepository.findByBalanceGreaterThan(balance);
    }

    public List<CardEntity> findByBalanceLessThan(Float balance) {
        return cardRepository.findByBalanceLessThan(balance);
    }

    public List<CardEntity> findByBalanceBetween(Float minBalance, Float maxBalance) {
        return cardRepository.findByBalanceBetween(minBalance, maxBalance);
    }

    public List<CardEntity> findByExpirationDateBefore(Date date) {
        return cardRepository.findByExpirationDateBefore(date);
    }

    public List<CardEntity> findByExpirationDateAfter(Date date) {
        return cardRepository.findByExpirationDateAfter(date);
    }

    public List<CardEntity> findByExpirationDateBetween(Date minDate, Date maxDate) {
        return cardRepository.findByExpirationDateBetween(minDate, maxDate);
    }

    public List<CardEntity> findAllByStatus(String status) {
        return cardRepository.findAllByStatus(status);
    }

    public List<CardEntity> findByUserAndStatus(UserEntity user, String status) {
        return cardRepository.findByUserAndStatus(user, status);
    }

    public List<CardEntity> findByUserIdAndStatus(Long userId, String status) {
        return cardRepository.findByUserIdAndStatus(userId, status);
    }

    public List<CardEntity> findByUserIdAndBalanceBetween(Long userId, Float min, Float max) {
        return cardRepository.findByUserIdAndBalanceBetween(userId, min, max);
    }

    public List<CardEntity> findByUserIdAndBalanceGreaterThan(Long userId, Float min) {
        return cardRepository.findByUserIdAndBalanceGreaterThan(userId, min);
    }

    public List<CardEntity> findByUserIdAndBalanceLessThan(Long userId, Float max) {
        return cardRepository.findByUserIdAndBalanceLessThan(userId, max);
    }

    public List<CardEntity> findByUserIdAndExpirationDateBefore(Long id, Date date) {
        return cardRepository.findByUserIdAndExpirationDateBefore(id, date);
    }

    public List<CardEntity> findByUserIdAndExpirationDateAfter(Long id, Date date) {
        return cardRepository.findByUserIdAndExpirationDateAfter(id, date);
    }

    public List<CardEntity> findByUserIdAndExpirationDateBetween(Long id, Date minDate, Date maxDate) {
        return cardRepository.findByUserIdAndExpirationDateBetween(id, minDate, maxDate);
    }
}
