package com.reliance.grievance.repository;

import com.reliance.grievance.entity.KnowledgeDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface KnowledgeDocRepository extends JpaRepository<KnowledgeDoc, Long> {
    List<KnowledgeDoc> findByActiveTrueOrderByTagAscTitleAsc();
    List<KnowledgeDoc> findByActiveTrueAndTagIgnoreCaseOrderByTitleAsc(String tag);
}
