package com.reliance.grievance.dto;

import java.time.LocalDateTime;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KnowledgeDocListDTO {
    private Long id;
    private String tag;
    private String title;
    private String description;
    private String fileName;
    private String contentType;
    private long sizeBytes;
    private LocalDateTime createdAt;
}
