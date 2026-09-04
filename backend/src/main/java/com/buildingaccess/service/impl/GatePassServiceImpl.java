package com.buildingaccess.service.impl;

import com.buildingaccess.dto.gatepass.GatePassCreateRequest;
import com.buildingaccess.dto.gatepass.GatePassResponse;
import com.buildingaccess.dto.gatepass.GatePassUpdateRequest;
import com.buildingaccess.dto.gatepass.PassStatusHistoryResponse;
import com.buildingaccess.dto.gatepass.PublicGatePassResponse;
import com.buildingaccess.exception.InvalidStatusException;
import com.buildingaccess.exception.ResourceNotFoundException;
import com.buildingaccess.mapper.GatePassMapper;
import com.buildingaccess.model.GatePass;
import com.buildingaccess.model.PassStatusHistory;
import com.buildingaccess.model.User;
import com.buildingaccess.model.enums.GatePassStatus;
import com.buildingaccess.model.enums.Role;
import com.buildingaccess.repository.GatePassRepository;
import com.buildingaccess.repository.PassStatusHistoryRepository;
import com.buildingaccess.service.GatePassService;
import com.buildingaccess.service.MailService;
import com.buildingaccess.util.CodeGeneratorUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GatePassServiceImpl implements GatePassService {

    private final GatePassRepository gatePassRepository;
    private final PassStatusHistoryRepository historyRepository;
    private final MailService mailService;

    public GatePassServiceImpl(GatePassRepository gatePassRepository,
                                PassStatusHistoryRepository historyRepository,
                                MailService mailService) {
        this.gatePassRepository = gatePassRepository;
        this.historyRepository = historyRepository;
        this.mailService = mailService;
    }

    @Override
    public Page<GatePassResponse> getMine(User resident, Pageable pageable) {
        return gatePassRepository.findByCreatedById(resident.getId(), pageable).map(GatePassMapper::toResponse);
    }

    @Override
    public GatePassResponse getById(Long id, User currentUser) {
        GatePass gatePass = findEntity(id);
        assertOwnerOrElevated(gatePass, currentUser);
        return GatePassMapper.toResponse(gatePass);
    }

    @Override
    public List<PassStatusHistoryResponse> getHistory(Long id, User currentUser) {
        GatePass gatePass = findEntity(id);
        assertOwnerOrElevated(gatePass, currentUser);
        return historyRepository.findByGatePassIdOrderByChangedAtDesc(id).stream()
                .map(GatePassMapper::toHistoryResponse)
                .toList();
    }

    @Override
    public PublicGatePassResponse getPublicByCode(String code) {
        GatePass gatePass = gatePassRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Propusnica nije pronađena"));
        return GatePassMapper.toPublicResponse(gatePass);
    }

    /** Prazan tekst se pretvara u null da JPQL "is null" provera radi i za prazan string. */
    @Override
    public Page<GatePassResponse> search(Long buildingId, Long apartmentId, GatePassStatus status,
                                          LocalDateTime from, LocalDateTime to, String text, Pageable pageable) {
        return gatePassRepository.search(buildingId, apartmentId, status, from, to, blankToNull(text), pageable)
                .map(GatePassMapper::toResponse);
    }

    /** Isti filteri kao search, ali bez paginacije — za CSV izvoz kompletnog rezultata pretrage. */
    @Override
    public List<GatePassResponse> export(Long buildingId, Long apartmentId, GatePassStatus status,
                                          LocalDateTime from, LocalDateTime to, String text, Sort sort) {
        return gatePassRepository.search(buildingId, apartmentId, status, from, to, blankToNull(text), sort)
                .stream().map(GatePassMapper::toResponse).toList();
    }

    private String blankToNull(String text) {
        return (text == null || text.isBlank()) ? null : text;
    }

    private GatePass findEntity(Long id) {
        return gatePassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propusnica nije pronađena: " + id));
    }

    @Override
    @Transactional
    public GatePassResponse create(GatePassCreateRequest request, User resident) {
        validatePeriod(request.validFrom(), request.validTo());
        if (resident.getApartment() == null) {
            throw new IllegalArgumentException("Stanar nema dodeljen stan");
        }

        GatePass gatePass = new GatePass();
        gatePass.setCode(generateUniqueCode());
        gatePass.setGuestName(request.guestName());
        gatePass.setGuestPhone(request.guestPhone());
        gatePass.setGuestEmail(request.guestEmail());
        gatePass.setReason(request.reason());
        gatePass.setValidFrom(request.validFrom());
        gatePass.setValidTo(request.validTo());
        gatePass.setMaxEntries(request.maxEntries());
        gatePass.setUsedEntries(0);
        gatePass.setType(request.type());
        gatePass.setStatus(GatePassStatus.ACTIVE);
        gatePass.setCreatedAt(LocalDateTime.now());
        gatePass.setCreatedBy(resident);
        gatePass.setApartment(resident.getApartment());

        GatePass saved = gatePassRepository.save(gatePass);

        if (saved.getGuestEmail() != null && !saved.getGuestEmail().isBlank()) {
            mailService.sendGatePassToGuest(saved.getGuestEmail(), saved.getGuestName(), saved.getCode(),
                    saved.getReason(), saved.getValidFrom(), saved.getValidTo());
        }

        return GatePassMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public GatePassResponse update(Long id, GatePassUpdateRequest request, User resident) {
        GatePass gatePass = findEntity(id);
        assertOwner(gatePass, resident);
        if (gatePass.getStatus() != GatePassStatus.ACTIVE) {
            throw new InvalidStatusException("Propusnica se više ne može menjati!");
        }
        validatePeriod(request.validFrom(), request.validTo());
        if (request.maxEntries() < gatePass.getUsedEntries()) {
            throw new IllegalArgumentException(
                    "Broj dozvoljenih ulazaka ne može biti manji od već iskorišćenih (" + gatePass.getUsedEntries() + ")");
        }

        gatePass.setGuestName(request.guestName());
        gatePass.setGuestPhone(request.guestPhone());
        gatePass.setGuestEmail(request.guestEmail());
        gatePass.setReason(request.reason());
        gatePass.setValidFrom(request.validFrom());
        gatePass.setValidTo(request.validTo());
        gatePass.setMaxEntries(request.maxEntries());

        return GatePassMapper.toResponse(gatePass);
    }

    @Override
    @Transactional
    public void cancel(Long id, User resident) {
        GatePass gatePass = findEntity(id);
        assertOwner(gatePass, resident);
        if (gatePass.getStatus() != GatePassStatus.ACTIVE) {
            throw new InvalidStatusException("Propusnica se više ne može otkazati!");
        }
        changeStatus(gatePass, GatePassStatus.CANCELED, resident);
    }

    /** Sistemska/lenja promena statusa (poziva se iz AccessProcessingService pri skeniranju). */
    @Override
    public void changeStatus(GatePass gatePass, GatePassStatus newStatus, User changedBy) {
        GatePassStatus previous = gatePass.getStatus();
        if (previous == newStatus) {
            return;
        }
        gatePass.setStatus(newStatus);
        PassStatusHistory history = new PassStatusHistory();
        history.setGatePass(gatePass);
        history.setPreviousStatus(previous);
        history.setNewStatus(newStatus);
        history.setChangedAt(LocalDateTime.now());
        history.setChangedBy(changedBy);
        historyRepository.save(history);
    }

    private void validatePeriod(LocalDateTime validFrom, LocalDateTime validTo) {
        if (!validTo.isAfter(validFrom)) {
            throw new IllegalArgumentException("Period važenja nije ispravan (vaziDo mora biti posle vaziOd)");
        }
        if (validTo.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Datum važenja ne može biti u prošlosti");
        }
    }

    private void assertOwner(GatePass gatePass, User resident) {
        if (!gatePass.getCreatedBy().getId().equals(resident.getId())) {
            throw new AccessDeniedException("Propusnica ne pripada trenutnom korisniku");
        }
    }

    private void assertOwnerOrElevated(GatePass gatePass, User currentUser) {
        boolean owner = gatePass.getCreatedBy().getId().equals(currentUser.getId());
        boolean elevated = currentUser.getRole() != Role.RESIDENT;
        if (!owner && !elevated) {
            throw new AccessDeniedException("Propusnica ne pripada trenutnom korisniku");
        }
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = CodeGeneratorUtil.generate("GP", 8);
        } while (gatePassRepository.findByCode(code).isPresent());
        return code;
    }
}
