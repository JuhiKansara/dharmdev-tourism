package com.dharmdev.tourism_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "estimates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estimate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Sequential estimate number per business (distinct from the PK). */
    @Column(name = "estimate_no", nullable = false)
    private Integer estimateNo;

    @Column(name = "estimate_date", nullable = false)
    private LocalDate estimateDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * State code of the place of supply — used together with the customer's
     * state code to decide CGST/SGST vs IGST.
     */
    @Column(name = "place_of_supply_state_code", length = 2, nullable = false)
    private String placeOfSupplyStateCode;

    /** Optional free-text note, e.g. "International Airport". */
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;

    /** DRAFT or FINALIZED */
    @Column(nullable = false)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "estimate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EstimateLineItem> lineItems = new ArrayList<>();
}
