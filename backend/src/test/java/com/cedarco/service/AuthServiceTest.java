package com.cedarco.service;

import com.cedarco.dto.AuthDto;
import com.cedarco.entity.User;
import com.cedarco.repository.UserRepository;
import com.cedarco.security.JwtUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    private User savedUser;

    @BeforeEach
    void setUp() {
        savedUser = User.builder()
                .id(1L).firstName("John").lastName("Doe")
                .email("john@test.com").password("hashed-password")
                .role(User.Role.CUSTOMER).build();
    }

    // ─── register ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("register creates user and returns token response")
    void register_newEmail_createsUserAndReturnsTokens() {
        AuthDto.RegisterRequest req = new AuthDto.RegisterRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john@test.com");
        req.setPassword("secret123");
        req.setPhone("70000000");

        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken("john@test.com")).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken("john@test.com")).thenReturn("refresh-token");

        AuthDto.TokenResponse result = authService.register(req);

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(result.getEmail()).isEqualTo("john@test.com");
        assertThat(result.getRole()).isEqualTo("CUSTOMER");
        verify(passwordEncoder).encode("secret123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register throws IllegalArgumentException when email already registered")
    void register_duplicateEmail_throwsException() {
        AuthDto.RegisterRequest req = new AuthDto.RegisterRequest();
        req.setEmail("john@test.com");
        req.setPassword("secret123");

        when(userRepository.existsByEmail("john@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already registered");

        verify(userRepository, never()).save(any());
    }

    // ─── login ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login returns token response for valid credentials")
    void login_validCredentials_returnsTokens() {
        AuthDto.LoginRequest req = new AuthDto.LoginRequest();
        req.setEmail("john@test.com");
        req.setPassword("secret123");

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(savedUser));
        when(jwtUtil.generateToken("john@test.com")).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken("john@test.com")).thenReturn("refresh-token");

        AuthDto.TokenResponse result = authService.login(req);

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getEmail()).isEqualTo("john@test.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("login throws when authenticationManager rejects credentials")
    void login_invalidCredentials_throwsException() {
        AuthDto.LoginRequest req = new AuthDto.LoginRequest();
        req.setEmail("john@test.com");
        req.setPassword("wrong-password");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);

        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("login throws UsernameNotFoundException when user not found after auth")
    void login_userNotFoundAfterAuth_throwsException() {
        AuthDto.LoginRequest req = new AuthDto.LoginRequest();
        req.setEmail("ghost@test.com");
        req.setPassword("password");

        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    // ─── refresh ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("refresh returns new tokens for valid refresh token")
    void refresh_validToken_returnsNewTokens() {
        AuthDto.RefreshRequest req = new AuthDto.RefreshRequest();
        req.setRefreshToken("valid-refresh-token");

        when(jwtUtil.isTokenValid("valid-refresh-token")).thenReturn(true);
        when(jwtUtil.extractEmail("valid-refresh-token")).thenReturn("john@test.com");
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(savedUser));
        when(jwtUtil.generateToken("john@test.com")).thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken("john@test.com")).thenReturn("new-refresh-token");

        AuthDto.TokenResponse result = authService.refresh(req);

        assertThat(result.getAccessToken()).isEqualTo("new-access-token");
        assertThat(result.getRefreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    @DisplayName("refresh throws IllegalArgumentException for invalid token")
    void refresh_invalidToken_throwsException() {
        AuthDto.RefreshRequest req = new AuthDto.RefreshRequest();
        req.setRefreshToken("bad-token");

        when(jwtUtil.isTokenValid("bad-token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    @DisplayName("refresh throws UsernameNotFoundException when user not found")
    void refresh_userNotFound_throwsException() {
        AuthDto.RefreshRequest req = new AuthDto.RefreshRequest();
        req.setRefreshToken("valid-token");

        when(jwtUtil.isTokenValid("valid-token")).thenReturn(true);
        when(jwtUtil.extractEmail("valid-token")).thenReturn("ghost@test.com");
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(req))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    // ─── role propagation ─────────────────────────────────────────────────────

    @Test
    @DisplayName("register assigns CUSTOMER role by default")
    void register_newUser_hasCustomerRole() {
        AuthDto.RegisterRequest req = new AuthDto.RegisterRequest();
        req.setFirstName("Jane"); req.setLastName("Doe");
        req.setEmail("jane@test.com"); req.setPassword("pass123");

        User janeUser = User.builder().id(2L).firstName("Jane").lastName("Doe")
                .email("jane@test.com").role(User.Role.CUSTOMER).build();

        when(userRepository.existsByEmail("jane@test.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(janeUser);
        when(jwtUtil.generateToken(anyString())).thenReturn("tok");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("rtok");

        AuthDto.TokenResponse result = authService.register(req);

        assertThat(result.getRole()).isEqualTo("CUSTOMER");
    }
}
