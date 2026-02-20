package com.supermarket.supermarket.unit.service;

import com.supermarket.supermarket.dto.cashregister.CashRegisterResponse;
import com.supermarket.supermarket.dto.cashregister.CloseRegisterRequest;
import com.supermarket.supermarket.dto.cashregister.OpenRegisterRequest;
import com.supermarket.supermarket.exception.InvalidOperationException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.fixtures.branch.BranchFixtures;
import com.supermarket.supermarket.fixtures.user.UserFixtures;
import com.supermarket.supermarket.mapper.CashRegisterMapper;
import com.supermarket.supermarket.model.*;
import com.supermarket.supermarket.repository.BranchRepository;
import com.supermarket.supermarket.repository.CashRegisterRepository;
import com.supermarket.supermarket.service.business.impl.CashRegisterServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CashRegisterServiceTest {
    @Mock
    private CashRegisterRepository cashRegisterRepository;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private CashRegisterMapper cashRegisterMapper;
    @InjectMocks
    private CashRegisterServiceImpl cashRegisterService;

    private User mockUser;
    private Branch branch;

    @BeforeEach
    void setUp() {
        mockUser = UserFixtures.defaultCashier();
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(mockUser);
        SecurityContextHolder.setContext(securityContext);

        branch = BranchFixtures.defaultBranch();
    }

    @Test
    void openRegister_shouldCreateNewOpenRegister() {
        OpenRegisterRequest request = new OpenRegisterRequest(branch.getId(), new BigDecimal("100.00"));
        CashRegister savedRegister = CashRegister.builder()
                .id(100L)
                .branch(branch)
                .openingBalance(request.getOpeningBalance())
                .openingTime(LocalDateTime.now())
                .status(CashRegisterStatus.OPEN)
                .openedBy(mockUser)
                .build();
        CashRegisterResponse response = CashRegisterResponse.builder().id(100L).build();

        given(branchRepository.findById(branch.getId())).willReturn(Optional.of(branch));
        given(cashRegisterRepository.findByBranchIdAndStatus(branch.getId(), CashRegisterStatus.OPEN))
                .willReturn(Optional.empty());
        given(cashRegisterRepository.save(any(CashRegister.class))).willReturn(savedRegister);
        given(cashRegisterMapper.toResponse(savedRegister)).willReturn(response);

        CashRegisterResponse result = cashRegisterService.openRegister(request);

        assertThat(result.getId()).isEqualTo(100L);
        then(cashRegisterRepository).should().save(any(CashRegister.class));
    }

    @Test
    void openRegister_whenBranchNotFound_shouldThrowException() {
        OpenRegisterRequest request = new OpenRegisterRequest(99L, new BigDecimal("100.00"));
        given(branchRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> cashRegisterService.openRegister(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void openRegister_whenRegisterAlreadyOpen_shouldThrowException() {
        OpenRegisterRequest request = new OpenRegisterRequest(branch.getId(), new BigDecimal("100.00"));
        given(branchRepository.findById(branch.getId())).willReturn(Optional.of(branch));
        given(cashRegisterRepository.findByBranchIdAndStatus(branch.getId(), CashRegisterStatus.OPEN))
                .willReturn(Optional.of(mock(CashRegister.class)));

        assertThatThrownBy(() -> cashRegisterService.openRegister(request))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void closeRegister_shouldCloseRegister() {
        Long registerId = 100L;
        CloseRegisterRequest request = new CloseRegisterRequest(new BigDecimal("150.00"));
        CashRegister register = CashRegister.builder()
                .id(registerId)
                .branch(branch)
                .status(CashRegisterStatus.OPEN)
                .openedBy(mockUser)
                .build();
        CashRegisterResponse response = CashRegisterResponse.builder().id(registerId).build();

        given(cashRegisterRepository.findById(registerId)).willReturn(Optional.of(register));
        given(cashRegisterRepository.save(register)).willReturn(register);
        given(cashRegisterMapper.toResponse(register)).willReturn(response);

        CashRegisterResponse result = cashRegisterService.closeRegister(registerId, request);

        assertThat(result.getId()).isEqualTo(registerId);
        assertThat(register.getStatus()).isEqualTo(CashRegisterStatus.CLOSED);
        assertThat(register.getClosingBalance()).isEqualByComparingTo("150.00");
        assertThat(register.getClosedBy()).isEqualTo(mockUser);
        then(cashRegisterRepository).should().save(register);
    }

    @Test
    void closeRegister_whenRegisterNotFound_shouldThrowException() {
        given(cashRegisterRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> cashRegisterService.closeRegister(99L, new CloseRegisterRequest(BigDecimal.ZERO)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void closeRegister_whenAlreadyClosed_shouldThrowException() {
        Long registerId = 100L;
        CashRegister register = CashRegister.builder()
                .id(registerId)
                .status(CashRegisterStatus.CLOSED)
                .build();
        given(cashRegisterRepository.findById(registerId)).willReturn(Optional.of(register));

        assertThatThrownBy(() -> cashRegisterService.closeRegister(registerId, new CloseRegisterRequest(BigDecimal.ZERO)))
                .isInstanceOf(InvalidOperationException.class);
    }
}