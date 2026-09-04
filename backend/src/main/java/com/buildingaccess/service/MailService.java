package com.buildingaccess.service;

import java.time.LocalDateTime;

public interface MailService {

    /**
     * Obaveštava stanara mejlom da je njegov gost upravo ušao u zgradu koristeći propusnicu.
     * "Fire and forget" — ne sme da baci izuzetak niti da blokira poziv ukoliko slanje ne uspe
     * ili mejl nije konfigurisan (v. MailServiceImpl).
     */
    void sendGatePassUsedNotification(String toEmail, String residentFirstName, String guestName,
                                       String reason, LocalDateTime entryTime);
}
