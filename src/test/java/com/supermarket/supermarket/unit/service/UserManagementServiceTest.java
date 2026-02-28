package com.supermarket.supermarket.unit.service;

import com.supermarket.supermarket.dto.auth.UserResponse;
import com.supermarket.supermarket.dto.user.ChangePasswordRequest;
import com.supermarket.supermarket.dto.user.ProfileUpdateRequest;
import com.supermarket.supermarket.dto.user.RoleUpdateRequest;
import com.supermarket.supermarket.dto.user.UserRequest;
import com.supermarket.supermarket.exception.DuplicateResourceException;
import com.supermarket.supermarket.exception.InvalidOperationException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.fixtures.user.UserFixtures;
import com.supermarket.supermarket.model.user.User;
import com.supermarket.supermarket.model.user.UserRole;
import com.supermarket.supermarket.repository.UserRepository;
import com.supermarket.supermarket.security.SecurityUtils;
import com.supermarket.supermarket.service.business.impl.UserManagementServiceImpl;
import com.supermarket.supermarket.validator.PasswordValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PasswordValidator passwordValidator;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private UserManagementServiceImpl userManagementService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = UserFixtures.defaultAdmin();
    }

    @Test
    @DisplayName("GET BY ID - should return user")
    void getById_ShouldReturnUser() {
        given(userRepository.findById(3L)).willReturn(Optional.of(mockUser));

        UserResponse result = userManagementService.getById(3L);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(mockUser.getEmail());
    }

    @Test
    @DisplayName("GET BY ID - should throw when not found")
    void getById_WhenNotFound_ShouldThrow() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userManagementService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("CREATE - should save user when data is unique")
    void create_WhenUnique_ShouldSave() {
        UserRequest request = UserRequest.builder()
                .username("newuser").email("new@test.com").password("Password1!")
                .firstName("New").lastName("User").role(UserRole.CASHIER).build();

        given(passwordValidator.validatePassword(request.getPassword())).willReturn(List.of());
        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(userRepository.existsByUsername(request.getUsername())).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn("encoded");
        given(userRepository.save(any(User.class))).willAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });

        UserResponse result = userManagementService.create(request);

        assertThat(result).isNotNull();
        then(userRepository).should().save(any(User.class));
    }

    @Test
    @DisplayName("CREATE - should throw when email already exists")
    void create_WhenEmailExists_ShouldThrow() {
        UserRequest request = UserRequest.builder()
                .username("newuser").email("existing@test.com").password("Password1!")
                .firstName("New").lastName("User").role(UserRole.CASHIER).build();

        given(passwordValidator.validatePassword(request.getPassword())).willReturn(List.of());
        given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

        assertThatThrownBy(() -> userManagementService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
        then(userRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("CREATE - should throw when username already exists")
    void create_WhenUsernameExists_ShouldThrow() {
        UserRequest request = UserRequest.builder()
                .username("taken").email("new@test.com").password("Password1!")
                .firstName("New").lastName("User").role(UserRole.CASHIER).build();

        given(passwordValidator.validatePassword(request.getPassword())).willReturn(List.of());
        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(userRepository.existsByUsername(request.getUsername())).willReturn(true);

        assertThatThrownBy(() -> userManagementService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
        then(userRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("CREATE - should throw when password is invalid")
    void create_WhenPasswordInvalid_ShouldThrow() {
        UserRequest request = UserRequest.builder()
                .username("newuser").email("new@test.com").password("weak")
                .firstName("New").lastName("User").role(UserRole.CASHIER).build();

        given(passwordValidator.validatePassword(request.getPassword()))
                .willReturn(List.of("Password must contain at least one uppercase letter"));

        assertThatThrownBy(() -> userManagementService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password validation failed");
        then(userRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("UPDATE ROLE - should update user role")
    void updateRole_ShouldUpdateRole() {
        User user = UserFixtures.defaultCashier();
        RoleUpdateRequest request = new RoleUpdateRequest(UserRole.MANAGER);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.save(user)).willReturn(user);

        UserResponse result = userManagementService.updateRole(1L, request);

        assertThat(user.getRole()).isEqualTo(UserRole.MANAGER);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("UPDATE ROLE - should throw when user not found")
    void updateRole_WhenNotFound_ShouldThrow() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userManagementService.updateRole(999L, new RoleUpdateRequest(UserRole.MANAGER)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("DELETE - should deactivate user")
    void delete_ShouldDeactivateUser() {
        User user = UserFixtures.defaultCashier();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.save(user)).willReturn(user);

        userManagementService.delete(1L);

        assertThat(user.getActive()).isFalse();
        then(userRepository).should().save(user);
    }

    @Test
    @DisplayName("DELETE - should throw when user not found")
    void delete_WhenNotFound_ShouldThrow() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userManagementService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("GET PROFILE - should return current user profile")
    void getProfile_ShouldReturnCurrentUser() {
        given(securityUtils.getCurrentUser()).willReturn(mockUser);

        UserResponse result = userManagementService.getProfile();

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(mockUser.getEmail());
    }

    @Test
    @DisplayName("UPDATE PROFILE - should update username and names")
    void updateProfile_ShouldUpdateFields() {
        given(securityUtils.getCurrentUser()).willReturn(mockUser);

        ProfileUpdateRequest request = new ProfileUpdateRequest("newusername", "NewFirst", "NewLast");
        given(userRepository.existsByUsername("newusername")).willReturn(false);
        given(userRepository.save(mockUser)).willReturn(mockUser);

        userManagementService.updateProfile(request);

        assertThat(mockUser.getUsername()).isEqualTo("newusername");
        assertThat(mockUser.getFirstName()).isEqualTo("NewFirst");
        assertThat(mockUser.getLastName()).isEqualTo("NewLast");
    }

    @Test
    @DisplayName("UPDATE PROFILE - should throw when username already taken")
    void updateProfile_WhenUsernameTaken_ShouldThrow() {
        given(securityUtils.getCurrentUser()).willReturn(mockUser);

        ProfileUpdateRequest request = new ProfileUpdateRequest("taken", "First", "Last");
        given(userRepository.existsByUsername("taken")).willReturn(true);

        assertThatThrownBy(() -> userManagementService.updateProfile(request))
                .isInstanceOf(DuplicateResourceException.class);
        then(userRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("CHANGE PASSWORD - should update password when current is correct")
    void changePassword_WhenCurrentCorrect_ShouldUpdate() {
        given(securityUtils.getCurrentUser()).willReturn(mockUser);

        ChangePasswordRequest request = new ChangePasswordRequest("OldPass1!", "NewPass1!");
        given(passwordEncoder.matches("OldPass1!", mockUser.getPassword())).willReturn(true);
        given(passwordValidator.validatePassword("NewPass1!")).willReturn(List.of());
        given(passwordEncoder.encode("NewPass1!")).willReturn("newEncoded");
        given(userRepository.save(mockUser)).willReturn(mockUser);

        userManagementService.changePassword(request);

        then(userRepository).should().save(mockUser);
    }

    @Test
    @DisplayName("CHANGE PASSWORD - should throw when current password is wrong")
    void changePassword_WhenCurrentWrong_ShouldThrow() {
        given(securityUtils.getCurrentUser()).willReturn(mockUser);

        ChangePasswordRequest request = new ChangePasswordRequest("WrongPass!", "NewPass1!");
        given(passwordEncoder.matches("WrongPass!", mockUser.getPassword())).willReturn(false);

        assertThatThrownBy(() -> userManagementService.changePassword(request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Current password is incorrect");
        then(userRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("CHANGE PASSWORD - should throw when new password is invalid")
    void changePassword_WhenNewPasswordInvalid_ShouldThrow() {
        given(securityUtils.getCurrentUser()).willReturn(mockUser);

        ChangePasswordRequest request = new ChangePasswordRequest("OldPass1!", "weak");
        given(passwordEncoder.matches("OldPass1!", mockUser.getPassword())).willReturn(true);
        given(passwordValidator.validatePassword("weak"))
                .willReturn(List.of("Password must contain at least one uppercase letter"));

        assertThatThrownBy(() -> userManagementService.changePassword(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password validation failed");
        then(userRepository).should(never()).save(any());
    }
}