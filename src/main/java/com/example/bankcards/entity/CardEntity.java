package com.example.bankcards.entity;

import com.example.bankcards.util.converter.PanEncryptConverter;
import com.example.bankcards.util.encryptors.AttributeEncryptor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "cards")
public class CardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private UserEntity user;

    @Convert(converter = PanEncryptConverter.class)
    @Column(nullable = false, unique = true)
    private String cardNumber;

    @Column(nullable = false, name = "expiry_date")
    private LocalDate expirationDate;

    @Convert(converter = AttributeEncryptor.class)
    @Column(nullable = false)
    private String cvv;

    @Column(nullable = false)
    private String status;

    @Min(0)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Convert(converter = AttributeEncryptor.class)
    @Column(nullable = false)
    private String pin;

    @Column(name = "created_at", updatable = false, insertable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false)
    private Instant updatedAt;

    public CardEntity() {}

    public CardEntity(long id, UserEntity user, String cardNumber, LocalDate expirationDate,
                      String cvv, String status, BigDecimal balance, String pin) {
        this.id = id;
        this.user = user;
        this.cardNumber = cardNumber;
        this.expirationDate = expirationDate;
        this.cvv = cvv;
        this.status = status;
        this.balance = balance;
        this.pin = pin;
    }

    // setters & getters
    public long getId() {return id;}

    public UserEntity getUser() {return user;}
    public void setUser(UserEntity user) {this.user = user;}

    public String getCardNumber() {return cardNumber;}
    public void setCardNumber(String cardNumber) {this.cardNumber = cardNumber;}

    public LocalDate getExpirationDate() {return expirationDate;}
    public void setExpirationDate(LocalDate expirationDate) {this.expirationDate = expirationDate;}

    public String getCvv() {return cvv;}
    public void setCvv(String cvv) {this.cvv = cvv;}

    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}

    public BigDecimal getBalance() {return balance;}
    public void setBalance(BigDecimal balance) {this.balance = balance;}

    public String getPin() {return pin;}

    public void setPin(String pin) {this.pin = pin;}

    public Long getUserId() {
        return user.getId();
    }

    @Transient
    public String getHolderName() {
        // this will not be saved in database
        return user != null ? user.getFullName() : null;
    }

    @PrePersist
    public void validateBeforeInsert() {
        if (pin == null || pin.length() != 4) {
            throw new IllegalArgumentException("PIN must be 4 digits");
        }
        if (cardNumber == null || cardNumber.isEmpty()) {
            throw new IllegalArgumentException("Card number cannot be empty");
        }
    }

    @PreUpdate
    public void validateBeforeUpdate() {
        if (pin != null && pin.length() != 4) {
            throw new IllegalArgumentException("PIN must be 4 digits");
        }
    }
}
