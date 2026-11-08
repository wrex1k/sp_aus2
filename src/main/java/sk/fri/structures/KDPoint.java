package sk.fri.structures;

import java.util.List;

/**
 * Represents a point in a k-dimensional coordinate system.
 *
 * @param <K> type of coordinates
 * @param coordinates list of coordinates of the point
 */
public record KDPoint<K>(List<K> coordinates) {

    /**
     * Creates a point and validates that the coordinate list and its elements are not null.
     *
     * @throws IllegalArgumentException if coordinates or any coordinate is null
     */
    public KDPoint {
        if (coordinates == null) {
            throw new IllegalArgumentException("coordinates cannot be null");
        }
        for (int index = 0; index < coordinates.size(); index++) {
            if (coordinates.get(index) == null) {
                throw new IllegalArgumentException(
                        "coordinate at index " + index + " cannot be null"
                );
            }
        }
        coordinates = List.copyOf(coordinates);
    }

    /**
     * Returns the coordinate at the specified dimension.
     *
     * @throws IllegalArgumentException if dimension is outside the coordinate list
     * @return the coordinate at the specified dimension index
     */
    public K getCoordinates(int dimension) {
        if (dimension < 0 || dimension >= coordinates.size()) {
            throw new IllegalArgumentException(
                    "dimension must be between 0 and " + (coordinates.size() - 1)
            );
        }
        return coordinates.get(dimension);
    }
}