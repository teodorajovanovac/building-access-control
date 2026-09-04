package com.buildingaccess.service.impl;

import com.buildingaccess.dto.user.UserCreateRequest;
import com.buildingaccess.dto.user.UserResponse;
import com.buildingaccess.dto.user.UserUpdateRequest;
import com.buildingaccess.exception.DuplicateResourceException;
import com.buildingaccess.exception.InvalidStatusException;
import com.buildingaccess.exception.ResourceNotFoundException;
import com.buildingaccess.mapper.UserMapper;
import com.buildingaccess.model.Apartment;
import com.buildingaccess.model.Building;
import com.buildingaccess.model.User;
import com.buildingaccess.model.enums.Role;
import com.buildingaccess.repository.AccessDenialRepository;
import com.buildingaccess.repository.EntryLogRepository;
import com.buildingaccess.repository.GatePassRepository;
import com.buildingaccess.repository.UserRepository;
import com.buildingaccess.service.ApartmentService;
import com.buildingaccess.service.BuildingService;
import com.buildingaccess.service.UserService;
import com.buildingaccess.util.CodeGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApartmentService apartmentService;
    private final BuildingService buildingService;
    private final GatePassRepository gatePassRepository;
    private final EntryLogRepository entryLogRepository;
    private final AccessDenialRepository accessDenialRepository;

    @Override
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(UserMapper::toResponse).toList();
    }

    /** Paginirana pretraga za admin tabele (SK15, SK16 — Korisnici i Osoblje). */
    @Override
    public Page<UserResponse> search(Role role, Long buildingId, Pageable pageable) {
        Page<User> page;
        if (role != null && buildingId != null) {
            page = userRepository.findByRoleAndBuildingId(role, buildingId, pageable);
        } else if (role != null) {
            page = userRepository.findByRole(role, pageable);
        } else {
            page = userRepository.findAll(pageable);
        }
        return page.map(UserMapper::toResponse);
    }

    @Override
    public UserResponse getById(Long id) {
        return UserMapper.toResponse(findEntity(id));
    }

    private User findEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik nije pronađen: " + id));
    }

    @Override
    @Transactional
    public UserResponse create(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Nalog sa ovim email-om već postoji!");
        }

        User.UserBuilder builder = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .createdAt(LocalDateTime.now());

        switch (request.role()) {
            case RESIDENT -> {
                if (request.apartmentId() == null) {
                    throw new IllegalArgumentException("apartmentId je obavezan za ulogu RESIDENT");
                }
                Apartment apartment = apartmentService.findEntity(request.apartmentId());
                builder.apartment(apartment).badgeCode(generateUniqueBadgeCode());
            }
            case SECURITY -> {
                if (request.buildingId() == null) {
                    throw new IllegalArgumentException("buildingId je obavezan za ulogu SECURITY");
                }
                Building building = buildingService.findEntity(request.buildingId());
                builder.building(building);
            }
            case STAFF -> {
                if (request.buildingId() == null) {
                    throw new IllegalArgumentException("buildingId je obavezan za ulogu STAFF");
                }
                Building building = buildingService.findEntity(request.buildingId());
                builder.building(building).badgeCode(generateUniqueBadgeCode()).jobTitle(request.jobTitle());
            }
            case ADMIN -> {
                // nema dodatnih obaveznih polja
            }
        }

        return UserMapper.toResponse(userRepository.save(builder.build()));
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = findEntity(id);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());

        if (user.getRole() == Role.RESIDENT && request.apartmentId() != null) {
            user.setApartment(apartmentService.findEntity(request.apartmentId()));
        }
        if (user.getRole() == Role.SECURITY && request.buildingId() != null) {
            user.setBuilding(buildingService.findEntity(request.buildingId()));
        }
        if (user.getRole() == Role.STAFF) {
            if (request.buildingId() != null) {
                user.setBuilding(buildingService.findEntity(request.buildingId()));
            }
            user.setJobTitle(request.jobTitle());
        }

        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = findEntity(id);
        boolean referenced = gatePassRepository.existsByCreatedById(id)
                || entryLogRepository.existsByUserId(id)
                || entryLogRepository.existsByProcessedById(id)
                || accessDenialRepository.existsByProcessedById(id);
        if (referenced) {
            throw new InvalidStatusException(
                    "Korisnik ima povezanu istoriju (propusnice/evidenciju) — ne može se obrisati.");
        }
        userRepository.delete(user);
    }

    private String generateUniqueBadgeCode() {
        String code;
        do {
            code = CodeGeneratorUtil.generate("RES", 8);
        } while (userRepository.existsByBadgeCode(code));
        return code;
    }
}
