package com.saarthi.saarthi_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "match_results")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MatchResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organ_id", nullable = false)
    private Organ organ;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Recipient recipient;

    @Column(name = "rank_position", nullable = false)
    private Integer rankPosition;

    @Column(name = "composite_score", nullable = false)
    private Double compositeScore;

    @Column(name = "hla_match_percentage", nullable = false)
    private Double hlaMatchPercentage;

    @Column(name = "distance_km", nullable = false)
    private Double distanceKm;

    @Column(name = "estimated_transit_hours", nullable = false)
    private Double estimatedTransitHours;
}