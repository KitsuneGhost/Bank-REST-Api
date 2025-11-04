package com.example.bankcards.repository;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.service.CardService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


/**
 * Repository interface for accessing and managing {@link CardEntity} data.
 * <p>
 * Defines derived query methods for searching users.
 * <p>
 * All methods rely on Spring Data JPA's automatic query generation based
 * on method naming conventions. The repository is primarily used by
 * the {@link CardService} to perform database operations, while maintaining
 * abstraction from the controller layer.
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     *
     * Finds user by their username.
     *
     * @param username username of the user
     * @return optional containing user if exists, otherwise empty
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     *
     * Finds user by their email.
     *
     * @param email email of the user
     * @return optional containing user if exists, otherwise empty
     */
    Optional<UserEntity> findByEmail(String email);
}
