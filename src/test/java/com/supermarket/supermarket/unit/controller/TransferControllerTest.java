package com.supermarket.supermarket.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.supermarket.supermarket.controller.TransferController;
import com.supermarket.supermarket.dto.transfer.RejectTransferRequest;
import com.supermarket.supermarket.dto.transfer.TransferRequest;
import com.supermarket.supermarket.dto.transfer.TransferResponse;
import com.supermarket.supermarket.exception.GlobalExceptionHandler;
import com.supermarket.supermarket.exception.InvalidOperationException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.fixtures.transfer.TransferFixtures;
import com.supermarket.supermarket.model.transfer.TransferStatus;
import com.supermarket.supermarket.service.business.TransferService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransferControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TransferService transferService;

    @InjectMocks
    private TransferController transferController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(transferController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /transfers - should return 201 and Location header")
    void requestTransfer_ShouldReturn201() throws Exception {
        TransferResponse response = buildResponse(TransferStatus.PENDING);
        given(transferService.requestTransfer(any())).willReturn(response);

        mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TransferFixtures.validTransferRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /transfers - should return 400 when request is invalid")
    void requestTransfer_InvalidRequest_ShouldReturn400() throws Exception {
        TransferRequest invalid = TransferRequest.builder()
                .sourceBranchId(null)
                .targetBranchId(2L)
                .productId(1L)
                .quantity(0)
                .build();

        mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /transfers - should return list of transfers")
    void getAllTransfers_ShouldReturnList() throws Exception {
        given(transferService.getAllTransfers()).willReturn(List.of(buildResponse(TransferStatus.PENDING)));

        mockMvc.perform(get("/transfers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /transfers/{id} - should return transfer")
    void getTransferById_ShouldReturnTransfer() throws Exception {
        given(transferService.getTransferById(1L)).willReturn(buildResponse(TransferStatus.PENDING));

        mockMvc.perform(get("/transfers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /transfers/{id} - should return 404 when not found")
    void getTransferById_NotFound_ShouldReturn404() throws Exception {
        given(transferService.getTransferById(99L))
                .willThrow(new ResourceNotFoundException("Transfer not found"));

        mockMvc.perform(get("/transfers/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /transfers/{id}/approve - should return 200 with APPROVED status")
    void approveTransfer_ShouldReturn200() throws Exception {
        given(transferService.approveTransfer(1L)).willReturn(buildResponse(TransferStatus.APPROVED));
        mockMvc.perform(post("/transfers/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("POST /transfers/{id}/approve - should return 400 when not pending")
    void approveTransfer_NotPending_ShouldReturn400() throws Exception {
        given(transferService.approveTransfer(1L))
                .willThrow(new InvalidOperationException("Only PENDING transfers can be approved"));
        mockMvc.perform(post("/transfers/1/approve"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /transfers/{id}/reject - should return 200 with REJECTED status")
    void rejectTransfer_ShouldReturn200() throws Exception {
        RejectTransferRequest request = TransferFixtures.validRejectRequest();
        given(transferService.rejectTransfer(eq(1L), any())).willReturn(buildResponse(TransferStatus.REJECTED));

        mockMvc.perform(post("/transfers/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @DisplayName("POST /transfers/{id}/reject - should return 400 when reason is blank")
    void rejectTransfer_BlankReason_ShouldReturn400() throws Exception {
        RejectTransferRequest invalid = RejectTransferRequest.builder().reason("").build();

        mockMvc.perform(post("/transfers/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /transfers/{id}/complete - should return 200 with COMPLETED status")
    void completeTransfer_ShouldReturn200() throws Exception {
        given(transferService.completeTransfer(1L)).willReturn(buildResponse(TransferStatus.COMPLETED));

        mockMvc.perform(post("/transfers/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("POST /transfers/{id}/cancel - should return 200 with CANCELLED status")
    void cancelTransfer_ShouldReturn200() throws Exception {
        given(transferService.cancelTransfer(1L)).willReturn(buildResponse(TransferStatus.CANCELLED));

        mockMvc.perform(post("/transfers/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("GET /transfers/status/{status} - should return filtered list")
    void getTransfersByStatus_ShouldReturnList() throws Exception {
        given(transferService.getTransfersByStatus("PENDING"))
                .willReturn(List.of(buildResponse(TransferStatus.PENDING)));

        mockMvc.perform(get("/transfers/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /transfers/source/{branchId} - should return transfers from source branch")
    void getTransfersBySourceBranch_ShouldReturnList() throws Exception {
        given(transferService.getTransfersBySourceBranch(1L))
                .willReturn(List.of(buildResponse(TransferStatus.PENDING)));

        mockMvc.perform(get("/transfers/source/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /transfers/target/{branchId} - should return transfers to target branch")
    void getTransfersByTargetBranch_ShouldReturnList() throws Exception {
        given(transferService.getTransfersByTargetBranch(2L))
                .willReturn(List.of(buildResponse(TransferStatus.APPROVED)));

        mockMvc.perform(get("/transfers/target/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    private TransferResponse buildResponse(TransferStatus status) {
        return TransferResponse.builder()
                .id(1L)
                .sourceBranchId(1L)
                .sourceBranchName("Central Branch")
                .targetBranchId(2L)
                .targetBranchName("North Branch")
                .productId(1L)
                .productName("Premium Rice")
                .quantity(10)
                .status(status)
                .requestedById(1L)
                .requestedByUsername("cashier-test")
                .requestedAt(LocalDateTime.now())
                .build();
    }
}