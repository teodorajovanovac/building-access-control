package com.buildingaccess.service;

import com.buildingaccess.dto.auth.AuthResponse;
import com.buildingaccess.dto.auth.LoginRequest;
import com.buildingaccess.dto.auth.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
