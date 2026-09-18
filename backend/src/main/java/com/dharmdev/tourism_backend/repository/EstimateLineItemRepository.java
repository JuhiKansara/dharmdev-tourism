package com.dharmdev.tourism_backend.repository;

import com.dharmdev.tourism_backend.entity.EstimateLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstimateLineItemRepository extends JpaRepository<EstimateLineItem, Long> {

    /** Return all line items belonging to a given estimate, ordered by PK (insertion order). */
    List<EstimateLineItem> findByEstimateIdOrderByIdAsc(Long estimateId);
}
