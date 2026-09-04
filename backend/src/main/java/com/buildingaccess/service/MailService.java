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

    /**
     * Šalje gostu kod propusnice i link ka javnoj stranici (bez prijave) čim je stanar napravi —
     * samo ako je stanar uneo guestEmail. "Fire and forget", isto kao gornja metoda.
     */
    void sendGatePassToGuest(String toEmail, String guestName, String code, String reason,
                              LocalDateTime validFrom, LocalDateTime validTo);
}
