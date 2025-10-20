package com.example.bankcards.repository;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<CardEntity, Long>, JpaSpecificationExecutor<CardEntity> {

    List<CardEntity> findByUser(UserEntity user);

    List<CardEntity> findAllByUser_Id(Long user_Id);

    Optional<CardEntity> findByCardNumber(String cardNumber);

    List<CardEntity> findByBalanceGreaterThan(BigDecimal minBalance);

    List<CardEntity> findByBalanceLessThan(BigDecimal maxBalance);

    List<CardEntity> findByBalanceBetween(BigDecimal minBalance, BigDecimal maxBalance);

    List<CardEntity> findByExpirationDateBefore(LocalDate date);

    List<CardEntity> findByExpirationDateAfter(LocalDate date);

    List<CardEntity> findByExpirationDateBetween(LocalDate minDate, LocalDate maxDate);

    List<CardEntity> findAllByStatus(String status);

    Page<CardEntity> findByUserId(Long userId, Pageable pageable);

    List<CardEntity> findByUserAndStatus(UserEntity user, String status);

    List<CardEntity> findByUser_IdAndStatus(Long user_Id, String status);

    List<CardEntity> findByUser_IdAndBalanceBetween(Long user_Id, BigDecimal min, BigDecimal max);

    List<CardEntity> findByUser_IdAndBalanceGreaterThan(Long user_Id, BigDecimal min);

    List<CardEntity> findByUser_IdAndBalanceLessThan(Long user_Id, BigDecimal max);

    List<CardEntity> findByUser_IdAndExpirationDateBefore(Long user_Id, LocalDate date);

    List<CardEntity> findByUser_IdAndExpirationDateAfter(Long user_Id, LocalDate date);

    List<CardEntity> findByUser_IdAndExpirationDateBetween(Long user_Id, LocalDate minDate, LocalDate maxDate);

    Optional<CardEntity> findByIdAndUser_Id(Long id, Long ownerId);
}
