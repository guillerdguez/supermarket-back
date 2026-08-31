package com.supermarket.supermarket.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.supermarket.supermarket.controller.CashRegisterController;
import com.supermarket.supermarket.dto.cashregister.CloseRegisterRequest;
import com.supermarket.supermarket.dto.cashregister.OpenRegisterRequest;
import com.supermarket.supermarket.exception.GlobalExceptionHandler;
import com.supermarket.supermarket.exception.InvalidOperationException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.fixtures.cashregister.CashRegisterFixtures;
import com.supermarket.supermarket.service.business.CashRegisterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CashRegisterControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CashRegisterService cashRegisterService;

    @InjectMocks
    private CashRegisterController cashRegisterController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(cashRegisterController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /cash-registers - should return list of registers")
    void getAll_ShouldReturnList() throws Exception {
        given(cashRegisterService.getAll()).willReturn(List.of(CashRegisterFixtures.openRegisterResponse()));

        mockMvc.perform(get("/cash-registers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("POST /cash-registers/open - should return 201 and Location header")
    void openRegister_ShouldReturn201() throws Exception {
        given(cashRegisterService.openRegister(any())).willReturn(CashRegisterFixtures.openRegisterResponse());

        mockMvc.perform(post("/cash-registers/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CashRegisterFixtures.validOpenRegisterRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @DisplayName("POST /cash-registers/open - should return 400 when request is invalid")
    void openRegister_InvalidRequest_ShouldReturn400() throws Exception {
        OpenRegisterRequest invalid = OpenRegisterRequest.builder()
                .branchId(1L)
                .openingBalance(null)
                .build();

        mockMvc.perform(post("/cash-registers/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /cash-registers/open - should return 400 when branch already has an open register")
    void openRegister_AlreadyOpen_ShouldReturn400() throws Exception {
        given(cashRegisterService.openRegister(any()))
                .willThrow(new InvalidOperationException("There is already an open register for this branch"));

        mockMvc.perform(post("/cash-registers/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CashRegisterFixtures.validOpenRegisterRequest())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /cash-registers/{id}/close - should return 200")
    void closeRegister_ShouldReturn200() throws Exception {
        given(cashRegisterService.closeRegister(any(), any())).willReturn(CashRegisterFixtures.closedRegisterResponse());

        mockMvc.perform(post("/cash-registers/1/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CashRegisterFixtures.validCloseRegisterRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    @DisplayName("POST /cash-registers/{id}/close - should return 400 when request is invalid")
    void closeRegister_InvalidRequest_ShouldReturn400() throws Exception {
        CloseRegisterRequest invalid = CloseRegisterRequest.builder()
                .closingBalance(new BigDecimal("-10.00"))
                .build();

        mockMvc.perform(post("/cash-registers/1/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /cash-registers/{id}/close - should return 400 when register is already closed")
    void closeRegister_AlreadyClosed_ShouldReturn400() throws Exception {
        given(cashRegisterService.closeRegister(any(), any()))
                .willThrow(new InvalidOperationException("Register is already closed"));

        mockMvc.perform(post("/cash-registers/1/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CashRegisterFixtures.validCloseRegisterRequest())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /cash-registers/{id}/close - should return 404 when register does not exist")
    void closeRegister_NotFound_ShouldReturn404() throws Exception {
        given(cashRegisterService.closeRegister(any(), any()))
                .willThrow(new ResourceNotFoundException("Cash register not found"));

        mockMvc.perform(post("/cash-registers/99/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CashRegisterFixtures.validCloseRegisterRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /cash-registers/branches/{branchId}/current - should return current register")
    void getCurrentRegister_ShouldReturn200() throws Exception {
        given(cashRegisterService.getCurrentRegisterByBranch(1L)).willReturn(CashRegisterFixtures.openRegisterResponse());

        mockMvc.perform(get("/cash-registers/branches/1/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @DisplayName("GET /cash-registers/branches/{branchId}/current - should return 404 when no open register")
    void getCurrentRegister_NotFound_ShouldReturn404() throws Exception {
        given(cashRegisterService.getCurrentRegisterByBranch(1L))
                .willThrow(new ResourceNotFoundException("No open register found for branch 1"));

        mockMvc.perform(get("/cash-registers/branches/1/current"))
                .andExpect(status().isNotFound());
    }
}
