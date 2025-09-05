package com.reliance.grievance.service;

import com.reliance.grievance.dto.KnowledgeDocListDTO;
import com.reliance.grievance.entity.KnowledgeDoc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface KnowledgeDocService {
    KnowledgeDoc save(String tag, String title, String description, MultipartFile file, String createdBy);

    List<KnowledgeDocListDTO> list(String tag);

    KnowledgeDoc get(Long id);

    void softDelete(Long id);
}
