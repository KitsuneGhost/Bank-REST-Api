package com.example.bankcards.repository;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    List<UserEntity> findByFullName(String fullName);

    UserEntity findByEmail(String email);

    Optional<UserEntity> findByCards(CardEntity card);

    Optional<UserEntity> findByCardsCardNumber(String cardNumber);

    List<UserEntity> findByFullNameContainingIgnoreCase(String namePart);
}
