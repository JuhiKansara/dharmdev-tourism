package com.dharmdev.tourism_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /**
     * 2-digit state code, e.g. "24" for Gujarat.
     * Used to determine CGST/SGST (intra-state) vs IGST (inter-state).
     */
    @Column(name = "state_code", length = 2, nullable = false)
    private String stateCode;

    @Column(name = "state_name", nullable = false)
    private String stateName;
}
