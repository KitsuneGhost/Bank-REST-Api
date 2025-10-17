package com.example.bankcards.entity;

import com.example.bankcards.security.Role;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CardEntity> cards = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    public UserEntity() {}

    public UserEntity(long id, String username, String fullName, String password,
                      String email, List<CardEntity> cards) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.password = password;
        this.email = email;
        this.cards = cards;
    }

    // setters & getters
    public long getId() {return id;}

    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}

    public String getFullName() {return fullName;}
    public void setFullName(String fullName) {this.fullName = fullName;}

    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    public List<CardEntity> getCards() {return cards;}
    public void setCards(List<CardEntity> cards) {this.cards = cards;}

    public Set<Role> getRoles() {
        return roles;
    }
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void addCard(CardEntity card) {
        cards.add(card);
        card.setUser(this);
    }

    public void removeCard(CardEntity card) {
        cards.remove(card);
        card.setUser(null);
    }
}