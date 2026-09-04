package com.buildingaccess.service;

import com.buildingaccess.dto.access.ManualDenyRequest;
import com.buildingaccess.dto.access.PersonSearchResponse;
import com.buildingaccess.dto.access.ScanResultResponse;
import com.buildingaccess.model.User;
import com.buildingaccess.model.enums.PersonType;

import java.util.List;

/**
 * Srce aplikacije (SK8-SK11): jedan scan endpoint za obezbeđenje prepoznaje da li je uneti/skenirani
 * kod propusnica gosta, lični bedž stanara ili bedž osoblja, i primenjuje odgovarajuću logiku —
 * uključujući automatsko naizmenično evidentiranje ulaska/izlaska za stanara i osoblje.
 */
public interface AccessProcessingService {

    ScanResultResponse processScan(String code, User security);

    /** SK10 — ručna pretraga stanara/osoblja svoje zgrade (ime ili broj stana). */
    List<PersonSearchResponse> searchPeople(String query, User security);

    /** SK10 — evidentiranje dolaska ručno pronađene osobe, ista naizmenična logika kao SK9. */
    ScanResultResponse processManual(PersonType type, Long id, User security);

    /** SK11 — ručno odbijanje ulaska, razlog je uvek obavezan. */
    ScanResultResponse manualDeny(ManualDenyRequest request, User security);
}
