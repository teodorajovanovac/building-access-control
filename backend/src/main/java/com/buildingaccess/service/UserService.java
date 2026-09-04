package com.buildingaccess.service;

import com.buildingaccess.dto.user.UserCreateRequest;
import com.buildingaccess.dto.user.UserResponse;
import com.buildingaccess.dto.user.UserUpdateRequest;
import com.buildingaccess.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    List<UserResponse> getAll();

    /** Paginirana pretraga za admin tabele (SK15, SK16 — Korisnici i Osoblje). */
    Page<UserResponse> search(Role role, Long buildingId, Pageable pageable);

    UserResponse getById(Long id);

    UserResponse create(UserCreateRequest request);

    UserResponse update(Long id, UserUpdateRequest request);

    void delete(Long id);
}
