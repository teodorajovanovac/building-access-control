package com.buildingaccess.service;

import com.buildingaccess.dto.auth.AuthResponse;
import com.buildingaccess.dto.auth.LoginRequest;
import com.buildingaccess.dto.auth.RegisterRequest;
import com.buildingaccess.exception.DuplicateResourceException;
import com.buildingaccess.exception.ResourceNotFoundException;
import com.buildingaccess.model.Apartment;
import com.buildingaccess.model.User;
import com.buildingaccess.model.enums.Role;
import com.buildingaccess.repository.ApartmentRepository;
import com.buildingaccess.repository.UserRepository;
import com.buildingaccess.security.JwtService;
import com.buildingaccess.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit testovi za AuthService: SK2 registracija stanara (sa proverom duplikata email-a) i SK1 prijava. */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ApartmentRepository apartmentRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, apartmentRepository, passwordEncoder, authenticationManager, jwtService);
    }

    @Test
    void register_duplicateEmail_throwsDuplicateResourceException() {
        RegisterRequest request = new RegisterRequest("Ana", "Anić", "ana@example.com", "lozinka1", 5L);
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
        verify(apartmentRepository, never()).findById(any());
    }

    @Test
    void register_apartmentNotFound_throwsResourceNotFoundException() {
        RegisterRequest request = new RegisterRequest("Ana", "Anić", "ana@example.com", "lozinka1", 5L);
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(apartmentRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void register_validRequest_createsResidentWithBadgeCodeAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("Ana", "Anić", "ana@example.com", "lozinka1", 5L);
        Apartment apartment = Apartment.builder().id(5L).number("7").build();

        when(userRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(apartmentRepository.findById(5L)).thenReturn(Optional.of(apartment));
        when(userRepository.existsByBadgeCode(anyString())).thenReturn(false);
        when(passwordEncoder.encode("lozinka1")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.role()).isEqualTo(Role.RESIDENT);
        assertThat(response.email()).isEqualTo("ana@example.com");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.RESIDENT);
        assertThat(captor.getValue().getBadgeCode()).startsWith("RES-");
        assertThat(captor.getValue().getPassword()).isEqualTo("ENCODED");
        assertThat(captor.getValue().getApartment()).isEqualTo(apartment);
    }

    @Test
    void login_badCredentials_propagatesException() {
        LoginRequest request = new LoginRequest("ana@example.com", "wrong-password");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void login_validCredentials_returnsAuthResponse() {
        LoginRequest request = new LoginRequest("ana@example.com", "lozinka1");
        User user = User.builder().id(1L).firstName("Ana").lastName("Anić").email("ana@example.com").role(Role.RESIDENT).build();

        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.userId()).isEqualTo(1L);
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void login_userVanishesAfterAuthentication_throwsResourceNotFound() {
        LoginRequest request = new LoginRequest("ghost@example.com", "lozinka1");
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
