package com.example.bankcards.controller;

import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserEntity> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping
    public UserEntity registerUser(@RequestBody UserEntity user) {
        return userService.registerUser(user);
    }

    @PutMapping("/{id}")
    public UserEntity updateUser(@PathVariable Long id, @RequestBody UserEntity updUser) {
        return userService.updateUser(id, updUser);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @GetMapping("/{id}")
    public UserEntity getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @GetMapping("/filter")
    public List<UserEntity> filterUsers(@RequestParam(required = false) String fullName,
                                        @RequestParam(required = false) String namePart) {
        if (fullName != null) return userService.findByFullName(fullName);
        if (namePart != null) return userService.findUsersWithNamePart(namePart);
        return userService.getAllUsers();
    }

    @GetMapping("/filter/username/{username}")
    public UserEntity getUserByUsername(@PathVariable String username) {
        return userService.findByUsername(username);
    }

    @GetMapping("/filter/email/{email}")
    public UserEntity getUserByEmail(@PathVariable String email) {
        return userService.findByEmail(email);
    }

    @GetMapping("/filter/cardNumber/{cardNumber}")
    public UserEntity getUserByCardNumber(@PathVariable String cardNumber) {
        return userService.findByCardsNumber(cardNumber);
    }
}
