package com.buildingaccess.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/** Unit testovi za MailServiceImpl: nekonfigurisan mejl se tiho preskače, greška pri slanju se guta. */
@ExtendWith(MockitoExtension.class)
class MailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    private MailServiceImpl mailService;

    @BeforeEach
    void setUp() {
        mailService = new MailServiceImpl(mailSender);
    }

    @Test
    void sendGatePassUsedNotification_usernameNotConfigured_neverCallsMailSender() {
        ReflectionTestUtils.setField(mailService, "mailUsername", "");

        mailService.sendGatePassUsedNotification(
                "stanar@example.com", "Marko", "Gost Gostić", "Poseta", LocalDateTime.now());

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendGatePassUsedNotification_usernameConfigured_sendsExpectedMessage() {
        ReflectionTestUtils.setField(mailService, "mailUsername", "app@gmail.com");
        LocalDateTime entryTime = LocalDateTime.of(2026, 9, 4, 14, 30);

        mailService.sendGatePassUsedNotification(
                "stanar@example.com", "Marko", "Gost Gostić", "Poseta", entryTime);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getTo()).containsExactly("stanar@example.com");
        assertThat(sent.getText()).contains("Marko", "Gost Gostić", "Poseta", "04.09.2026. 14:30");
    }

    @Test
    void sendGatePassUsedNotification_mailSenderThrows_exceptionIsSwallowed() {
        ReflectionTestUtils.setField(mailService, "mailUsername", "app@gmail.com");
        doThrow(new org.springframework.mail.MailSendException("smtp down"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertThat(org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            mailService.sendGatePassUsedNotification(
                    "stanar@example.com", "Marko", "Gost Gostić", "Poseta", LocalDateTime.now());
            return true;
        })).isTrue();
    }
}
