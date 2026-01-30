package com.supermarket.supermarket.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.supermarket.supermarket.controller.ProductController;
import com.supermarket.supermarket.dto.product.ProductRequest;
import com.supermarket.supermarket.dto.product.ProductResponse;
import com.supermarket.supermarket.exception.GlobalExceptionHandler;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.fixtures.TestFixtures;
import com.supermarket.supermarket.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {
    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    private ObjectMapper objectMapper;
    private ProductController productController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        productController = new ProductController(productService);

        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();
        SortHandlerMethodArgumentResolver sortResolver = new SortHandlerMethodArgumentResolver();

        mockMvc = MockMvcBuilders.standaloneSetup(productController).setCustomArgumentResolvers(pageableResolver, sortResolver).setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    @DisplayName("GET /products - should return paginated list")
    void getAll_ShouldReturnPaginatedList() throws Exception {
        Page<ProductResponse> productPage = new PageImpl<>(List.of(TestFixtures.productResponse()), PageRequest.of(0, 10), 1);

        given(productService.getAll(any(Specification.class), any(Pageable.class))).willReturn(productPage);

        mockMvc.perform(get("/products").param("page", "0").param("size", "10")).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1))).andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /products/all - should return simple list")
    void getAllList_ShouldReturnList() throws Exception {
        given(productService.getAllForDropdown()).willReturn(List.of(TestFixtures.productResponse()));

        mockMvc.perform(get("/products/all")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /products/{id} - should return product")
    void getById_ShouldReturnProduct() throws Exception {
        given(productService.getById(1L)).willReturn(TestFixtures.productResponse());

        mockMvc.perform(get("/products/1")).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Premium Rice"));
    }

    @Test
    @DisplayName("GET /products/{id} - should return 404 when not found")
    void getById_WhenNotFound_ShouldReturn404() throws Exception {
        given(productService.getById(999L)).willThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(get("/products/999")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /products - should create product")
    void create_ShouldReturn201() throws Exception {
        ProductResponse response = TestFixtures.productResponse();
        given(productService.create(any(ProductRequest.class))).willReturn(response);

        mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(TestFixtures.validProductRequest()))).andExpect(status().isCreated()).andExpect(header().exists("Location")).andExpect(jsonPath("$.name").value("Premium Rice"));
    }

    @Test
    @DisplayName("POST /products - should return 400 when invalid request")
    void create_WithInvalidRequest_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(TestFixtures.invalidProductRequest()))).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /products/{id} - should update product")
    void update_ShouldReturnUpdatedProduct() throws Exception {
        ProductResponse response = TestFixtures.productResponse();
        given(productService.update(eq(1L), any(ProductRequest.class))).willReturn(response);

        mockMvc.perform(put("/products/1").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(TestFixtures.validProductRequest()))).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Premium Rice"));
    }

    @Test
    @DisplayName("DELETE /products/{id} - should return 204")
    void delete_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/products/1")).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /products/{id} - should return 404 when not found")
    void delete_WhenNotFound_ShouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Product not found")).when(productService).delete(999L);

        mockMvc.perform(delete("/products/999")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /products/low-stock - should return low stock products")
    void getLowStock_ShouldReturnLowStockProducts() throws Exception {
        ProductResponse lowStockResponse = ProductResponse.builder().id(3L).name("Low Stock Milk").category("Dairy").price(new BigDecimal("2.50")).quantity(5).build();

        given(productService.getLowStockProducts(10)).willReturn(List.of(lowStockResponse));

        mockMvc.perform(get("/products/low-stock")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].quantity").value(5));
    }

    @Test
    @DisplayName("GET /products/low-stock?amount=5 - should return low stock products with custom threshold")
    void getLowStock_WithCustomAmount_ShouldReturnLowStockProducts() throws Exception {
        ProductResponse lowStockResponse = ProductResponse.builder().id(3L).name("Low Stock Milk").category("Dairy").price(new BigDecimal("2.50")).quantity(3).build();

        given(productService.getLowStockProducts(5)).willReturn(List.of(lowStockResponse));

        mockMvc.perform(get("/products/low-stock").param("amount", "5")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].quantity").value(3));
    }

    @Test
    @DisplayName("GET /products/low-stock - should return empty list when no low stock products")
    void getLowStock_WhenNoLowStock_ShouldReturnEmptyList() throws Exception {
        given(productService.getLowStockProducts(10)).willReturn(List.of());

        mockMvc.perform(get("/products/low-stock")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    }
}