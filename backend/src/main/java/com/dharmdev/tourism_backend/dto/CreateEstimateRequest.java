package com.dharmdev.tourism_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateEstimateRequest(
        Integer estimateNo,
        LocalDate estimateDate,
        Long customerId,
        String placeOfSupplyStateCode,
        String description,
        String termsAndConditions,
        List<LineItemRequest> lineItems
) {
    public record LineItemRequest(
            String itemName,
            String hsnSac,
            BigDecimal quantity,
            BigDecimal pricePerUnit,
            BigDecimal gstRate
    ) {}
}