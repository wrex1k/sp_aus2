package sk.fri.poi.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a geographic point of interest.
 */
public record Poi(
        String id,
        String name,
        String description,
        double latitude,
        double longitude,
        String address,
        Instant createdAt,
        Instant updatedAt
) {
    /**
     * Validates a POI and replaces optional null text with empty text.
     *
     * @throws IllegalArgumentException if required values or coordinates are invalid
     */
    public Poi {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID is required.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }
        name = name.trim();
        description = description == null ? "" : description;
        address = address == null ? "" : address;
        validateCoordinates(latitude, longitude);
        if (createdAt == null || updatedAt == null) {
            throw new IllegalArgumentException("Creation and update timestamps are required.");
        }
    }

    /**
     * Creates a new POI with a UUID and current timestamps.
     *
     * @param name required POI name
     * @param description optional description
     * @param latitude latitude in decimal degrees
     * @param longitude longitude in decimal degrees
     * @param address optional address
     * @return the new POI
     * @throws IllegalArgumentException if the name or coordinates are invalid
     */
    public static Poi create(
            String name,
            String description,
            double latitude,
            double longitude,
            String address
    ) {
        Instant now = Instant.now();
        return new Poi(UUID.randomUUID().toString(), name, description, latitude, longitude, address, now, now);
    }

    /**
     * Checks that coordinates are finite and within geographic limits.
     *
     * @param latitude latitude in decimal degrees
     * @param longitude longitude in decimal degrees
     * @throws IllegalArgumentException if either coordinate is invalid
     */
    public static void validateCoordinates(double latitude, double longitude) {
        if (!Double.isFinite(latitude) || latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90.");
        }
        if (!Double.isFinite(longitude) || longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180.");
        }
    }
}
