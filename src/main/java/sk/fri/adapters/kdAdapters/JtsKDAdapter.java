package sk.fri.adapters.kdAdapters;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdTree;
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
 * Adapts the JTS KD tree to the spatial tree interface.
 *
 * @param <T> type of stored values
 * @param <K> type of coordinates
 */
public final class JtsKDAdapter<T, K> extends SpatialTree<T, Point<K>> {
    /** Identifies a point after conversion to double coordinates. */
    private record Coordinates(double x, double y) {
    }

    private final KdTree tree = new KdTree();
    private final Map<Coordinates, List<T>> entries = new HashMap<>();
    private final Function<T, Point<K>> pointExtractor;
    private final ToDoubleFunction<K> coordinateToDouble;

    /**
     * Creates an adapter with coordinate extraction and conversion functions.
     *
     * @param pointExtractor extracts a point from a value
     * @param coordinateToDouble converts a coordinate to a double
     */
    public JtsKDAdapter(Function<T, Point<K>> pointExtractor, ToDoubleFunction<K> coordinateToDouble) {
        super("JTS KDTree");
        this.pointExtractor = pointExtractor;
        this.coordinateToDouble = coordinateToDouble;
    }

    @Override
    public boolean insert(T value) {
        Coordinates coordinates = coordinates(pointExtractor.apply(value));
        List<T> values = entries.get(coordinates);
        if (values == null) {
            values = new ArrayList<>();
            entries.put(coordinates, values);
            tree.insert(new Coordinate(coordinates.x(), coordinates.y()), coordinates);
        }
        values.add(value);
        return true;
    }

    @Override
    public Optional<List<T>> find(Point<K> point) {
        List<T> values = entries.get(coordinates(point));
        return values == null ? Optional.empty() : Optional.of(new ArrayList<>(values));
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
        if (minX > maxX || minY > maxY) {
            throw new IllegalArgumentException("Minimum range coordinates must not exceed maximum coordinates.");
        }

        List<T> matches = new ArrayList<>();
        for (Object result : tree.query(new Envelope(minX, maxX, minY, maxY))) {
            if (result instanceof KdNode node) {
                Coordinate coordinate = node.getCoordinate();
                List<T> values = entries.get(new Coordinates(coordinate.x, coordinate.y));
                if (values != null) {
                    matches.addAll(values);
                }
            }
        }
        return matches;
    }

    @Override
    public Optional<List<T>> rangeSearch(Point<K> min, Point<K> max) {
        Coordinates minCoordinates = coordinates(min);
        Coordinates maxCoordinates = coordinates(max);
        return Optional.of(rangeSearch(minCoordinates.x(), minCoordinates.y(), maxCoordinates.x(), maxCoordinates.y()));
    }

    /** Converts a generic point to double coordinates. */
    private Coordinates coordinates(Point<K> point) {
        return new Coordinates(
                coordinateToDouble.applyAsDouble(point.x()),
                coordinateToDouble.applyAsDouble(point.y())
        );
    }
}
