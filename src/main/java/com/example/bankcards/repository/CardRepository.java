package com.example.bankcards.repository;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<CardEntity, Long> {

    List<CardEntity> findByUser(UserEntity user);

    List<CardEntity> findAllByUserId(Long userId);

    Optional<CardEntity> findByCardNumber(String cardNumber);

    List<CardEntity> findByBalanceGreaterThan(Float minBalance);

    List<CardEntity> findByBalanceLessThan(Float maxBalance);

    List<CardEntity> findByBalanceBetween(Float minBalance, Float maxBalance);

    List<CardEntity> findByExpirationDateBefore(Date date);

    List<CardEntity> findByExpirationDateAfter(Date date);

    List<CardEntity> findByExpirationDateBetween(Date minDate, Date maxDate);

    List<CardEntity> findAllByStatus(String status);

    List<CardEntity> findByUserAndStatus(UserEntity user, String status);

    List<CardEntity> findByUserIdAndStatus(Long userId, String status);

    List<CardEntity> findByUserIdAndBalanceBetween(Long userId, Float min, Float max);

    List<CardEntity> findByUserIdAndBalanceGreaterThan(Long userId, Float min);

    List<CardEntity> findByUserIdAndBalanceLessThan(Long userId, Float max);

    List<CardEntity> findByUserIdAndExpirationDateBefore(Long userId, Date date);

    List<CardEntity> findByUserIdAndExpirationDateAfter(Long userId, Date date);

    List<CardEntity> findByUserIdAndExpirationDateBetween(Long userId, Date minDate, Date maxDate);
}
