package com.saarthi.saarthi_backend.engine;

import org.springframework.stereotype.Service;

@Service
public class GeoLocationService {

    // India geographic bounding box
    private static final double INDIA_MIN_LAT = 6.5;
    private static final double INDIA_MAX_LAT = 37.5;
    private static final double INDIA_MIN_LON = 68.0;
    private static final double INDIA_MAX_LON = 97.5;

    // Kilometer conversion factors calibrated for India's latitude band (~22° N)
    private static final double KM_PER_DEGREE_LAT = 111.132;
    private static final double KM_PER_DEGREE_LON_INDIA = 103.05;

    // Typical Indian medical transit speeds (km/h)
    public static final double GREEN_CORRIDOR_ROAD_SPEED_KMH = 55.0;
    public static final double DOMESTIC_AIR_CORRIDOR_SPEED_KMH = 550.0;

    /**
     * Calculates the straight-line distance in kilometers between two Indian hospitals
     * using a regional planar projection calibrated for India's coordinates.
     *
     * @param lat1 Latitude of source hospital in India (6.5 to 37.5)
     * @param lon1 Longitude of source hospital in India (68.0 to 97.5)
     * @param lat2 Latitude of destination hospital in India (6.5 to 37.5)
     * @param lon2 Longitude of destination hospital in India (68.0 to 97.5)
     * @return Distance in kilometers, rounded to 2 decimal places
     */
    public double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        validateIndiaCoordinates(lat1, lon1);
        validateIndiaCoordinates(lat2, lon2);

        double deltaYKm = (lat2 - lat1) * KM_PER_DEGREE_LAT;
        double deltaXKm = (lon2 - lon1) * KM_PER_DEGREE_LON_INDIA;

        double distance = Math.hypot(deltaXKm, deltaYKm);
        return Math.round(distance * 100.0) / 100.0;
    }

    /**
     * Estimates organ transit duration in hours across Indian medical corridors.
     * Automatically switches from road green-corridor speed to domestic air speed
     * for inter-city distances exceeding 250 km.
     *
     * @param distanceKm Distance between hospitals in kilometers
     * @return Estimated travel duration in hours
     */
    public double estimateIndiaTransitHours(double distanceKm) {
        if (distanceKm < 0) {
            throw new IllegalArgumentException("Distance cannot be negative.");
        }
        double speedKmh = (distanceKm <= 250.0)
                ? GREEN_CORRIDOR_ROAD_SPEED_KMH
                : DOMESTIC_AIR_CORRIDOR_SPEED_KMH;

        // Add 0.75 hours (45 mins) buffer for airport/hospital handover if air transport is used
        double transitHours = (distanceKm / speedKmh) + (distanceKm > 250.0 ? 0.75 : 0.0);
        return Math.round(transitHours * 100.0) / 100.0;
    }

    private void validateIndiaCoordinates(double lat, double lon) {
        if (lat < INDIA_MIN_LAT || lat > INDIA_MAX_LAT) {
            throw new IllegalArgumentException(
                    "Latitude " + lat + " is outside India's operational bounds (" + INDIA_MIN_LAT + "°N to " + INDIA_MAX_LAT + "°N)."
            );
        }
        if (lon < INDIA_MIN_LON || lon > INDIA_MAX_LON) {
            throw new IllegalArgumentException(
                    "Longitude " + lon + " is outside India's operational bounds (" + INDIA_MIN_LON + "°E to " + INDIA_MAX_LON + "°E)."
            );
        }
    }
}