package com.example.bankcards.repository;

import com.example.bankcards.entity.TransferEntity;
import com.example.bankcards.service.CardService;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing transfers between cards.
 * <p>
 *      It is used by {@link CardService}.
 * </p>
 */
public interface TransferRepository extends JpaRepository<TransferEntity, Long> {}
