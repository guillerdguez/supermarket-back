package com.supermercado.supermercado.unit.service;

import com.supermercado.supermercado.dto.sucursalDto.SucursalRequest;
import com.supermercado.supermercado.dto.sucursalDto.SucursalResponse;
import com.supermercado.supermercado.exception.DuplicateResourceException;
import com.supermercado.supermercado.exception.InvalidOperationException;
import com.supermercado.supermercado.exception.ResourceNotFoundException;
import com.supermercado.supermercado.fixtures.TestFixtures;
import com.supermercado.supermercado.mapper.SucursalMapper;
import com.supermercado.supermercado.model.Sucursal;
import com.supermercado.supermercado.repository.SucursalRepository;
import com.supermercado.supermercado.repository.VentaRepository;
import com.supermercado.supermercado.service.impl.SucursalServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock
    private SucursalRepository branchRepository;
    @Mock
    private SucursalMapper branchMapper;
    @Mock
    private VentaRepository saleRepository;
    @InjectMocks
    private SucursalServiceImpl branchService;

    @Test
    @DisplayName("GET ALL - should return list")
    void getAll_ShouldReturnList() {
        Sucursal branch = TestFixtures.defaultBranch();
        SucursalResponse response = TestFixtures.branchResponse();

        given(branchRepository.findAll()).willReturn(List.of(branch));
        given(branchMapper.toResponseList(List.of(branch))).willReturn(List.of(response));

        List<SucursalResponse> result = branchService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Central Branch");
    }

    @Test
    @DisplayName("GET BY ID - should return branch")
    void getById_ShouldReturnBranch() {
        Long id = 1L;
        Sucursal branch = TestFixtures.defaultBranch();
        SucursalResponse response = TestFixtures.branchResponse();

        given(branchRepository.findById(id)).willReturn(Optional.of(branch));
        given(branchMapper.toResponse(branch)).willReturn(response);

        SucursalResponse result = branchService.getById(id);

        assertThat(result).isNotNull();
        assertThat(result.getNombre()).isEqualTo("Central Branch");
    }

    @Test
    @DisplayName("GET BY ID - should throw exception when not found")
    void getById_WhenNotFound_ShouldThrowException() {
        Long id = 999L;
        given(branchRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> branchService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("CREATE - should save branch when name is unique")
    void create_WhenNameUnique_ShouldSave() {
        SucursalRequest request = TestFixtures.validBranchRequest();
        Sucursal entity = TestFixtures.defaultBranch();
        SucursalResponse response = TestFixtures.branchResponse();

        given(branchRepository.existsByNombre(request.getNombre())).willReturn(false);
        given(branchMapper.toEntity(request)).willReturn(entity);
        given(branchRepository.save(entity)).willReturn(entity);
        given(branchMapper.toResponse(entity)).willReturn(response);

        SucursalResponse result = branchService.create(request);

        assertThat(result).isNotNull();
        then(branchRepository).should().save(entity);
    }

    @Test
    @DisplayName("CREATE - should throw exception when name exists")
    void create_WhenNameExists_ShouldThrowException() {
        SucursalRequest request = TestFixtures.validBranchRequest();
        given(branchRepository.existsByNombre(request.getNombre())).willReturn(true);

        assertThatThrownBy(() -> branchService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        then(branchRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("UPDATE - should update branch")
    void update_ShouldUpdateBranch() {
        Long id = 1L;
        SucursalRequest request = TestFixtures.validBranchRequest();
        Sucursal existingBranch = TestFixtures.defaultBranch();
        SucursalResponse response = TestFixtures.branchResponse();

        given(branchRepository.findById(id)).willReturn(Optional.of(existingBranch));
        given(branchRepository.existsByNombre(request.getNombre())).willReturn(false);
        given(branchRepository.save(existingBranch)).willReturn(existingBranch);
        given(branchMapper.toResponse(existingBranch)).willReturn(response);

        SucursalResponse result = branchService.update(id, request);

        assertThat(result).isNotNull();
        then(branchMapper).should().updateEntity(request, existingBranch);
    }

    @Test
    @DisplayName("UPDATE - should throw exception when duplicate name")
    void update_WithDuplicateName_ShouldThrowException() {
        Long id = 1L;
        SucursalRequest request = TestFixtures.validBranchRequest();
        Sucursal existingBranch = TestFixtures.defaultBranch();

        given(branchRepository.findById(id)).willReturn(Optional.of(existingBranch));
        given(branchRepository.existsByNombre(request.getNombre())).willReturn(true);

        assertThatThrownBy(() -> branchService.update(id, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("DELETE - should delete when no associated sales")
    void delete_WhenNoSales_ShouldDelete() {
        Long id = 1L;
        Sucursal branch = TestFixtures.defaultBranch();

        given(branchRepository.findById(id)).willReturn(Optional.of(branch));
        given(saleRepository.existsBySucursalId(id)).willReturn(false);

        branchService.delete(id);

        then(branchRepository).should().delete(branch);
    }

    @Test
    @DisplayName("DELETE - should throw exception when sales exist")
    void delete_WhenSalesExist_ShouldThrowException() {
        Long id = 1L;
        Sucursal branch = TestFixtures.defaultBranch();

        given(branchRepository.findById(id)).willReturn(Optional.of(branch));
        given(saleRepository.existsBySucursalId(id)).willReturn(true);

        assertThatThrownBy(() -> branchService.delete(id))
                .isInstanceOf(InvalidOperationException.class);

        then(branchRepository).should(never()).delete(branch);
    }

    @Test
    @DisplayName("DELETE - should throw exception when branch not found")
    void delete_WhenNotFound_ShouldThrowException() {
        Long id = 999L;
        given(branchRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> branchService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);

        then(branchRepository).should(never()).delete(any());
    }
}