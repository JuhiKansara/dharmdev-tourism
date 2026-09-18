package com.dharmdev.tourism_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** HSN/SAC code — nullable per schema. */
    @Column(name = "hsn_sac")
    private String hsnSac;

    @Column(name = "default_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal defaultPrice;

    /** e.g. 18.00 for 18% GST */
    @Column(name = "default_gst_rate", nullable = false, precision = 4, scale = 2)
    private BigDecimal defaultGstRate;
}
