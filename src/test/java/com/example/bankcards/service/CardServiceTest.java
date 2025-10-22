package com.example.bankcards.service;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.TransferEntity;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransferRepository transferRepository;
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private UserService userService;
    @Mock
    private CurrentUserService currentUserService;

    private CardService cardService;

    @BeforeEach
    void setUp() {
        cardService = new CardService(
                cardRepository,
                userRepository,
                transferRepository,
                securityUtils,
                userService,
                currentUserService
        );
    }

    @Test
    void transferBetweenMyCards_updatesBalancesPersistsTransferAndSavesInOrder() throws Exception {
        Long currentUserId = 99L;
        when(securityUtils.currentUserId()).thenReturn(currentUserId);

        CardEntity from = cardWithId(10L);
        from.setStatus("ACTIVE");
        from.setBalance(new BigDecimal("500.00"));

        CardEntity to = cardWithId(20L);
        to.setStatus("ACTIVE");
        to.setBalance(new BigDecimal("125.50"));

        when(cardRepository.findByIdAndUser_Id(10L, currentUserId)).thenReturn(Optional.of(from));
        when(cardRepository.findByIdAndUser_Id(20L, currentUserId)).thenReturn(Optional.of(to));
        when(cardRepository.save(any(CardEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal amount = new BigDecimal("200.00");

        cardService.transferBetweenMyCards(10L, 20L, amount);

        assertEquals(new BigDecimal("300.00"), from.getBalance());
        assertEquals(new BigDecimal("325.50"), to.getBalance());

        InOrder inOrder = inOrder(cardRepository, transferRepository);
        inOrder.verify(cardRepository).save(from);
        inOrder.verify(cardRepository).save(to);

        ArgumentCaptor<TransferEntity> transferCaptor = ArgumentCaptor.forClass(TransferEntity.class);
        inOrder.verify(transferRepository).save(transferCaptor.capture());

        TransferEntity savedTransfer = transferCaptor.getValue();
        assertEquals(currentUserId, savedTransfer.getUserId());
        assertEquals(10L, savedTransfer.getFromCardId());
        assertEquals(20L, savedTransfer.getToCardId());
        assertEquals(amount, savedTransfer.getAmount());
        assertEquals("COMPLETED", savedTransfer.getStatus());
    }

    @Test
    void transferBetweenMyCards_rejectsSameCardIds() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cardService.transferBetweenMyCards(5L, 5L, new BigDecimal("10.00")));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Cannot transfer to the same card", ex.getReason());
    }

    @Test
    void transferBetweenMyCards_rejectsInsufficientFunds() throws Exception {
        Long currentUserId = 7L;
        when(securityUtils.currentUserId()).thenReturn(currentUserId);

        CardEntity from = cardWithId(1L);
        from.setStatus("ACTIVE");
        from.setBalance(new BigDecimal("50.00"));

        CardEntity to = cardWithId(2L);
        to.setStatus("ACTIVE");
        to.setBalance(new BigDecimal("10.00"));

        when(cardRepository.findByIdAndUser_Id(1L, currentUserId)).thenReturn(Optional.of(from));
        when(cardRepository.findByIdAndUser_Id(2L, currentUserId)).thenReturn(Optional.of(to));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cardService.transferBetweenMyCards(1L, 2L, new BigDecimal("200.00")));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Insufficient funds", ex.getReason());

        verify(cardRepository, never()).save(any(CardEntity.class));
        verify(transferRepository, never()).save(any(TransferEntity.class));
    }

    @Test
    void filterCards_nonAdminIgnoresProvidedUserId() {
        Long currentUserId = 88L;
        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.currentUserId()).thenReturn(currentUserId);

        CardEntity card = new CardEntity();
        when(cardRepository.findByUser_IdAndStatus(currentUserId, "ACTIVE")).thenReturn(List.of(card));

        List<CardEntity> result = cardService.filterCards(123L, null, null, null, null, "ACTIVE");

        assertThat(result).containsExactly(card);
        verify(cardRepository).findByUser_IdAndStatus(currentUserId, "ACTIVE");
        verifyNoMoreInteractions(cardRepository);
    }

    private static CardEntity cardWithId(Long id) throws Exception {
        CardEntity card = new CardEntity();
        setField(card, "id", id);
        return card;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}