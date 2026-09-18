package com.dharmdev.tourism_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "estimate_line_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstimateLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estimate_id", nullable = false)
    @JsonIgnore
    private Estimate estimate;

    /**
     * Item name copied at entry time — intentionally not a FK to the items
     * table so edits to the catalog don't affect historical line items.
     */
    @Column(name = "item_name", nullable = false)
    private String itemName;

    /** HSN/SAC code — nullable per schema. */
    @Column(name = "hsn_sac")
    private String hsnSac;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "price_per_unit", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerUnit;

    /** e.g. 18.00 for 18% GST. taxable_amount/gst_amount/line_total are NOT stored — computed on read. */
    @Column(name = "gst_rate", nullable = false, precision = 4, scale = 2)
    private BigDecimal gstRate;
}
