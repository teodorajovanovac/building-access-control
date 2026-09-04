package com.buildingaccess.integration;

import com.buildingaccess.dto.apartment.ApartmentResponse;
import com.buildingaccess.dto.auth.AuthResponse;
import com.buildingaccess.dto.auth.LoginRequest;
import com.buildingaccess.dto.auth.RegisterRequest;
import com.buildingaccess.dto.building.BuildingRequest;
import com.buildingaccess.dto.building.BuildingResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Integracioni test: pristup /api/admin/** mora biti odbijen bez tokena (401) i sa pogrešnom ulogom (403). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminEndpointSecurityIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@buildingaccess.com";
    private static final String ADMIN_PASSWORD = "admin123";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private String login(String email, String password) throws Exception {
        LoginRequest request = new LoginRequest(email, password);
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(body, AuthResponse.class).token();
    }

    @Test
    void adminEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/buildings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpoint_withInvalidToken_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/buildings")
                        .header("Authorization", "Bearer this-is-not-a-valid-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpoint_withResidentRole_returns403() throws Exception {
        String adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);

        BuildingRequest buildingRequest = new BuildingRequest("Zgrada Auth Test", "Test adresa 3");
        String buildingBody = mockMvc.perform(post("/api/admin/buildings")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildingRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long buildingId = objectMapper.readValue(buildingBody, BuildingResponse.class).id();

        String apartmentBody = mockMvc.perform(post("/api/admin/apartments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new com.buildingaccess.dto.apartment.ApartmentRequest("303", 3, buildingId))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long apartmentId = objectMapper.readValue(apartmentBody, ApartmentResponse.class).id();

        RegisterRequest registerRequest = new RegisterRequest("Resident", "Only", "resident-authz-test@example.com",
                "lozinka1", apartmentId);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        String residentToken = login("resident-authz-test@example.com", "lozinka1");

        mockMvc.perform(get("/api/admin/buildings")
                        .header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void residentEndpoint_withSecurityRole_returns403() throws Exception {
        String adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);

        BuildingRequest buildingRequest = new BuildingRequest("Zgrada Auth Test 2", "Test adresa 4");
        String buildingBody = mockMvc.perform(post("/api/admin/buildings")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildingRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long buildingId = objectMapper.readValue(buildingBody, BuildingResponse.class).id();

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new com.buildingaccess.dto.user.UserCreateRequest(
                                "Sec", "Urity", "security-authz-test@example.com", "lozinka1",
                                com.buildingaccess.model.enums.Role.SECURITY, null, buildingId, null))))
                .andExpect(status().isCreated());

        String securityToken = login("security-authz-test@example.com", "lozinka1");

        mockMvc.perform(get("/api/resident/gatepasses")
                        .header("Authorization", "Bearer " + securityToken))
                .andExpect(status().isForbidden());
    }
}
