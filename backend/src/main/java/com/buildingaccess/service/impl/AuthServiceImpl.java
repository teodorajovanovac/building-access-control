package com.buildingaccess.service.impl;

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
import com.buildingaccess.service.AuthService;
import com.buildingaccess.util.CodeGeneratorUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final ApartmentRepository apartmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                            ApartmentRepository apartmentRepository,
                            PasswordEncoder passwordEncoder,
                            AuthenticationManager authenticationManager,
                            JwtService jwtService) {
        this.userRepository = userRepository;
        this.apartmentRepository = apartmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Nalog sa ovim email-om već postoji!");
        }
        Apartment apartment = apartmentRepository.findById(request.apartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Stan nije pronađen: " + request.apartmentId()));

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.RESIDENT);
        user.setBadgeCode(generateUniqueBadgeCode());
        user.setApartment(apartment);
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        return toAuthResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik nije pronađen"));
        return toAuthResponse(user);
    }

    private String generateUniqueBadgeCode() {
        String code;
        do {
            code = CodeGeneratorUtil.generate("RES", 8);
        } while (userRepository.existsByBadgeCode(code));
        return code;
    }

    private AuthResponse toAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole());
    }
}
