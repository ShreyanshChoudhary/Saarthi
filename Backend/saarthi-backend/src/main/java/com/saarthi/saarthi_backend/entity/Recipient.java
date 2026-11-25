package com.saarthi.saarthi_backend.entity;

import com.saarthi.saarthi_backend.constants.BloodGroup;
import com.saarthi.saarthi_backend.constants.OrganType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recipients")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Recipient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_ref_code", nullable = false, unique = true)
    private String recipientRefCode; // Anonymized ID (e.g., RCP-PNQ-2026-042)

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "needed_organ", nullable = false)
    private OrganType neededOrgan;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false)
    private BloodGroup bloodGroup;

    @Column(name = "hla_markers", nullable = false)
    private String hlaMarkers;

    @Column(name = "urgency_score", nullable = false)
    private Double urgencyScore; // Normalized clinical urgency (0.0 to 40.0)

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "registered_hospital_id", nullable = false)
    private Hospital registeredHospital;

    @Column(nullable = false)
    @Builder.Default
    private String status = "WAITING"; // WAITING, OFFERED, TRANSPLANTED, INACTIVE
}