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
import com.buildingaccess.dto.gatepass.GatePassCreateRequest;
import com.buildingaccess.dto.gatepass.GatePassResponse;
import com.buildingaccess.dto.user.UserCreateRequest;
import com.buildingaccess.dto.user.UserResponse;
import com.buildingaccess.model.enums.GatePassType;
import com.buildingaccess.model.enums.GatePassStatus;
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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integracioni test celog HTTP toka (kontroler -> servis -> repo -> H2) za SK2/SK3/SK8:
 * registracija stanara -> prijava -> kreiranje propusnice za gosta -> obrada dolaska gosta
 * od strane obezbeđenja -> provera da je ulazak odobren i usedEntries uvećan.
 *
 * Admin nalog (admin@buildingaccess.com / admin123) kreira DataSeeder pri podizanju konteksta
 * (jedini način da se kroz API napravi zgrada/stan/obezbeđenje bez postojećeg admina).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GuestAccessFlowIntegrationTest {

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
    void fullGuestFlow_registerLoginCreatePassAndScan_approvesEntryAndIncrementsUsedEntries() throws Exception {
        String adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);

        // Admin kreira zgradu i stan (SK13/SK14).
        BuildingRequest buildingRequest = new BuildingRequest("Zgrada Integracioni Test", "Test adresa 1");
        String buildingBody = mockMvc.perform(post("/api/admin/buildings")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildingRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long buildingId = objectMapper.readValue(buildingBody, BuildingResponse.class).id();

        ApartmentRequest apartmentRequest = new ApartmentRequest("101", 1, buildingId);
        String apartmentBody = mockMvc.perform(post("/api/admin/apartments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apartmentRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long apartmentId = objectMapper.readValue(apartmentBody, ApartmentResponse.class).id();

        // Admin kreira nalog obezbeđenja dodeljen ovoj zgradi (SK15).
        UserCreateRequest securityRequest = new UserCreateRequest("Pera", "Perić", "security-guest-flow@example.com",
                "lozinka1", Role.SECURITY, null, buildingId, null);
        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(securityRequest)))
                .andExpect(status().isCreated());

        // SK2 — registracija stanara za novi stan.
        RegisterRequest registerRequest = new RegisterRequest("Ana", "Anić", "resident-guest-flow@example.com",
                "lozinka1", apartmentId);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // SK1 — prijava stanara i obezbeđenja.
        String residentToken = login("resident-guest-flow@example.com", "lozinka1");
        String securityToken = login("security-guest-flow@example.com", "lozinka1");

        // SK3 — stanar kreira propusnicu za gosta, dozvoljen tačno 1 ulazak.
        GatePassCreateRequest gatePassRequest = new GatePassCreateRequest("Gost Petrovic", "0601234567",
                "gost@example.com", "Poseta rodbine", LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1,
                GatePassType.SINGLE);
        String gatePassBody = mockMvc.perform(post("/api/resident/gatepasses")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gatePassRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        GatePassResponse createdPass = objectMapper.readValue(gatePassBody, GatePassResponse.class);
        assertThat(createdPass.status()).isEqualTo(GatePassStatus.ACTIVE);
        assertThat(createdPass.usedEntries()).isEqualTo(0);

        // SK8 — obezbeđenje skenira kod propusnice.
        ScanRequest scanRequest = new ScanRequest(createdPass.code());
        String scanBody = mockMvc.perform(post("/api/security/access/scan")
                        .header("Authorization", "Bearer " + securityToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scanRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        ScanResultResponse scanResult = objectMapper.readValue(scanBody, ScanResultResponse.class);

        assertThat(scanResult.outcome()).isEqualTo(ScanOutcome.GUEST_ENTRY_APPROVED);
        assertThat(scanResult.personName()).isEqualTo("Gost Petrovic");

        // maxEntries=1, pa nakon jednog ulaska propusnica prelazi u USED_UP (proveravamo kroz stanarov uvid, SK6).
        String afterScanBody = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/resident/gatepasses/{id}", createdPass.id())
                        .header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        GatePassResponse afterScan = objectMapper.readValue(afterScanBody, GatePassResponse.class);

        assertThat(afterScan.usedEntries()).isEqualTo(1);
        assertThat(afterScan.status()).isEqualTo(GatePassStatus.USED_UP);
    }
}
