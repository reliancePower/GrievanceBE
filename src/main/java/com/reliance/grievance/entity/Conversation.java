package com.reliance.grievance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grievance_id", nullable = false)
    private Grievance grievance;

    @Column(name = "sender_type", nullable = false, length = 20)
    private String senderType; // "USER" or "AUTHORITY"

    @Column(name = "sender_id", nullable = false, length = 100)
    private String senderId; // user ID or employee ID/email

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "sent_on", nullable = false)
    private LocalDateTime sentOn = LocalDateTime.now();
}
