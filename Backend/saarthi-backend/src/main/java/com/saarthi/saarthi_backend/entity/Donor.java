package com.saarthi.saarthi_backend.entity;

import com.saarthi.saarthi_backend.constants.BloodGroup;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "donors")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Donor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "donor_ref_code", nullable = false, unique = true)
    private String donorRefCode; // Anonymized ID (e.g., DNR-MH-2026-001)

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false)
    private BloodGroup bloodGroup;

    @Column(name = "hla_markers", nullable = false)
    private String hlaMarkers; // Format: "A2,A24,B35,B44,DR4,DR11"

    @Column(name = "serology_cleared", nullable = false)
    private boolean serologyCleared; // HIV, HBV, HCV negative clearance

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital sourceHospital;

    @OneToMany(mappedBy = "donor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Organ> organs = new ArrayList<>();
}