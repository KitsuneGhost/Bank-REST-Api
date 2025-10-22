package com.example.bankcards.controller;

import com.example.bankcards.dto.card.*;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "server.port=0")
@Import(CardControllerTest.MockConfig.class)
class CardControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CardService cardService;

    @BeforeEach
    void resetMocks() {
        reset(cardService);
    }

    @Test
    void listAllPaged_returnsPageResponse() throws Exception {
        UserEntity owner = new UserEntity(5L, "alice", "Alice", "pass",
                "alice@example.com", List.of());
        CardEntity entity = new CardEntity(
                1L, owner, "1234567812345678",
                LocalDate.of(2030, 5, 1), "123",
                "ACTIVE", BigDecimal.valueOf(100), "4321"
        );

        org.springframework.data.domain.Page<CardEntity> page =
                new org.springframework.data.domain.PageImpl<>(List.of(entity));

        given(cardService.filterAll(any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(page);

        mockMvc.perform(get("/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.items[0].maskedPan").value("**** **** **** 5678"))
                .andExpect(jsonPath("$.items[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.items[0].ownerId").value(5));
    }

    @Test
    void myCards_returnsMyCards() throws Exception {
        CardSummaryDTO dto = new CardSummaryDTO(
                1L, "Alice", "5678",
                LocalDate.of(2030, 5, 1),
                "ACTIVE", BigDecimal.TEN,
                java.time.Instant.now()
        );
        given(cardService.filterMine(any(CardFilter.class), any()))
                .willReturn(new org.springframework.data.domain.PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/cards/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].holderName").value("Alice"));
    }

    @Test
    void createForMe_createsCard() throws Exception {
        UserEntity owner = new UserEntity(5L, "jdoe", "John Doe", "pass",
                "john@example.com", List.of());
        CardEntity card = new CardEntity(
                10L, owner, "1234567812345678",
                LocalDate.of(2030, 5, 1), "123",
                "ACTIVE", BigDecimal.ZERO, "4321"
        );

        given(cardService.createForCurrentUser(any(CardCreateRequestDTO.class))).willReturn(card);

        CardCreateRequestDTO req = new CardCreateRequestDTO(
                "1234567812345678", "05/30", "4321", BigDecimal.ZERO, "123"
        );

        mockMvc.perform(post("/cards/me/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.ownerId").value(5));
    }

    @Test
    void transfer_executesServiceCall() throws Exception {
        String body = """
                {
                  "fromCardId": 1,
                  "toCardId": 2,
                  "amount": 50.00
                }
                """;

        mockMvc.perform(post("/cards/me/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());

        verify(cardService).transferBetweenMyCards(
                eq(1L), eq(2L), eq(new BigDecimal("50.00"))
        );
    }

    @Test
    void blockCard_callsService() throws Exception {
        mockMvc.perform(patch("/cards/10/block"))
                .andExpect(status().isNoContent());

        verify(cardService).updateStatus(10L, "BLOCKED");
    }

    @Test
    void activateCard_callsService() throws Exception {
        mockMvc.perform(patch("/cards/10/activate"))
                .andExpect(status().isNoContent());

        verify(cardService).updateStatus(10L, "ACTIVE");
    }

    @Test
    void requestBlock_callsService() throws Exception {
        mockMvc.perform(patch("/cards/me/15/request-block"))
                .andExpect(status().isNoContent());

        verify(cardService).requestBlock(15L);
    }

    @Test
    void getById_returnsCard() throws Exception {
        UserEntity owner = new UserEntity(5L, "alice", "Alice", "pass",
                "alice@example.com", List.of());
        CardEntity card = new CardEntity(
                10L, owner, "1234567812345678",
                LocalDate.of(2030, 5, 1), "123",
                "ACTIVE", BigDecimal.TEN, "4321"
        );
        given(cardService.getByIdAuthorized(10L)).willReturn(card);

        mockMvc.perform(get("/cards/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.ownerId").value(5));
    }

    @Test
    void updateCard_updatesEntity() throws Exception {
        UserEntity owner = new UserEntity(1L, "alice", "Alice", "pass",
                "alice@example.com", List.of());
        CardEntity updated = new CardEntity(
                2L, owner, "1234567812345678",
                LocalDate.of(2030, 5, 1), "123",
                "ACTIVE", BigDecimal.TEN, "4321"
        );
        given(cardService.updateCard(eq(2L), any(CardUpdateRequestDTO.class))).willReturn(updated);

        CardUpdateRequestDTO req = new CardUpdateRequestDTO("05/30", "ACTIVE");

        mockMvc.perform(put("/cards/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.expiry").value("05/30"));
    }

    @Test
    void deleteCard_callsService() throws Exception {
        mockMvc.perform(delete("/cards/3"))
                .andExpect(status().isNoContent());
        verify(cardService).deleteCard(3L);
    }

    @TestConfiguration
    static class MockConfig {
        @Bean @Primary CardService cardService() {
            return Mockito.mock(CardService.class);
        }
    }
}
