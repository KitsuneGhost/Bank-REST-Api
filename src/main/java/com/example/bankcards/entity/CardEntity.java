package com.example.bankcards.entity;

import com.example.bankcards.util.AttributeEncryptor;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "cards")
public class CardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Convert(converter = AttributeEncryptor.class)
    @Column(nullable = false, unique = true)
    private String cardNumber;

    @Column(nullable = false)
    private Date expirationDate;

    @Convert(converter = AttributeEncryptor.class)
    @Column(nullable = false)
    private String cvv;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private float balance;

    @Convert(converter = AttributeEncryptor.class)
    @Column(nullable = false)
    private String pin;

    public CardEntity() {}

    public CardEntity(long id, UserEntity user, String cardNumber, String holderName,
                      Date expirationDate, String cvv, String status, float balance, String pin) {
        this.id = id;
        this.user = user;
        this.cardNumber = cardNumber;
        this.expirationDate = expirationDate;
        this.cvv = cvv;
        this.status = status;
        this.balance = balance;
        this.pin = pin;
    }

    // сеттеры и геттеры
    public long getId() {return id;}

    public UserEntity getUser() {return user;}
    public void setUser(UserEntity user) {this.user = user;}

    public String getCardNumber() {return cardNumber;}
    public void setCardNumber(String cardNumber) {this.cardNumber = cardNumber;}

    public Date getExpirationDate() {return expirationDate;}
    public void setExpirationDate(Date expirationDate) {this.expirationDate = expirationDate;}

    public String getCvv() {return cvv;}
    public void setCvv(String cvv) {this.cvv = cvv;}

    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}

    public float getBalance() {return balance;}
    public void setBalance(float balance) {this.balance = balance;}

    public String getPin() {return pin;}

    public void setPin(String pin) {this.pin = pin;}

    @Transient
    public String getHolderName() {
        // это не сохраняетс в бд
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
