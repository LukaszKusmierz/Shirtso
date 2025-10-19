package org.peter_lukas.shirtso.auth.registration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.peter_lukas.shirtso.auth.AuthService;
import org.peter_lukas.shirtso.auth.NewUserRegistrationDto;
import org.peter_lukas.shirtso.auth.RegisterUserDataDto;
import org.peter_lukas.shirtso.auth.config.SpringSecurityConfig;
import org.peter_lukas.shirtso.auth.user.*;
import org.peter_lukas.shirtso.auth.validation.UserAlreadyExistsException;
import org.peter_lukas.shirtso.auth.validation.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AuthService authService;

    private Role userRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        userRole = createUserRole();
        testUser = createTestUser();
    }

    private Role createUserRole() {
        Role role = new Role();
        role.setRoleId(UUID.randomUUID());
        role.setName(SpringSecurityConfig.USER_READ);
        return role;
    }

    private User createTestUser() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.addRole(userRole);
        return user;
    }

    @Test
    void registerNewUser_WithValidData_CreatesUser() {
        // given
        NewUserRegistrationDto registrationDto = new NewUserRegistrationDto(
                "newuser", "newuser@example.com", "password123456"
        );

        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123456")).thenReturn("encodedPassword");
        when(roleRepository.findByName(SpringSecurityConfig.USER_READ))
                .thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // when
        RegisterUserDataDto result = authService.registerNewUser(registrationDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo(testUser.getEmail());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123456");
    }

    @Test
    void registerNewUser_WithExistingEmail_ThrowsException() {
        // given
        NewUserRegistrationDto registrationDto = new NewUserRegistrationDto(
                "newuser", "existing@example.com", "password123456"
        );

        when(userRepository.findByEmail("existing@example.com"))
                .thenReturn(Optional.of(testUser));

        // when & then
        assertThatThrownBy(() -> authService.registerNewUser(registrationDto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("newuser");

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerNewUser_WithExistingUsername_ThrowsException() {
        // given
        NewUserRegistrationDto registrationDto = new NewUserRegistrationDto(
                "existinguser", "new@example.com", "password123456"
        );

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("existinguser")).thenReturn(Optional.of(testUser));

        // when & then
        assertThatThrownBy(() -> authService.registerNewUser(registrationDto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("existinguser");

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerNewUser_WhenRoleNotFound_ThrowsException() {
        // given
        NewUserRegistrationDto registrationDto = new NewUserRegistrationDto(
                "newuser", "new@example.com", "password123456"
        );

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName(SpringSecurityConfig.USER_READ))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.registerNewUser(registrationDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User Role not found");
    }

    @Test
    void registerNewUser_AssignsRoleCorrectly() {
        // given
        NewUserRegistrationDto registrationDto = new NewUserRegistrationDto(
                "newuser", "new@example.com", "password123456"
        );

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName(SpringSecurityConfig.USER_READ))
                .thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertThat(savedUser.getRoles()).contains(userRole);
            return savedUser;
        });

        // when
        authService.registerNewUser(registrationDto);

        // then
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getCurrentUser_WithValidUser_ReturnsUserData() throws UserNotFoundException {
        // given
        testUser.getRoles().add(userRole);
        when(currentUserService.getCurrentUser()).thenReturn(testUser);

        // when
        RegisterUserDataDto result = authService.getCurrentUser();

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(testUser.getUserId());
        assertThat(result.userName()).isEqualTo(testUser.getUserName());
        assertThat(result.email()).isEqualTo(testUser.getEmail());
        assertThat(result.roles()).isNotNull();
    }

    @Test
    void getCurrentUser_WithNonExistentUser_ThrowsException() throws UserNotFoundException {
        // given
        when(currentUserService.getCurrentUser())
                .thenThrow(new UserNotFoundException("User not found"));

        // when & then
        assertThatThrownBy(() -> authService.getCurrentUser())
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void registerNewUser_EncodesPassword() {
        // given
        String plainPassword = "mySecurePassword123";
        NewUserRegistrationDto registrationDto = new NewUserRegistrationDto(
                "newuser", "new@example.com", plainPassword
        );

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(plainPassword)).thenReturn("$2a$10$encodedHash");
        when(roleRepository.findByName(SpringSecurityConfig.USER_READ))
                .thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertThat(savedUser.getPassword()).isNotEqualTo(plainPassword);
            assertThat(savedUser.getPassword()).isEqualTo("$2a$10$encodedHash");
            return savedUser;
        });

        // when
        authService.registerNewUser(registrationDto);

        // then
        verify(passwordEncoder).encode(plainPassword);
    }

    @Test
    void getCurrentUser_ReturnsAllUserRoles() throws UserNotFoundException {
        // given
        Role readRole = new Role();
        readRole.setName(SpringSecurityConfig.USER_READ);

        Role writeRole = new Role();
        writeRole.setName(SpringSecurityConfig.USER_WRITE);

        testUser.addRole(readRole);
        testUser.addRole(writeRole);

        when(currentUserService.getCurrentUser()).thenReturn(testUser);

        // when
        RegisterUserDataDto result = authService.getCurrentUser();

        // then
        assertThat(result.roles()).hasSize(2);
        assertThat(result.roles()).contains(
                SpringSecurityConfig.USER_READ,
                SpringSecurityConfig.USER_WRITE
        );
    }
}