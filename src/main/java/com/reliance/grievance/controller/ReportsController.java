package com.reliance.grievance.controller;


import com.reliance.grievance.repository.ReportsRepository;
import com.reliance.grievance.repository.ReportsRow;
import com.reliance.grievance.util.ReportsAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reports")
public class ReportsController {

    private final ReportsRepository repo;
    private final ReportsAuthService auth;

    public ReportsController(ReportsRepository repo, ReportsAuthService auth) {
        this.repo = repo;
        this.auth = auth;
    }

    private void assertSuper(HttpServletRequest req) {
        String user = req.getHeader("X-User");
        if (!user.contains("@"))
            user = generateEmail(user);
        if (!auth.isSuper(user)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized");
        }
    }

    // GET /reports/grievances?from=2025-08-01&to=2025-09-01&page=0&size=50
    @GetMapping("/grievances")
    public Page<ReportsRow> list(HttpServletRequest req,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "50") int size) {

//        assertSuper(req);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay(); // [from, to)
        return repo.findBetween(start, end, PageRequest.of(page, size));
    }

    @GetMapping("/grievances/export")
    public ResponseEntity<ByteArrayResource> exportCsv(HttpServletRequest req,
                                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

//        assertSuper(req);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        Page<ReportsRow> page = repo.findBetween(start, end, PageRequest.of(0, 100000));
        String csv = buildCsv(page);

        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv; charset=utf-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"grievances_" + from + "_to_" + to + ".csv\"")
                .contentLength(bytes.length)
                .body(new ByteArrayResource(bytes));
    }

    private String buildCsv(Page<ReportsRow> page) {
        String header = "ID,Location,Category,Sub Category,Subject,Status,Assigned To,Submitted On,Updated On,Details,Tat(Days)\n";
        String rows = page.getContent().stream().map(r ->
                String.join(",",
                        s(r.getId()),
                        s(r.getLocationName()),
                        s(r.getCategoryName()),
                        s(r.getSubcategoryName()),
                        s(r.getSubject()),
                        s(r.getStatus()),
                        s(r.getAssignedTo()),
                        s(r.getSubmittedOn()),
                        s(r.getUpdateDate()),
                        s(r.getDetails()),
                        s(r.getTatDays())
                )).collect(Collectors.joining("\n"));
        return header + rows + "\n";
    }

    private static String s(Object o) {
        if (o == null) return "";
        String v = o.toString();
        // rudimentary CSV escaping
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    public String generateEmail(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }

        String[] parts = fullName.trim().toLowerCase().split("\\s+");

        String joined = String.join(".", parts);

        return joined + "@reliancegroupindia.com";
    }
}
