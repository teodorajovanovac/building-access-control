package com.buildingaccess.controller;

import com.buildingaccess.dto.common.PageResponse;
import com.buildingaccess.dto.denial.AccessDenialResponse;
import com.buildingaccess.model.User;
import com.buildingaccess.model.enums.Role;
import com.buildingaccess.service.AccessDenialService;
import com.buildingaccess.util.CsvUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/** SK12 dnevni odbijeni pokušaji, SK17 pretraga/filtriranje/sortiranje/paginacija. */
@RestController
@RequestMapping("/api/query/denials")
@PreAuthorize("hasAnyRole('ADMIN','SECURITY')")
@Tag(name = "Access Denials (query)", description = "SK12 Dnevni odbijeni pokušaji, SK17 Pretraga/filtriranje/sortiranje/paginacija")
public class AccessDenialQueryController {

    private final AccessDenialService accessDenialService;

    public AccessDenialQueryController(AccessDenialService accessDenialService) {
        this.accessDenialService = accessDenialService;
    }

    @GetMapping("/today")
    public List<AccessDenialResponse> today(@RequestParam(required = false) Long buildingId,
                                             @AuthenticationPrincipal User currentUser) {
        return accessDenialService.getToday(resolveBuildingId(buildingId, currentUser));
    }

    @GetMapping("/search")
    public PageResponse<AccessDenialResponse> search(@RequestParam(required = false) Long buildingId,
                                                       @RequestParam(required = false) String reasonType,
                                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                                       @RequestParam(required = false) String text,
                                                       @AuthenticationPrincipal User currentUser,
                                                       Pageable pageable) {
        Long effectiveBuildingId = currentUser.getRole() == Role.SECURITY
                ? currentUser.getBuilding().getId()
                : buildingId;
        return PageResponse.of(accessDenialService.search(effectiveBuildingId, reasonType, from, to, text, pageable));
    }

    /** Izvoz u CSV (otvara se u Excel-u) — isti filteri kao pretraga, bez paginacije. */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) Long buildingId,
                                          @RequestParam(required = false) String reasonType,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                          @RequestParam(required = false) String text,
                                          @AuthenticationPrincipal User currentUser) {
        Long effectiveBuildingId = currentUser.getRole() == Role.SECURITY
                ? currentUser.getBuilding().getId()
                : buildingId;
        List<AccessDenialResponse> rows = accessDenialService.export(effectiveBuildingId, reasonType, from, to, text,
                Sort.by(Sort.Direction.DESC, "attemptTime"));

        List<String> headers = List.of("Vreme pokusaja", "Uneti kod", "Ime lica", "Razlog", "Napomena", "Zgrada", "Obradio");
        List<List<String>> csvRows = rows.stream().map(d -> List.of(
                String.valueOf(d.attemptTime()), d.enteredCode() == null ? "" : d.enteredCode(),
                d.personName() == null ? "" : d.personName(), d.reasonType().name(),
                d.reasonNote() == null ? "" : d.reasonNote(), d.buildingName(),
                d.processedByName() == null ? "" : d.processedByName()
        )).toList();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("odbijeni-pokusaji.csv").build().toString())
                .body(CsvUtil.toCsv(headers, csvRows).getBytes(StandardCharsets.UTF_8));
    }

    private Long resolveBuildingId(Long requested, User currentUser) {
        if (currentUser.getRole() == Role.SECURITY) {
            return currentUser.getBuilding().getId();
        }
        if (requested == null) {
            throw new IllegalArgumentException("buildingId je obavezan parametar za admina");
        }
        return requested;
    }
}
