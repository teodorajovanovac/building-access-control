package com.buildingaccess.controller;

import com.buildingaccess.dto.common.PageResponse;
import com.buildingaccess.dto.gatepass.GatePassResponse;
import com.buildingaccess.model.enums.GatePassStatus;
import com.buildingaccess.service.GatePassService;
import com.buildingaccess.util.CsvUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/** SK17 — pretraga/filtriranje/sortiranje/paginacija svih propusnica (admin). */
@RestController
@RequestMapping("/api/admin/gatepasses")
@RequiredArgsConstructor
@Tag(name = "Gate Passes (Admin)", description = "SK17 Pregled/pretraga svih propusnica")
public class GatePassQueryController {

    private final GatePassService gatePassService;

    @GetMapping("/search")
    public PageResponse<GatePassResponse> search(@RequestParam(required = false) Long buildingId,
                                                  @RequestParam(required = false) Long apartmentId,
                                                  @RequestParam(required = false) GatePassStatus status,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                                  @RequestParam(required = false) String text,
                                                  Pageable pageable) {
        return PageResponse.of(gatePassService.search(buildingId, apartmentId, status, from, to, text, pageable));
    }

    /** Izvoz u CSV (otvara se u Excel-u) — isti filteri kao pretraga, bez paginacije. */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) Long buildingId,
                                          @RequestParam(required = false) Long apartmentId,
                                          @RequestParam(required = false) GatePassStatus status,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                          @RequestParam(required = false) String text) {
        List<GatePassResponse> rows = gatePassService.export(buildingId, apartmentId, status, from, to, text,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        List<String> headers = List.of("Kod", "Gost", "Telefon", "Razlog", "Stan", "Zgrada", "Tip", "Status",
                "Ulasci", "Maks. ulazaka", "Vazi od", "Vazi do", "Kreirana");
        List<List<String>> csvRows = rows.stream().map(p -> List.of(
                p.code(), p.guestName(), nullToEmpty(p.guestPhone()), p.reason(), p.apartmentNumber(), p.buildingName(),
                p.type().name(), p.status().name(), String.valueOf(p.usedEntries()), String.valueOf(p.maxEntries()),
                String.valueOf(p.validFrom()), String.valueOf(p.validTo()), String.valueOf(p.createdAt())
        )).toList();

        return csvResponse("propusnice.csv", CsvUtil.toCsv(headers, csvRows));
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static ResponseEntity<byte[]> csvResponse(String filename, String csv) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }
}
