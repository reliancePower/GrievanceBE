package com.reliance.grievance.repository;

import com.reliance.grievance.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {

    List<Conversation> findByGrievanceIdOrderBySentOnAsc(Integer grievanceId);
}
