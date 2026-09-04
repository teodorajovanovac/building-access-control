package com.buildingaccess.service;

import com.buildingaccess.dto.entrylog.EntryLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

public interface EntryLogService {

    /** SK12 — dnevna evidencija zgrade (uključuje lica trenutno prisutna, bez evidentiranog izlaska). */
    List<EntryLogResponse> getToday(Long buildingId);

    List<EntryLogResponse> getCurrentlyPresent(Long buildingId);

    /** SK7 — evidencija ulazaka povezanih sa stanom (sopstveni ulasci + gosti preko propusnica stana). */
    List<EntryLogResponse> getForApartment(Long apartmentId);

    /** SK17 — pretraga/filtriranje/sortiranje/paginacija. */
    Page<EntryLogResponse> search(Long buildingId, String personType, LocalDateTime from, LocalDateTime to,
                                   String text, Pageable pageable);

    /** Isti filteri kao search, ali bez paginacije — za CSV izvoz kompletnog rezultata pretrage. */
    List<EntryLogResponse> export(Long buildingId, String personType, LocalDateTime from, LocalDateTime to,
                                   String text, Sort sort);
}
