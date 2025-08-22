package com.reliance.grievance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttachmentDTO {
    private Long id;
    private String fileName;
    private String fileType;   // e.g. image/jpeg, application/pdf
    private Long sizeBytes;
    private LocalDateTime uploadedOn;
    private String base64Data; // <-- add this

    public AttachmentDTO(Long id, String fileName, String fileType,
                         Long sizeBytes, LocalDateTime uploadedOn, String base64Data) {
        this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
        this.sizeBytes = sizeBytes;
        this.uploadedOn = uploadedOn;
        this.base64Data = base64Data;
    }
}

