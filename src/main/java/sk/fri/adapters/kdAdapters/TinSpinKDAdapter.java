package sk.fri.adapters.kdAdapters;

import org.tinspin.index.Index.PointEntry;
import org.tinspin.index.Index.PointIterator;
import org.tinspin.index.kdtree.KDTree;
import sk.fri.structures.Point;
import sk.fri.structures.SpatialTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 * Adapts the TinSpin KD tree to the spatial tree interface.
 *
 * @param <T> type of stored values
 * @param <K> type of coordinates
 */
public final class TinSpinKDAdapter<T, K> extends SpatialTree<T, Point<K>> {
    private final KDTree<T> tree;
    private final Function<T, Point<K>> pointExtractor;
    private final ToDoubleFunction<K> coordinateToDouble;

    /**
     * Creates an adapter with coordinate extraction and conversion functions.
     *
     * @param pointExtractor extracts a point from a value
     * @param coordinateToDouble converts a coordinate to a double
     */
    public TinSpinKDAdapter(Function<T, Point<K>> pointExtractor, ToDoubleFunction<K> coordinateToDouble) {
        super("TinSpin KDTree");
        this.tree = KDTree.create(2);
        this.pointExtractor = pointExtractor;
        this.coordinateToDouble = coordinateToDouble;
    }

    @Override
    public boolean insert(T value) {
        tree.insert(toCoordinates(pointExtractor.apply(value)), value);
        return true;
    }

    @Override
    public Optional<List<T>> find(Point<K> point) {
        List<T> values = new ArrayList<>();
        PointIterator<T> iterator = tree.queryExactPoint(toCoordinates(point));
        while (iterator.hasNext()) {
            values.add(iterator.next().value());
        }
        return values.isEmpty() ? Optional.empty() : Optional.of(values);
    }

    /**
     * Finds values in a rectangular range.
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
        List<T> values = new ArrayList<>();
        PointIterator<T> iterator = tree.query(
                new double[]{minX, minY},
                new double[]{maxX, maxY}
        );
        while (iterator.hasNext()) {
            PointEntry<T> entry = iterator.next();
            values.add(entry.value());
        }
        return values;
    }

    @Override
    public Optional<List<T>> rangeSearch(Point<K> min, Point<K> max) {
        double[] minCoordinates = toCoordinates(min);
        double[] maxCoordinates = toCoordinates(max);
        return Optional.of(rangeSearch(
                minCoordinates[0], minCoordinates[1], maxCoordinates[0], maxCoordinates[1]
        ));
    }

    /** Converts a point to the coordinate array expected by TinSpin. */
    private double[] toCoordinates(Point<K> point) {
        return new double[]{
                coordinateToDouble.applyAsDouble(point.x()),
                coordinateToDouble.applyAsDouble(point.y())
        };
    }

    /** Rejects ranges whose minimum exceeds their maximum. */
    private static void validateRange(double minX, double minY, double maxX, double maxY) {
        if (minX > maxX || minY > maxY) {
            throw new IllegalArgumentException("Minimum range coordinates must not exceed maximum coordinates.");
        }
    }
}
