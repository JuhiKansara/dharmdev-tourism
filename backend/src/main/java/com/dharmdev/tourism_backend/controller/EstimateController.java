package com.dharmdev.tourism_backend.controller;

import com.dharmdev.tourism_backend.entity.Estimate;
import com.dharmdev.tourism_backend.repository.EstimateRepository;
import com.dharmdev.tourism_backend.service.GstCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/estimates")
@RequiredArgsConstructor
public class EstimateController {

    private final EstimateRepository estimateRepository;
    private final GstCalculationService gstCalculationService;

    @GetMapping("/{id}")
    public GstCalculationService.EstimateCalculation getEstimate(@PathVariable Long id) {
        Estimate estimate = estimateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estimate not found: " + id));
        return gstCalculationService.calculate(estimate);
    }
}