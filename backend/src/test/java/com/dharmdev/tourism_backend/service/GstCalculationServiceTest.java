package com.dharmdev.tourism_backend.service;

import com.dharmdev.tourism_backend.entity.Customer;
import com.dharmdev.tourism_backend.entity.Estimate;
import com.dharmdev.tourism_backend.entity.EstimateLineItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GstCalculationServiceTest {

    @Test
    void matchesRealPdfExample() {
        Customer customer = Customer.builder()
                .name("Vivekbhai Dipakkumar Kansara")
                .stateCode("24")
                .stateName("Gujarat")
                .build();

        EstimateLineItem lineItem = EstimateLineItem.builder()
                .itemName("Mahesana to Ahemdabad Airport Drop")
                .hsnSac(null)
                .quantity(BigDecimal.ONE)
                .pricePerUnit(new BigDecimal("2200.00"))
                .gstRate(new BigDecimal("18.00"))
                .build();

        Estimate estimate = Estimate.builder()
                .estimateNo(1)
                .estimateDate(LocalDate.of(2026, 8, 23))
                .customer(customer)
                .placeOfSupplyStateCode("24")
                .lineItems(List.of(lineItem))
                .build();

        GstCalculationService service = new GstCalculationService();
        GstCalculationService.EstimateCalculation result = service.calculate(estimate);

        assertEquals(new BigDecimal("2200.00"), result.subTotal());
        assertEquals(new BigDecimal("396.00"), result.totalTax());
        assertEquals(new BigDecimal("2596.00"), result.grandTotal());
        assertEquals("Two Thousand Five Hundred and Ninety Six Rupees only", result.grandTotalInWords());
        assertEquals("Vivekbhai Dipakkumar Kansara", result.customerName());

        var line = result.lineItems().get(0);
        assertEquals(new BigDecimal("198.00"), line.cgstAmt());
        assertEquals(new BigDecimal("198.00"), line.sgstAmt());
    }
}