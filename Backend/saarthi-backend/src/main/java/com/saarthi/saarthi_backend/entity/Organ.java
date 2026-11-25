package com.saarthi.saarthi_backend.entity;

import com.saarthi.saarthi_backend.constants.OrganStatus;
import com.saarthi.saarthi_backend.constants.OrganType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "organs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Organ extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

    @Enumerated(EnumType.STRING)
    @Column(name = "organ_type", nullable = false)
    private OrganType organType;

    @Column(name = "extraction_timestamp", nullable = false)
    private LocalDateTime extractionTimestamp;

    @Column(name = "max_ttl_hours", nullable = false)
    private Integer maxTtlHours; // Heart: 4h, Lungs: 6h, Liver: 12h, Pancreas: 12h, Kidney: 24h

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrganStatus status = OrganStatus.AVAILABLE;

    @Version
    private Long version; // Optimistic locking to prevent double-allocation race conditions
}