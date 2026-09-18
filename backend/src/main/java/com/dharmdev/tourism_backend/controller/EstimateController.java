package com.dharmdev.tourism_backend.controller;

import com.dharmdev.tourism_backend.dto.CreateEstimateRequest;
import com.dharmdev.tourism_backend.entity.Customer;
import com.dharmdev.tourism_backend.entity.Estimate;
import com.dharmdev.tourism_backend.entity.EstimateLineItem;
import com.dharmdev.tourism_backend.repository.CustomerRepository;
import com.dharmdev.tourism_backend.repository.EstimateRepository;
import com.dharmdev.tourism_backend.service.GstCalculationService;
import com.dharmdev.tourism_backend.service.PdfGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/estimates")
@RequiredArgsConstructor
public class EstimateController {

    private final EstimateRepository estimateRepository;
    private final CustomerRepository customerRepository;
    private final GstCalculationService gstCalculationService;
    private final PdfGenerationService pdfGenerationService;

    @GetMapping("/{id}")
    public GstCalculationService.EstimateCalculation getEstimate(@PathVariable Long id) {
        Estimate estimate = estimateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estimate not found: " + id));
        return gstCalculationService.calculate(estimate);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getEstimatePdf(@PathVariable Long id) throws Exception {
        Estimate estimate = estimateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estimate not found: " + id));
        var calculation = gstCalculationService.calculate(estimate);
        byte[] pdfBytes = pdfGenerationService.generateEstimatePdf(calculation);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=estimate-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping
    public GstCalculationService.EstimateCalculation create(@RequestBody CreateEstimateRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new RuntimeException("Customer not found: " + request.customerId()));

        Estimate estimate = Estimate.builder()
                .estimateNo(request.estimateNo())
                .estimateDate(request.estimateDate())
                .customer(customer)
                .placeOfSupplyStateCode(request.placeOfSupplyStateCode())
                .description(request.description())
                .termsAndConditions(request.termsAndConditions())
                .status("DRAFT")
                .build();

        List<EstimateLineItem> lineItems = request.lineItems().stream()
                .map(li -> EstimateLineItem.builder()
                        .estimate(estimate)
                        .itemName(li.itemName())
                        .hsnSac(li.hsnSac())
                        .quantity(li.quantity())
                        .pricePerUnit(li.pricePerUnit())
                        .gstRate(li.gstRate())
                        .build())
                .collect(Collectors.toList());

        estimate.setLineItems(lineItems);

        Estimate saved = estimateRepository.save(estimate);
        return gstCalculationService.calculate(saved);
    }
}