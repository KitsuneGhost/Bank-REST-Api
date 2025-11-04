package com.example.bankcards.repository;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.service.CardService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


/**
 * Repository interface for accessing and managing {@link CardEntity} data.
 * <p>
 * Defines derived query methods for searching cards by user ownership,
 * balance thresholds, expiration dates, and card status.
 * <p>
 * All methods rely on Spring Data JPA's automatic query generation based
 * on method naming conventions. The repository is primarily used by
 * the {@link CardService} to perform database operations, while maintaining
 * abstraction from the controller layer.
 * <p>
 * Sensitive fields such as card numbers are stored in encrypted form,
 * and queries always respect user-level data isolation.
 */
public interface CardRepository extends JpaRepository<CardEntity, Long>, JpaSpecificationExecutor<CardEntity> {

    /**
     * Retrieves all cards owned by the specified user.
     *
     * @param user_Id ID of the card owner
     * @return list of cards that belong to the given user
     */
    List<CardEntity> findAllByUser_Id(Long user_Id);

    /**
     * Finds all cards with a balance greater than the specified minimum value.
     *
     * @param minBalance minimum balance threshold
     * @return list of cards with a balance greater than the given value
     */
    List<CardEntity> findByBalanceGreaterThan(BigDecimal minBalance);

    /**
     * Finds all cards with a balance less than the specified maximum value.
     *
     * @param maxBalance maximum balance threshold
     * @return list of cards with a balance less than the given value
     */
    List<CardEntity> findByBalanceLessThan(BigDecimal maxBalance);

    /**
     * Finds all cards whose balance falls within the specified range.
     *
     * @param minBalance minimum balance (inclusive)
     * @param maxBalance maximum balance (inclusive)
     * @return list of cards within the specified balance range
     */
    List<CardEntity> findByBalanceBetween(BigDecimal minBalance, BigDecimal maxBalance);

    /**
     * Finds all cards whose expiration date is before the specified date.
     *
     * @param date upper bound for the expiration date (exclusive)
     * @return list of cards expiring before the given date
     */
    List<CardEntity> findByExpirationDateBefore(LocalDate date);

    /**
     * Finds all cards whose expiration date is after the specified date.
     *
     * @param date lower bound for the expiration date (exclusive)
     * @return list of cards expiring after the given date
     */
    List<CardEntity> findByExpirationDateAfter(LocalDate date);

    /**
     * Finds all cards whose expiration date is within the specified range.
     *
     * @param minDate earliest expiration date (inclusive)
     * @param maxDate latest expiration date (inclusive)
     * @return list of cards within the specified expiration date range
     */
    List<CardEntity> findByExpirationDateBetween(LocalDate minDate, LocalDate maxDate);

    /**
     * Finds all cards with the specified status.
     *
     * @param status card status
     * @return list of cards with the given status
     */
    List<CardEntity> findAllByStatus(String status);

    /**
     * Finds all cards with the specified status that belong to the given user.
     *
     * @param user_Id ID of the card owner
     * @param status  card status
     * @return list of cards matching the status and user ID
     */
    List<CardEntity> findByUser_IdAndStatus(Long user_Id, String status);

    /**
     * Finds all cards belonging to a specific user whose balance is greater than the given value.
     *
     * @param user_Id ID of the card owner
     * @param min     minimum balance threshold (exclusive)
     * @return list of cards with balance greater than the given value
     */
    List<CardEntity> findByUser_IdAndBalanceGreaterThan(Long user_Id, BigDecimal min);

    /**
     * Finds all cards belonging to a specific user whose balance is less than the given value.
     *
     * @param user_Id ID of the card owner
     * @param max     maximum balance threshold (exclusive)
     * @return list of cards with balance less than the given value
     */
    List<CardEntity> findByUser_IdAndBalanceLessThan(Long user_Id, BigDecimal max);

    /**
     * Finds all cards belonging to a specific user whose balance is within the specified range.
     *
     * @param user_Id ID of the card owner
     * @param min     minimum balance (inclusive)
     * @param max     maximum balance (inclusive)
     * @return list of cards matching the balance range
     */
    List<CardEntity> findByUser_IdAndBalanceBetween(Long user_Id, BigDecimal min, BigDecimal max);

    /**
     * Finds all cards belonging to a specific user that expire before the given date.
     *
     * @param user_Id ID of the card owner
     * @param date    expiration date upper bound (exclusive)
     * @return list of cards expiring before the given date
     */
    List<CardEntity> findByUser_IdAndExpirationDateBefore(Long user_Id, LocalDate date);

    /**
     * Finds all cards belonging to a specific user that expire after the given date.
     *
     * @param user_Id ID of the card owner
     * @param date    expiration date lower bound (exclusive)
     * @return list of cards expiring after the given date
     */
    List<CardEntity> findByUser_IdAndExpirationDateAfter(Long user_Id, LocalDate date);

    /**
     * Finds all cards belonging to a specific user whose expiration date is within the specified range.
     *
     * @param user_Id ID of the card owner
     * @param minDate minimum expiration date (inclusive)
     * @param maxDate maximum expiration date (inclusive)
     * @return list of cards with expiration dates in the specified range
     */
    List<CardEntity> findByUser_IdAndExpirationDateBetween(Long user_Id, LocalDate minDate, LocalDate maxDate);

    /**
     * Finds a card by its ID, verifying that it belongs to the specified user.
     *
     * @param id       ID of the card
     * @param ownerId  ID of the expected card owner
     * @return optional containing the card if it exists and belongs to the user, otherwise empty
     */
    Optional<CardEntity> findByIdAndUser_Id(Long id, Long ownerId);
}
