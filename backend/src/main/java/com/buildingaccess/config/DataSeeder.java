package com.buildingaccess.config;

import com.buildingaccess.model.User;
import com.buildingaccess.model.enums.Role;
import com.buildingaccess.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Pravi početni ADMIN nalog ako u bazi još ne postoji nijedan — inače nema načina da se napravi prvi admin. */
@Component
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private static final String DEFAULT_ADMIN_EMAIL = "admin@buildingaccess.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }

        User admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("Admin");
        admin.setEmail(DEFAULT_ADMIN_EMAIL);
        admin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
        admin.setRole(Role.ADMIN);
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);

        log.warn("Nije pronađen nijedan ADMIN nalog — kreiran podrazumevani: email={}, password={} (promeniti/obrisati pre produkcije!)",
                DEFAULT_ADMIN_EMAIL, DEFAULT_ADMIN_PASSWORD);
    }
}
