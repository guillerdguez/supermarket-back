package com.supermarket.supermarket.unit.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supermarket.supermarket.filter.JwtFilter;
import com.supermarket.supermarket.service.security.JwtService;
import com.supermarket.supermarket.service.security.TokenBlacklistService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    private static final String TOKEN = "valid.jwt.token";
    private static final String USERNAME = "user@test.com";

    @Mock
    private JwtService jwtService;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private JwtFilter jwtFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + TOKEN);
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        // ObjectMapper isn't mocked - JwtFilter builds it via @RequiredArgsConstructor,
        // and error responses need a real serializer to write JSON.
        jwtFilter = new JwtFilter(jwtService, userDetailsService, tokenBlacklistService, new ObjectMapper());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("should reject a valid token when the account is disabled, without reaching the chain")
    void doFilter_DisabledUser_ShouldRejectBeforeChain() throws Exception {
        UserDetails disabledUser = User.withUsername(USERNAME)
                .password("irrelevant")
                .authorities(List.of())
                .disabled(true)
                .build();

        given(tokenBlacklistService.isBlacklisted(TOKEN)).willReturn(false);
        given(jwtService.getUsername(TOKEN)).willReturn(USERNAME);
        given(userDetailsService.loadUserByUsername(USERNAME)).willReturn(disabledUser);

        jwtFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(filterChain.getRequest()).isNull();
        then(jwtService).should(never()).isValidToken(anyString(), any());
    }

    @Test
    @DisplayName("should authenticate and continue the chain for an enabled user with a valid token")
    void doFilter_EnabledUserValidToken_ShouldAuthenticateAndContinue() throws Exception {
        UserDetails enabledUser = User.withUsername(USERNAME)
                .password("irrelevant")
                .authorities(List.of())
                .disabled(false)
                .build();

        given(tokenBlacklistService.isBlacklisted(TOKEN)).willReturn(false);
        given(jwtService.getUsername(TOKEN)).willReturn(USERNAME);
        given(userDetailsService.loadUserByUsername(USERNAME)).willReturn(enabledUser);
        given(jwtService.isValidToken(TOKEN, enabledUser)).willReturn(true);

        jwtFilter.doFilter(request, response, filterChain);

        assertThat(filterChain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(enabledUser);
    }
}
