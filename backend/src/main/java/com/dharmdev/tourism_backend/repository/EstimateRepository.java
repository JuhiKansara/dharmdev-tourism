package com.dharmdev.tourism_backend.repository;

import com.dharmdev.tourism_backend.entity.Estimate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstimateRepository extends JpaRepository<Estimate, Long> {

    /** Fetch the highest estimate_no so far — used when generating the next sequential number. */
    Optional<Estimate> findTopByOrderByEstimateNoDesc();
}
