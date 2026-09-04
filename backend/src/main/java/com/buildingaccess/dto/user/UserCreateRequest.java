package com.buildingaccess.dto.user;

import com.buildingaccess.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** apartmentId obavezan samo za RESIDENT, buildingId za SECURITY/STAFF, jobTitle opcion samo za STAFF. */
public record UserCreateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, message = "Lozinka mora imati bar 6 karaktera") String password,
        @NotNull Role role,
        Long apartmentId,
        Long buildingId,
        String jobTitle
) {
}
