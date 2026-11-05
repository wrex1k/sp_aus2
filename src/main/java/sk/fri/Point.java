package sk.fri;

/**
 * Represents a point in a two-dimensional coordinate system.
 *
 * @param <K> type of coordinates
 * @param x x-coordinate of the point
 * @param y y-coordinate of the point
 */
public record Point<K>(K x, K y) {

    /**
     * Creates a point and validates that neither coordinate is null.
     *
     * @throws IllegalArgumentException if x or y is null
     */
    public Point {
        if (x == null) {
            throw new IllegalArgumentException("x cannot be null");
        }
        if (y == null) {
            throw new IllegalArgumentException("y cannot be null");
        }
    }
}