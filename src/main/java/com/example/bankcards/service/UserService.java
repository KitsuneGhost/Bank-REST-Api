package com.example.bankcards.service;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, CardRepository cardRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public UserEntity registerUser(UserEntity user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        return userRepository.save(user);
    }

    @Transactional
    public UserEntity updateUser(Long id, UserEntity updUser) {
        UserEntity existingUser = userRepository.findById(id).orElseThrow(
                () -> new RuntimeException("User Not Found"));

        BeanUtils.copyProperties(updUser, existingUser, "id");

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public UserEntity findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserEntity findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));
    }

    public List<UserEntity> findByFullName(String fullName) {
        return userRepository.findByFullName(fullName);
    }

    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UserEntity findByCard(CardEntity card) {
        return userRepository.findByCards(card)
                .orElseThrow(() -> new RuntimeException("Card not found"));
    }

    public UserEntity findByCardsNumber(String cardNumber) {
        return userRepository.findByCardsCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException("Card not found"));
    }

    public List<UserEntity> findUsersWithNamePart(String namePart) {
        return userRepository.findByFullNameContainingIgnoreCase(namePart);
    }

    public UserEntity addCardToUser(Long userId, CardEntity card) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.addCard(card);
        return userRepository.save(user);
    }

    public UserEntity removeCardFromUser(Long userId, Long cardId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        CardEntity card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        user.removeCard(card);
        return userRepository.save(user);
    }
}
