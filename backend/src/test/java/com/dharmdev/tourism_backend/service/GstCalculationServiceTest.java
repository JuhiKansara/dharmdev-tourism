package com.dharmdev.tourism_backend.service;

import com.dharmdev.tourism_backend.entity.Estimate;
import com.dharmdev.tourism_backend.entity.EstimateLineItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GstCalculationServiceTest {

    @Test
    void matchesRealPdfExample() {
        // This is the exact data from your uploaded Dharmdev Tourism PDF:
        // Mahesana to Ahmedabad Airport Drop, qty=1, price=2200, GST=18%,
        // both business and customer in Gujarat (state code 24)

        EstimateLineItem lineItem = EstimateLineItem.builder()
                .itemName("Mahesana to Ahemdabad Airport Drop")
                .hsnSac(null)
                .quantity(BigDecimal.ONE)
                .pricePerUnit(new BigDecimal("2200.00"))
                .gstRate(new BigDecimal("18.00"))
                .build();

        Estimate estimate = Estimate.builder()
                .placeOfSupplyStateCode("24") // same as business -> should trigger CGST+SGST split
                .lineItems(List.of(lineItem))
                .build();

        GstCalculationService service = new GstCalculationService();
        GstCalculationService.EstimateCalculation result = service.calculate(estimate);

        // These are the EXACT numbers printed on your real PDF — if these
        // assertions pass, the calculation logic is proven correct.
        assertEquals(new BigDecimal("2200.00"), result.subTotal());
        assertEquals(new BigDecimal("396.00"), result.totalTax());
        assertEquals(new BigDecimal("2596.00"), result.grandTotal());
        assertEquals("Two Thousand Five Hundred and Ninety Six Rupees only", result.grandTotalInWords());

        var line = result.lineItems().get(0);
        assertEquals(new BigDecimal("198.00"), line.cgstAmt());
        assertEquals(new BigDecimal("198.00"), line.sgstAmt());
    }
}