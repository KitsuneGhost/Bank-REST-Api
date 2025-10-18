package com.example.bankcards.repository;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<CardEntity, Long> {

    List<CardEntity> findByUser(UserEntity user);

    List<CardEntity> findAllByUser_Id(Long user_Id);

    Optional<CardEntity> findByCardNumber(String cardNumber);

    List<CardEntity> findByBalanceGreaterThan(Float minBalance);

    List<CardEntity> findByBalanceLessThan(Float maxBalance);

    List<CardEntity> findByBalanceBetween(Float minBalance, Float maxBalance);

    List<CardEntity> findByExpirationDateBefore(YearMonth date);

    List<CardEntity> findByExpirationDateAfter(YearMonth date);

    List<CardEntity> findByExpirationDateBetween(YearMonth minDate, YearMonth maxDate);

    List<CardEntity> findAllByStatus(String status);

    List<CardEntity> findByUserAndStatus(UserEntity user, String status);

    List<CardEntity> findByUser_IdAndStatus(Long user_Id, String status);

    List<CardEntity> findByUser_IdAndBalanceBetween(Long user_Id, Float min, Float max);

    List<CardEntity> findByUser_IdAndBalanceGreaterThan(Long user_Id, Float min);

    List<CardEntity> findByUser_IdAndBalanceLessThan(Long user_Id, Float max);

    List<CardEntity> findByUser_IdAndExpirationDateBefore(Long user_Id, YearMonth date);

    List<CardEntity> findByUser_IdAndExpirationDateAfter(Long user_Id, YearMonth date);

    List<CardEntity> findByUser_IdAndExpirationDateBetween(Long user_Id, YearMonth minDate, YearMonth maxDate);
}
