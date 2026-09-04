package com.buildingaccess.controller;

import com.buildingaccess.dto.common.PageResponse;
import com.buildingaccess.dto.entrylog.EntryLogResponse;
import com.buildingaccess.model.User;
import com.buildingaccess.model.enums.Role;
import com.buildingaccess.service.EntryLogService;
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

/** Upiti nad evidencijom ulazaka: dnevna evidencija i pretraga/filtriranje za admina i obezbeđenje. */
@RestController
@RequestMapping("/api/query/entrylogs")
@PreAuthorize("hasAnyRole('ADMIN','SECURITY')")
@Tag(name = "Entry Logs (query)", description = "SK12 Dnevna evidencija, SK17 Pretraga/filtriranje/sortiranje/paginacija")
public class EntryLogQueryController {

    private final EntryLogService entryLogService;

    public EntryLogQueryController(EntryLogService entryLogService) {
        this.entryLogService = entryLogService;
    }

    @GetMapping("/today")
    public List<EntryLogResponse> today(@RequestParam(required = false) Long buildingId,
                                         @AuthenticationPrincipal User currentUser) {
        return entryLogService.getToday(resolveBuildingId(buildingId, currentUser));
    }

    @GetMapping("/present")
    public List<EntryLogResponse> currentlyPresent(@RequestParam(required = false) Long buildingId,
                                                     @AuthenticationPrincipal User currentUser) {
        return entryLogService.getCurrentlyPresent(resolveBuildingId(buildingId, currentUser));
    }

    @GetMapping("/search")
    public PageResponse<EntryLogResponse> search(@RequestParam(required = false) Long buildingId,
                                                  @RequestParam(required = false) String personType,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                                  @RequestParam(required = false) String text,
                                                  @AuthenticationPrincipal User currentUser,
                                                  Pageable pageable) {
        Long effectiveBuildingId = currentUser.getRole() == Role.SECURITY
                ? currentUser.getBuilding().getId()
                : buildingId;
        return PageResponse.of(entryLogService.search(effectiveBuildingId, personType, from, to, text, pageable));
    }

    /** Izvoz u CSV (otvara se u Excel-u) — isti filteri kao pretraga, bez paginacije. */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) Long buildingId,
                                          @RequestParam(required = false) String personType,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                          @RequestParam(required = false) String text,
                                          @AuthenticationPrincipal User currentUser) {
        Long effectiveBuildingId = currentUser.getRole() == Role.SECURITY
                ? currentUser.getBuilding().getId()
                : buildingId;
        List<EntryLogResponse> rows = entryLogService.export(effectiveBuildingId, personType, from, to, text,
                Sort.by(Sort.Direction.DESC, "entryTime"));

        List<String> headers = List.of("Tip", "Ime", "Ulazak", "Izlazak", "Zgrada", "Propusnica", "Obradio", "Rucni unos");
        List<List<String>> csvRows = rows.stream().map(l -> List.of(
                l.personType().name(), l.personName(), String.valueOf(l.entryTime()),
                l.exitTime() == null ? "" : String.valueOf(l.exitTime()),
                l.buildingName(), l.gatePassCode() == null ? "" : l.gatePassCode(),
                l.processedByName() == null ? "" : l.processedByName(), l.manualEntry() ? "DA" : "NE"
        )).toList();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("evidencija-ulazaka.csv").build().toString())
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
