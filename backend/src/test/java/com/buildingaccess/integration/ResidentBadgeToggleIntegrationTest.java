package com.buildingaccess.integration;

import com.buildingaccess.dto.access.ScanOutcome;
import com.buildingaccess.dto.access.ScanRequest;
import com.buildingaccess.dto.access.ScanResultResponse;
import com.buildingaccess.dto.apartment.ApartmentRequest;
import com.buildingaccess.dto.apartment.ApartmentResponse;
import com.buildingaccess.dto.auth.AuthResponse;
import com.buildingaccess.dto.auth.LoginRequest;
import com.buildingaccess.dto.auth.RegisterRequest;
import com.buildingaccess.dto.building.BuildingRequest;
import com.buildingaccess.dto.building.BuildingResponse;
import com.buildingaccess.dto.user.UserCreateRequest;
import com.buildingaccess.dto.user.UserResponse;
import com.buildingaccess.model.enums.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Integracioni test: dva uzastopna skeniranja bedža stanara — prvo ulazak, drugo izlazak. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ResidentBadgeToggleIntegrationTest {

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
    void residentBadge_scannedTwiceInARow_firstRecordsEntryThenExit() throws Exception {
        String adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);

        BuildingRequest buildingRequest = new BuildingRequest("Zgrada Bedz Test", "Test adresa 2");
        String buildingBody = mockMvc.perform(post("/api/admin/buildings")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildingRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long buildingId = objectMapper.readValue(buildingBody, BuildingResponse.class).id();

        ApartmentRequest apartmentRequest = new ApartmentRequest("202", 2, buildingId);
        String apartmentBody = mockMvc.perform(post("/api/admin/apartments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apartmentRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long apartmentId = objectMapper.readValue(apartmentBody, ApartmentResponse.class).id();

        UserCreateRequest securityRequest = new UserCreateRequest("Jovan", "Jovanović", "security-badge-flow@example.com",
                "lozinka1", Role.SECURITY, null, buildingId, null);
        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(securityRequest)))
                .andExpect(status().isCreated());

        RegisterRequest registerRequest = new RegisterRequest("Milica", "Milić", "resident-badge-flow@example.com",
                "lozinka1", apartmentId);
        String registerBody = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long residentId = objectMapper.readValue(registerBody, AuthResponse.class).userId();

        String securityToken = login("security-badge-flow@example.com", "lozinka1");

        // Bedž kod stanara nije deo AuthResponse-a (osetljiv trajni kod) — dohvata se preko admin uvida u korisnika.
        String residentUserBody = mockMvc.perform(get("/api/admin/users/{id}", residentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String badgeCode = objectMapper.readValue(residentUserBody, UserResponse.class).badgeCode();
        assertThat(badgeCode).isNotBlank();

        ScanRequest scanRequest = new ScanRequest(badgeCode);

        String firstScanBody = mockMvc.perform(post("/api/security/access/scan")
                        .header("Authorization", "Bearer " + securityToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scanRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        ScanResultResponse firstScan = objectMapper.readValue(firstScanBody, ScanResultResponse.class);
        assertThat(firstScan.outcome()).isEqualTo(ScanOutcome.ENTRY_RECORDED);

        String secondScanBody = mockMvc.perform(post("/api/security/access/scan")
                        .header("Authorization", "Bearer " + securityToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scanRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        ScanResultResponse secondScan = objectMapper.readValue(secondScanBody, ScanResultResponse.class);
        assertThat(secondScan.outcome()).isEqualTo(ScanOutcome.EXIT_RECORDED);
    }
}
