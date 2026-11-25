package com.saarthi.saarthi_backend.engine;

import com.saarthi.saarthi_backend.constants.BloodGroup;
import com.saarthi.saarthi_backend.constants.OrganType;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.saarthi.saarthi_backend.constants.OrganType.*;

@Component
public class CompatibilityCalculator {

    public enum AboMatchType {
        INCOMPATIBLE,
        COMPATIBLE,
        IDENTICAL
    }

    /**
     * Evaluates clinical ABO blood group compatibility for solid organ transplantation.
     * Scientifically, solid organ endothelial cells express ABO antigens, NOT Rh(D) antigens.
     * Hence, compatibility depends strictly on the ABO phenotype (O, A, B, AB), regardless of Rh (+/-).
     */
    public AboMatchType evaluateAboCompatibility(BloodGroup donor, BloodGroup recipient) {
        if (donor == null || recipient == null) {
            return AboMatchType.INCOMPATIBLE;
        }

        String donorAbo = extractAboGroup(donor);
        String recipientAbo = extractAboGroup(recipient);

        if (donorAbo.equals(recipientAbo)) {
            return AboMatchType.IDENTICAL;
        }

        boolean isCompatible = switch (donorAbo) {
            case "O" -> true; // Universal solid-organ donor (O -> O, A, B, AB)
            case "A" -> recipientAbo.equals("AB"); // A -> A, AB
            case "B" -> recipientAbo.equals("AB"); // B -> B, AB
            case "AB" -> false; // AB can only donate to AB (already handled by IDENTICAL)
            default -> false;
        };

        return isCompatible ? AboMatchType.COMPATIBLE : AboMatchType.INCOMPATIBLE;
    }

    public boolean isBloodGroupCompatible(BloodGroup donor, BloodGroup recipient) {
        return evaluateAboCompatibility(donor, recipient) != AboMatchType.INCOMPATIBLE;
    }

    /**
     * Computes locus-weighted 6-antigen HLA compatibility (HLA-A, HLA-B, HLA-DR).
     * Expected format: "A2,A24,B35,B44,DR4,DR11" (2 alleles per locus; 1 if homozygous).
     *
     * Scientific Weighting:
     * - HLA-DR (Class II): 50% weight (strongest driver of T-cell mediated graft rejection)
     * - HLA-B  (Class I):  30% weight
     * - HLA-A  (Class I):  20% weight
     */
    public double calculateHlaMatchScore(String donorHla, String recipientHla) {
        if (donorHla == null || donorHla.isBlank() || recipientHla == null || recipientHla.isBlank()) {
            return 0.0;
        }

        Map<String, List<String>> donorLoci = parseHlaByLocus(donorHla);
        Map<String, List<String>> recipientLoci = parseHlaByLocus(recipientHla);

        double drMatchRatio = calculateLocusMatchRatio(donorLoci.get("DR"), recipientLoci.get("DR"));
        double bMatchRatio = calculateLocusMatchRatio(donorLoci.get("B"), recipientLoci.get("B"));
        double aMatchRatio = calculateLocusMatchRatio(donorLoci.get("A"), recipientLoci.get("A"));

        double weightedScore = ((drMatchRatio * 0.50) + (bMatchRatio * 0.30) + (aMatchRatio * 0.20)) * 100.0;
        return Math.round(weightedScore * 100.0) / 100.0;
    }

    /**
     * Calculates the total number of matched antigens out of 6 (0 to 6) for clinical display
     * (e.g., "5/6 HLA Match" or "0-Mismatch / 6/6 Full House Match").
     */
    public int countMatchedAntigensOutOfSix(String donorHla, String recipientHla) {
        if (donorHla == null || recipientHla == null) return 0;

        Map<String, List<String>> donorLoci = parseHlaByLocus(donorHla);
        Map<String, List<String>> recipientLoci = parseHlaByLocus(recipientHla);

        int drMatches = (int) Math.round(calculateLocusMatchRatio(donorLoci.get("DR"), recipientLoci.get("DR")) * 2);
        int bMatches = (int) Math.round(calculateLocusMatchRatio(donorLoci.get("B"), recipientLoci.get("B")) * 2);
        int aMatches = (int) Math.round(calculateLocusMatchRatio(donorLoci.get("A"), recipientLoci.get("A")) * 2);

        return drMatches + bMatches + aMatches;
    }

    /**
     * Returns the immunological importance weight of HLA matching based on organ type.
     * Kidney and Pancreas rely heavily on HLA tissue compatibility; Heart, Liver, and Lungs
     * prioritize urgency and cold-ischemia time over HLA matching.
     */
    public double getOrganHlaImportanceWeight(OrganType organType) {
        return switch (organType) {
            case KIDNEY, PANCREAS -> 0.40;
            case HEART, LUNGS -> 0.15;
            case LIVER -> 0.10;
            case CORNEA -> 0.0; // Avascular tissue; immune-privileged, HLA matching not required
        };
    }

    /**
     * Computes a normalized proximity score (0.0 to 100.0) based on distance vs. maximum corridor limit.
     */
    public double calculateProximityScore(double distanceKm, double maxAllowedDistanceKm) {
        if (distanceKm < 0 || distanceKm >= maxAllowedDistanceKm) {
            return 0.0;
        }
        double score = ((maxAllowedDistanceKm - distanceKm) / maxAllowedDistanceKm) * 100.0;
        return Math.round(score * 100.0) / 100.0;
    }

    private String extractAboGroup(BloodGroup group) {
        // Converts A_POS -> "A", AB_NEG -> "AB", O_POS -> "O"
        return group.name().split("_")[0];
    }

    private Map<String, List<String>> parseHlaByLocus(String hlaString) {
        Map<String, List<String>> lociMap = new HashMap<>();
        lociMap.put("A", new ArrayList<>());
        lociMap.put("B", new ArrayList<>());
        lociMap.put("DR", new ArrayList<>());

        List<String> tokens = Arrays.stream(hlaString.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(s -> !s.isEmpty())
                .toList();

        for (String allele : tokens) {
            if (allele.startsWith("DR")) {
                lociMap.get("DR").add(allele);
            } else if (allele.startsWith("A")) {
                lociMap.get("A").add(allele);
            } else if (allele.startsWith("B")) {
                lociMap.get("B").add(allele);
            }
        }

        // Clinical Homozygosity Rule: If only 1 antigen is typed at a locus (e.g., "A2"),
        // the individual is homozygous at that locus ("A2, A2")
        for (List<String> alleles : lociMap.values()) {
            if (alleles.size() == 1) {
                alleles.add(alleles.get(0));
            }
        }
        return lociMap;
    }

    private double calculateLocusMatchRatio(List<String> donorAlleles, List<String> recipientAlleles) {
        if (donorAlleles == null || donorAlleles.isEmpty() || recipientAlleles == null || recipientAlleles.isEmpty()) {
            return 0.0;
        }

        // Count how many of the donor's 2 antigens at this locus are recognized as "self" by the recipient
        // (Any donor antigen absent in the recipient triggers an immune mismatch)
        List<String> availableRecipientAlleles = new ArrayList<>(recipientAlleles);
        int matches = 0;

        for (String donorAntigen : donorAlleles) {
            if (availableRecipientAlleles.contains(donorAntigen)) {
                matches++;
                availableRecipientAlleles.remove(donorAntigen);
            }
        }

        return matches / 2.0; // Normalized 0.0 (0/2 match), 0.5 (1/2 match), or 1.0 (2/2 match)
    }
}