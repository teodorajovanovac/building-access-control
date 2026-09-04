package com.buildingaccess.service;

import java.time.LocalDateTime;

public interface MailService {

    /** Fire and forget — ne sme da baci izuzetak ni da blokira poziv ako slanje ne uspe. */
    void sendGatePassUsedNotification(String toEmail, String residentFirstName, String guestName,
                                       String reason, LocalDateTime entryTime);

    /** Šalje se samo ako je stanar uneo guestEmail; fire and forget kao gornja metoda. */
    void sendGatePassToGuest(String toEmail, String guestName, String code, String reason,
                              LocalDateTime validFrom, LocalDateTime validTo);
}
