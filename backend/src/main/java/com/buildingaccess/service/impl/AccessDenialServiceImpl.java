package com.buildingaccess.service.impl;

import com.buildingaccess.dto.denial.AccessDenialResponse;
import com.buildingaccess.mapper.AccessDenialMapper;
import com.buildingaccess.model.AccessDenial;
import com.buildingaccess.model.enums.DenialReasonType;
import com.buildingaccess.repository.AccessDenialRepository;
import com.buildingaccess.service.AccessDenialService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccessDenialServiceImpl implements AccessDenialService {

    private final AccessDenialRepository accessDenialRepository;

    /** SK12 — dnevni odbijeni pokušaji zgrade. */
    @Override
    public List<AccessDenialResponse> getToday(Long buildingId) {
        LocalDateTime from = LocalDate.now().atStartOfDay();
        LocalDateTime to = LocalDate.now().atTime(LocalTime.MAX);
        return accessDenialRepository.findByBuildingIdAndAttemptTimeBetweenOrderByAttemptTimeDesc(buildingId, from, to)
                .stream().map(AccessDenialMapper::toResponse).toList();
    }

    /** SK17 — pretraga/filtriranje/sortiranje/paginacija. */
    @Override
    public Page<AccessDenialResponse> search(Long buildingId, String reasonType, LocalDateTime from, LocalDateTime to,
                                              String text, Pageable pageable) {
        Specification<AccessDenial> spec = buildSpecification(buildingId, reasonType, from, to, text);
        return accessDenialRepository.findAll(spec, pageable).map(AccessDenialMapper::toResponse);
    }

    /** Isti filteri kao search, ali bez paginacije — za CSV izvoz kompletnog rezultata pretrage. */
    @Override
    public List<AccessDenialResponse> export(Long buildingId, String reasonType, LocalDateTime from, LocalDateTime to,
                                              String text, Sort sort) {
        Specification<AccessDenial> spec = buildSpecification(buildingId, reasonType, from, to, text);
        return accessDenialRepository.findAll(spec, sort).stream().map(AccessDenialMapper::toResponse).toList();
    }

    private Specification<AccessDenial> buildSpecification(Long buildingId, String reasonType, LocalDateTime from,
                                                             LocalDateTime to, String text) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (buildingId != null) {
                predicates.add(cb.equal(root.get("building").get("id"), buildingId));
            }
            if (reasonType != null && !reasonType.isBlank()) {
                predicates.add(cb.equal(root.get("reasonType"), DenialReasonType.valueOf(reasonType)));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("attemptTime"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("attemptTime"), to));
            }
            if (text != null && !text.isBlank()) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("personName")), "%" + text.toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("enteredCode")), "%" + text.toLowerCase() + "%")
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
