package com.reliance.grievance.service.impl;

import com.reliance.grievance.dto.KnowledgeDocListDTO;
import com.reliance.grievance.entity.KnowledgeDoc;
import com.reliance.grievance.repository.KnowledgeDocRepository;
import com.reliance.grievance.service.KnowledgeDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class KnowledgeDocServiceImpl implements KnowledgeDocService {

    @Autowired
    KnowledgeDocRepository repo;

    public KnowledgeDoc save(String tag, String title, String description, MultipartFile file, String createdBy) {
        try {
            KnowledgeDoc doc = KnowledgeDoc.builder()
                    .tag(tag)
                    .title(title)
                    .description(description)
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType() != null ? file.getContentType() : "application/pdf")
                    .sizeBytes(file.getSize())
                    .data(file.getBytes())
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .createdBy(createdBy)
                    .build();
            return repo.save(doc);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save document", e);
        }
    }

    public List<KnowledgeDocListDTO> list(String tag) {
        var docs = (tag == null || tag.isBlank())
                ? repo.findByActiveTrueOrderByTagAscTitleAsc()
                : repo.findByActiveTrueAndTagIgnoreCaseOrderByTitleAsc(tag);

        return docs.stream().map(d -> KnowledgeDocListDTO.builder()
                .id(d.getId())
                .tag(d.getTag())
                .title(d.getTitle())
                .description(d.getDescription())
                .fileName(d.getFileName())
                .contentType(d.getContentType())
                .sizeBytes(d.getSizeBytes())
                .createdAt(d.getCreatedAt())
                .build()).toList();
    }

    public KnowledgeDoc get(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Document not found"));
    }

    public void softDelete(Long id) {
        var doc = get(id);
        doc.setActive(false);
        doc.setUpdatedAt(LocalDateTime.now());
        repo.save(doc);
    }
}
