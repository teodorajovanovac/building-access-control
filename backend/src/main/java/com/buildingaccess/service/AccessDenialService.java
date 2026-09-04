package com.buildingaccess.service;

import com.buildingaccess.dto.denial.AccessDenialResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

public interface AccessDenialService {

    /** SK12 — dnevni odbijeni pokušaji zgrade. */
    List<AccessDenialResponse> getToday(Long buildingId);

    /** SK17 — pretraga/filtriranje/sortiranje/paginacija. */
    Page<AccessDenialResponse> search(Long buildingId, String reasonType, LocalDateTime from, LocalDateTime to,
                                       String text, Pageable pageable);

    /** Isti filteri kao search, ali bez paginacije — za CSV izvoz kompletnog rezultata pretrage. */
    List<AccessDenialResponse> export(Long buildingId, String reasonType, LocalDateTime from, LocalDateTime to,
                                       String text, Sort sort);
}
