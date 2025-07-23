package com.reliance.grievance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDTO {

    private String senderType;     // "USER" or "AUTHORITY"
    private String senderId;       // email or employeeId
    private String message;        // the actual message
    private LocalDateTime sentOn;  // timestamp (optional in POST)
}
