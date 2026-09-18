package com.dharmdev.tourism_backend.service;

import com.dharmdev.tourism_backend.entity.Estimate;
import com.dharmdev.tourism_backend.entity.EstimateLineItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class GstCalculationService {

    private static final String BUSINESS_STATE_CODE = "24";

    public enum TaxType { INTRA_STATE, INTER_STATE }

    public record LineItemCalculation(
            EstimateLineItem lineItem,
            BigDecimal taxableAmount,
            TaxType taxType,
            BigDecimal cgstAmt,
            BigDecimal sgstAmt,
            BigDecimal igstAmt,
            BigDecimal totalTax,
            BigDecimal lineAmount
    ) {}

    // Added: estimateId, estimateNo, estimateDate, customerName,
    // placeOfSupplyStateCode, description, termsAndConditions —
    // everything the PDF-matching view needs, in one response.
    public record EstimateCalculation(
            Long estimateId,
            Integer estimateNo,
            LocalDate estimateDate,
            String customerName,
            String placeOfSupplyStateCode,
            String description,
            String termsAndConditions,
            List<LineItemCalculation> lineItems,
            BigDecimal subTotal,
            BigDecimal totalTax,
            BigDecimal grandTotal,
            String grandTotalInWords,
            List<TaxSummaryRow> taxSummary
    ) {}

    public record TaxSummaryRow(
            String hsnSac,
            BigDecimal gstRate,
            BigDecimal taxableAmount,
            BigDecimal cgstAmt,
            BigDecimal sgstAmt,
            BigDecimal igstAmt,
            BigDecimal totalTax
    ) {}

    public EstimateCalculation calculate(Estimate estimate) {
        String customerStateCode = estimate.getPlaceOfSupplyStateCode();

        List<LineItemCalculation> calculations = new ArrayList<>();
        for (EstimateLineItem li : estimate.getLineItems()) {
            calculations.add(calculateLine(li, customerStateCode));
        }

        BigDecimal subTotal = sum(calculations, LineItemCalculation::taxableAmount);
        BigDecimal totalTax = sum(calculations, LineItemCalculation::totalTax);
        BigDecimal grandTotal = subTotal.add(totalTax);

        List<TaxSummaryRow> taxSummary = buildTaxSummary(calculations);

        return new EstimateCalculation(
                estimate.getId(),
                estimate.getEstimateNo(),
                estimate.getEstimateDate(),
                estimate.getCustomer().getName(),
                estimate.getPlaceOfSupplyStateCode(),
                estimate.getDescription(),
                estimate.getTermsAndConditions(),
                calculations,
                subTotal,
                totalTax,
                grandTotal,
                AmountToWordsConverter.convert(grandTotal),
                taxSummary
        );
    }

    private LineItemCalculation calculateLine(EstimateLineItem li, String customerStateCode) {
        BigDecimal taxableAmount = li.getQuantity()
                .multiply(li.getPricePerUnit())
                .setScale(2, RoundingMode.HALF_UP);

        TaxType taxType = BUSINESS_STATE_CODE.equals(customerStateCode)
                ? TaxType.INTRA_STATE
                : TaxType.INTER_STATE;

        BigDecimal cgstAmt = BigDecimal.ZERO;
        BigDecimal sgstAmt = BigDecimal.ZERO;
        BigDecimal igstAmt = BigDecimal.ZERO;

        if (taxType == TaxType.INTRA_STATE) {
            BigDecimal halfRate = li.getGstRate().divide(BigDecimal.valueOf(2));
            cgstAmt = taxableAmount.multiply(halfRate)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            sgstAmt = cgstAmt;
        } else {
            igstAmt = taxableAmount.multiply(li.getGstRate())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        BigDecimal totalTax = cgstAmt.add(sgstAmt).add(igstAmt);
        BigDecimal lineAmount = taxableAmount.add(totalTax);

        return new LineItemCalculation(li, taxableAmount, taxType, cgstAmt, sgstAmt, igstAmt, totalTax, lineAmount);
    }

    private List<TaxSummaryRow> buildTaxSummary(List<LineItemCalculation> calculations) {
        Map<String, List<LineItemCalculation>> grouped = new LinkedHashMap<>();
        for (LineItemCalculation calc : calculations) {
            String key = calc.lineItem().getHsnSac() + "|" + calc.lineItem().getGstRate();
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(calc);
        }

        List<TaxSummaryRow> rows = new ArrayList<>();
        for (List<LineItemCalculation> group : grouped.values()) {
            String hsnSac = group.get(0).lineItem().getHsnSac();
            BigDecimal gstRate = group.get(0).lineItem().getGstRate();
            BigDecimal taxable = sum(group, LineItemCalculation::taxableAmount);
            BigDecimal cgst = sum(group, LineItemCalculation::cgstAmt);
            BigDecimal sgst = sum(group, LineItemCalculation::sgstAmt);
            BigDecimal igst = sum(group, LineItemCalculation::igstAmt);
            BigDecimal totalTax = sum(group, LineItemCalculation::totalTax);
            rows.add(new TaxSummaryRow(hsnSac, gstRate, taxable, cgst, sgst, igst, totalTax));
        }
        return rows;
    }

    private BigDecimal sum(List<LineItemCalculation> list, java.util.function.Function<LineItemCalculation, BigDecimal> extractor) {
        return list.stream().map(extractor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}