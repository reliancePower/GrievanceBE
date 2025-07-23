package com.reliance.grievance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttachmentDTO {
    private Long id;
    private String fileName;
    private String fileType;
    private String base64Data;
}
