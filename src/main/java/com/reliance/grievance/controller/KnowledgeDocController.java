package com.reliance.grievance.controller;

import com.reliance.grievance.dto.KnowledgeDocListDTO;
import com.reliance.grievance.entity.KnowledgeDoc;
import com.reliance.grievance.service.KnowledgeDocService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/common/docs")
@RequiredArgsConstructor
public class KnowledgeDocController {

    @Autowired
    KnowledgeDocService service;

    // Admin upload (multipart/form-data)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<KnowledgeDocListDTO> upload(
            @RequestParam String tag,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String createdBy
    ) {
        KnowledgeDoc saved = service.save(tag, title, description, file, createdBy);
        KnowledgeDocListDTO dto = new KnowledgeDocListDTO(
                saved.getId(), saved.getTag(), saved.getTitle(), saved.getDescription(),
                saved.getFileName(), saved.getContentType(), saved.getSizeBytes(), saved.getCreatedAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // List for UI (optionally filter by tag)
    @GetMapping
    public List<KnowledgeDocListDTO> list(@RequestParam(required = false) String tag) {
        return service.list(tag);
    }

    // Download by id
    @GetMapping("/{id}/download")
    public ResponseEntity<ByteArrayResource> download(@PathVariable Long id) {
        KnowledgeDoc doc = service.get(id);

        String encoded = URLEncoder.encode(doc.getFileName(), StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                doc.getContentType() != null ? doc.getContentType() : "application/pdf"));
        headers.setContentLength(doc.getSizeBytes());
        headers.setContentDisposition(ContentDisposition.attachment().filename(encoded).build());

        return new ResponseEntity<>(new ByteArrayResource(doc.getData()), headers, HttpStatus.OK);
    }

    // Soft delete (optional, admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
