package sk.fri.adapters.quadAdapters;

import com.alwex.tree.QuadRectangle;
import com.alwex.tree.QuadTree;
import sk.fri.structures.Point;
import sk.fri.structures.SpatialTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 * Adapts the Alwex QuadTree to the spatial tree interface.
 *
 * @param <T> type of stored values
 * @param <K> type of coordinates
 */
public final class AlwexQuadTreeAdapter<T, K> extends SpatialTree<T, Point<K>> {
    private static final float POINT_SIZE = 0.000001f;

    /** Identifies a point after conversion to double coordinates. */
    private record Coordinates(double x, double y) {
    }

    /** Groups values at one coordinate for insertion into the QuadTree. */
    private static final class Entry<T> {
        private final double x;
        private final double y;
        private final List<T> values = new ArrayList<>();

        private Entry(double x, double y, T value) {
            this.x = x;
            this.y = y;
            values.add(value);
        }
    }

    private final QuadTree<Entry<T>> tree = new QuadTree<>(
            new QuadRectangle(-90, -180, 180, 360),
            0
    );
    private final Function<T, Point<K>> pointExtractor;
    private final ToDoubleFunction<K> coordinateToDouble;
    private final Map<Coordinates, Entry<T>> entries = new HashMap<>();

    /**
     * Creates an adapter with coordinate extraction and conversion functions.
     *
     * @param pointExtractor extracts a point from a value
     * @param coordinateToDouble converts a coordinate to a double
     */
    public AlwexQuadTreeAdapter(Function<T, Point<K>> pointExtractor, ToDoubleFunction<K> coordinateToDouble) {
        super("Alwex QuadTree");
        this.pointExtractor = pointExtractor;
        this.coordinateToDouble = coordinateToDouble;
    }

    @Override
    public boolean insert(T value) {
        Coordinates coordinates = coordinates(pointExtractor.apply(value));
        Entry<T> existing = entries.get(coordinates);
        if (existing != null) {
            existing.values.add(value);
            return true;
        }

        Entry<T> entry = new Entry<>(coordinates.x(), coordinates.y(), value);
        entries.put(coordinates, entry);
        tree.insert(new QuadRectangle(
                (float) coordinates.x(),
                (float) coordinates.y(),
                POINT_SIZE,
                POINT_SIZE
        ), entry);
        return true;
    }

    @Override
    public Optional<List<T>> find(Point<K> point) {
        Coordinates coordinates = coordinates(point);
        List<T> values = rangeSearch(coordinates.x(), coordinates.y(), coordinates.x(), coordinates.y());
        return values.isEmpty() ? Optional.empty() : Optional.of(values);
    }

    /**
     * Finds values in a rectangular range with inclusive bounds.
     *
     * @param minX minimum x coordinate
     * @param minY minimum y coordinate
     * @param maxX maximum x coordinate
     * @param maxY maximum y coordinate
     * @return matching values
     * @throws IllegalArgumentException if a minimum exceeds its maximum
     */
    public List<T> rangeSearch(double minX, double minY, double maxX, double maxY) {
        validateRange(minX, minY, maxX, maxY);
        List<Entry<T>> candidates = tree.getElements(
                new ArrayList<>(),
                new QuadRectangle(
                        (float) minX,
                        (float) minY,
                        Math.max((float) (maxX - minX), POINT_SIZE),
                        Math.max((float) (maxY - minY), POINT_SIZE)
                )
        );
        List<T> matches = new ArrayList<>();
        for (Entry<T> entry : candidates) {
            if (entry.x >= minX && entry.x <= maxX && entry.y >= minY && entry.y <= maxY) {
                matches.addAll(entry.values);
            }
        }
        return matches;
    }

    @Override
    public Optional<List<T>> rangeSearch(Point<K> min, Point<K> max) {
        Coordinates minCoordinates = coordinates(min);
        Coordinates maxCoordinates = coordinates(max);
        return Optional.of(rangeSearch(
                minCoordinates.x(), minCoordinates.y(), maxCoordinates.x(), maxCoordinates.y()
        ));
    }

    /** Converts a generic point to double coordinates. */
    private Coordinates coordinates(Point<K> point) {
        return new Coordinates(
                coordinateToDouble.applyAsDouble(point.x()),
                coordinateToDouble.applyAsDouble(point.y())
        );
    }

    /** Rejects ranges whose minimum exceeds their maximum. */
    private static void validateRange(double minX, double minY, double maxX, double maxY) {
        if (minX > maxX || minY > maxY) {
            throw new IllegalArgumentException("Minimum range coordinates must not exceed maximum coordinates.");
        }
    }
}
