package com.buildingaccess.service.impl;

import com.buildingaccess.service.MailService;
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
public class MailServiceImpl implements MailService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm");

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${app.public-url:http://localhost:5173}")
    private String publicUrl;

    public MailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendGatePassUsedNotification(String toEmail, String residentFirstName, String guestName,
                                              String reason, LocalDateTime entryTime) {
        if (!isConfigured(toEmail)) {
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Vaš gost je ušao u zgradu");
        message.setText("Poštovani " + residentFirstName + ",\n\nVaš gost " + guestName
                + " je upravo ušao u zgradu koristeći propusnicu (" + reason + "), u "
                + entryTime.format(TIME_FORMAT) + ".");
        send(message, toEmail);
    }

    @Override
    public void sendGatePassToGuest(String toEmail, String guestName, String code, String reason,
                                     LocalDateTime validFrom, LocalDateTime validTo) {
        if (!isConfigured(toEmail)) {
            return;
        }
        String link = publicUrl + "/pass/" + code;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Vaša propusnica za ulazak u zgradu");
        message.setText("Poštovani/a " + guestName + ",\n\nDobili ste propusnicu za ulazak u zgradu.\n\n"
                + "Kod propusnice: " + code + "\n"
                + "Razlog: " + reason + "\n"
                + "Važi od: " + validFrom.format(TIME_FORMAT) + "\n"
                + "Važi do: " + validTo.format(TIME_FORMAT) + "\n\n"
                + "Detalje možete videti i ovde: " + link + "\n\n"
                + "Ovaj kod pokažite obezbeđenju pri dolasku.");
        send(message, toEmail);
    }

    private boolean isConfigured(String toEmail) {
        if (mailUsername == null || mailUsername.isBlank()) {
            log.info("Mejl nije konfigurisan (spring.mail.username prazan) — poruka za {} preskočena.", toEmail);
            return false;
        }
        return true;
    }

    private void send(SimpleMailMessage message, String toEmail) {
        try {
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Slanje mejla korisniku {} nije uspelo: {}", toEmail, ex.getMessage());
        }
    }
}
