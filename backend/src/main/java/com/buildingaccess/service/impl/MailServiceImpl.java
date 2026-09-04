package com.buildingaccess.service.impl;

import com.buildingaccess.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Najjednostavnija moguća implementacija (plain text, bez priloga) — SK-poželjno obaveštenje
 * stanaru kada se njegova propusnica iskoristi. Namerno "best effort": ako mejl nije podešen
 * (spring.mail.username prazan, npr. dok korisnica ne unese svoj Gmail App Password) ili ako
 * slanje ikad pukne, ovo NIKAD ne sme da uspori ili sruši scan endpoint — samo se loguje.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm");

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Override
    public void sendGatePassUsedNotification(String toEmail, String residentFirstName, String guestName,
                                              String reason, LocalDateTime entryTime) {
        if (mailUsername == null || mailUsername.isBlank()) {
            log.info("Mejl nije konfigurisan (spring.mail.username prazan) — obaveštenje za {} preskočeno.", toEmail);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Vaš gost je ušao u zgradu");
            message.setText("Poštovani " + residentFirstName + ",\n\nVaš gost " + guestName
                    + " je upravo ušao u zgradu koristeći propusnicu (" + reason + "), u "
                    + entryTime.format(TIME_FORMAT) + ".");
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Slanje mejl obaveštenja korisniku {} nije uspelo: {}", toEmail, ex.getMessage());
        }
    }
}
