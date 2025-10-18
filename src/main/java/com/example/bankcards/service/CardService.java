package com.example.bankcards.service;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.Date;
import java.util.List;

@Service
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final SecurityUtils security;

    public CardService(CardRepository cardRepository, UserRepository userRepository, SecurityUtils securityUtils) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.security = securityUtils;
    }

    public List<CardEntity> getAllCards() {
        if (security.isAdmin()) {
            return cardRepository.findAll();
        }
        return cardRepository.findAllByUser_Id(security.currentUserId());
    }

    public CardEntity createCardForUser(Long userId, CardEntity card) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        card.setUser(user);
        return cardRepository.save(card);
    }

    @Transactional
    public CardEntity updateCard(Long id, CardEntity updCard) {
        CardEntity existingCard = cardRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Card was Not Found")
        );

        BeanUtils.copyProperties(updCard, existingCard, "id", "cardNumber", "user", "cvv",
                "expirationDate");

        return cardRepository.save(existingCard);
    }

    public void deleteCard(Long id) {
        cardRepository.deleteById(id);
    }

    public CardEntity findById(Long id) {
        CardEntity card = cardRepository.findById(id) // getting the card
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (security.isAdmin()) return card; // if the user is admin return card

        // user is not an admin, getting id to check if the card belongs to the user
        Long me = security.currentUserId();
        if (card.getUserId().equals(me)) {
            return card;
        }
        throw new AccessDeniedException("You can only access your own card");
    }

    public List<CardEntity> findByUser(UserEntity user) {
        return cardRepository.findByUser(user);
    }

    public List<CardEntity> findAllByUserId(Long id) {
        return cardRepository.findAllByUser_Id(id);
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

    public List<CardEntity> findByExpirationDateBefore(YearMonth date) {
        return cardRepository.findByExpirationDateBefore(date);
    }

    public List<CardEntity> findByExpirationDateAfter(YearMonth date) {
        return cardRepository.findByExpirationDateAfter(date);
    }

    public List<CardEntity> findByExpirationDateBetween(YearMonth minDate, YearMonth maxDate) {
        return cardRepository.findByExpirationDateBetween(minDate, maxDate);
    }

    public List<CardEntity> findAllByStatus(String status) {
        return cardRepository.findAllByStatus(status);
    }

    public List<CardEntity> findByUserAndStatus(UserEntity user, String status) {
        return cardRepository.findByUserAndStatus(user, status);
    }

    public List<CardEntity> findByUserIdAndStatus(Long userId, String status) {
        return cardRepository.findByUser_IdAndStatus(userId, status);
    }

    public List<CardEntity> findByUserIdAndBalanceBetween(Long userId, Float min, Float max) {
        return cardRepository.findByUser_IdAndBalanceBetween(userId, min, max);
    }

    public List<CardEntity> findByUserIdAndBalanceGreaterThan(Long userId, Float min) {
        return cardRepository.findByUser_IdAndBalanceGreaterThan(userId, min);
    }

    public List<CardEntity> findByUserIdAndBalanceLessThan(Long userId, Float max) {
        return cardRepository.findByUser_IdAndBalanceLessThan(userId, max);
    }

    public List<CardEntity> findByUserIdAndExpirationDateBefore(Long id, YearMonth date) {
        return cardRepository.findByUser_IdAndExpirationDateBefore(id, date);
    }

    public List<CardEntity> findByUserIdAndExpirationDateAfter(Long id, YearMonth date) {
        return cardRepository.findByUser_IdAndExpirationDateAfter(id, date);
    }

    public List<CardEntity> findByUserIdAndExpirationDateBetween(Long id, YearMonth minDate, YearMonth maxDate) {
        return cardRepository.findByUser_IdAndExpirationDateBetween(id, minDate, maxDate);
    }

    public List<CardEntity> filterCards(Long userId,
                                        Float minBalance,
                                        Float maxBalance,
                                        YearMonth minDate,
                                        YearMonth maxDate,
                                        String status) {

        // Force the effective user scope if not admin
        Long effectiveUserId = security.isAdmin() ? userId : security.currentUserId();

        // filtering
        if (effectiveUserId != null && status != null)
            return cardRepository.findByUser_IdAndStatus(effectiveUserId, status);

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

        // admin only paths below
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
}
