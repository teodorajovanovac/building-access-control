package com.buildingaccess.service;

import com.buildingaccess.dto.gatepass.GatePassCreateRequest;
import com.buildingaccess.dto.gatepass.GatePassResponse;
import com.buildingaccess.dto.gatepass.GatePassUpdateRequest;
import com.buildingaccess.dto.gatepass.PassStatusHistoryResponse;
import com.buildingaccess.dto.gatepass.PublicGatePassResponse;
import com.buildingaccess.model.GatePass;
import com.buildingaccess.model.User;
import com.buildingaccess.model.enums.GatePassStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

public interface GatePassService {

    Page<GatePassResponse> getMine(User resident, Pageable pageable);

    GatePassResponse getById(Long id, User currentUser);

    List<PassStatusHistoryResponse> getHistory(Long id, User currentUser);

    PublicGatePassResponse getPublicByCode(String code);

    /** SK17 — pretraga/filtriranje/sortiranje/paginacija (admin). */
    Page<GatePassResponse> search(Long buildingId, Long apartmentId, GatePassStatus status,
                                   LocalDateTime from, LocalDateTime to, String text, Pageable pageable);

    /** Isti filteri kao search, ali bez paginacije — za CSV izvoz kompletnog rezultata pretrage. */
    List<GatePassResponse> export(Long buildingId, Long apartmentId, GatePassStatus status,
                                   LocalDateTime from, LocalDateTime to, String text, Sort sort);

    GatePassResponse create(GatePassCreateRequest request, User resident);

    GatePassResponse update(Long id, GatePassUpdateRequest request, User resident);

    void cancel(Long id, User resident);

    /** Sistemska/lenja promena statusa — javno jer je poziva AccessProcessingService pri skeniranju. */
    void changeStatus(GatePass gatePass, GatePassStatus newStatus, User changedBy);
}
