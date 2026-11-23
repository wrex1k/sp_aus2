package sk.fri.adapters.quadAdapters;

import com.github.ryanp102694.QuadTree;
import com.github.ryanp102694.geometry.AbstractRectangleObject;
import com.github.ryanp102694.geometry.RectangleObject;
import com.github.ryanp102694.geometry.SearchRectangleObject;
import sk.fri.structures.Point;
import sk.fri.structures.SpatialTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 * Adapts the Ryan Pelletier QuadTree to the spatial tree interface.
 *
 * @param <T> type of stored values
 * @param <K> type of coordinates
 */
public final class RyanQuadTreeAdapter<T, K> extends SpatialTree<T, Point<K>> {
    /** Wraps a value as a zero-size rectangle for the external QuadTree. */
    private static final class Entry<T> extends AbstractRectangleObject {
        private final T value;

        private Entry(T value, double x, double y) {
            super(x, y, 0.0, 0.0);
            this.value = value;
            setId(Integer.toHexString(System.identityHashCode(value)) + ":" + x + ":" + y);
        }
    }

    private final QuadTree tree = new QuadTree(10, 10, 0, -90, -180, 180, 360, null);
    private final Function<T, Point<K>> pointExtractor;
    private final ToDoubleFunction<K> coordinateToDouble;

    /**
     * Creates an adapter with coordinate extraction and conversion functions.
     *
     * @param pointExtractor extracts a point from a value
     * @param coordinateToDouble converts a coordinate to a double
     */
    public RyanQuadTreeAdapter(Function<T, Point<K>> pointExtractor, ToDoubleFunction<K> coordinateToDouble) {
        super("Ryan Pelletier QuadTree");
        this.pointExtractor = pointExtractor;
        this.coordinateToDouble = coordinateToDouble;
    }

    @Override
    public boolean insert(T value) {
        Point<K> point = pointExtractor.apply(value);
        Entry<T> entry = new Entry<>(
                value,
                coordinateToDouble.applyAsDouble(point.x()),
                coordinateToDouble.applyAsDouble(point.y())
        );
        tree.insert(entry);
        return true;
    }

    @Override
    public Optional<List<T>> find(Point<K> point) {
        double x = coordinateToDouble.applyAsDouble(point.x());
        double y = coordinateToDouble.applyAsDouble(point.y());
        List<T> values = rangeSearch(x, y, x, y);
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
        List<T> matches = new ArrayList<>();
        RectangleObject search = new SearchRectangleObject(
                minX,
                minY,
                maxX - minX,
                maxY - minY
        );
        for (RectangleObject result : tree.search(search)) {
            Entry<T> entry = (Entry<T>) result;
            if (entry.getX() >= minX && entry.getX() <= maxX
                    && entry.getY() >= minY && entry.getY() <= maxY) {
                matches.add(entry.value);
            }
        }
        return matches;
    }

    @Override
    public Optional<List<T>> rangeSearch(Point<K> min, Point<K> max) {
        double minX = coordinateToDouble.applyAsDouble(min.x());
        double minY = coordinateToDouble.applyAsDouble(min.y());
        double maxX = coordinateToDouble.applyAsDouble(max.x());
        double maxY = coordinateToDouble.applyAsDouble(max.y());
        return Optional.of(rangeSearch(minX, minY, maxX, maxY));
    }

    /** Rejects ranges whose minimum exceeds their maximum. */
    private static void validateRange(double minX, double minY, double maxX, double maxY) {
        if (minX > maxX || minY > maxY) {
            throw new IllegalArgumentException("Minimum range coordinates must not exceed maximum coordinates.");
        }
    }
}
