package sk.fri.adapters.quadAdapters;

import com.github.varunpant.quadtree.QuadTree;
import sk.fri.structures.Point;
import sk.fri.structures.SpatialTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public final class VarunPantQuadTreeAdapter<T, K> extends SpatialTree<T, Point<K>> {
    private record Coordinates(double x, double y) {
    }

    private final QuadTree<List<T>> tree = new QuadTree<>(-90, -180, 90, 180);
    private final Function<T, Point<K>> pointExtractor;
    private final ToDoubleFunction<K> coordinateToDouble;
    private final Map<Coordinates, List<T>> valuesByPoint = new HashMap<>();

    public VarunPantQuadTreeAdapter(
            Function<T, Point<K>> pointExtractor,
            ToDoubleFunction<K> coordinateToDouble
    ) {
        super("Varun Pant QuadTree");
        this.pointExtractor = pointExtractor;
        this.coordinateToDouble = coordinateToDouble;
    }

    @Override
    public boolean insert(T value) {
        Coordinates coordinates = coordinates(pointExtractor.apply(value));
        List<T> values = valuesByPoint.get(coordinates);
        if (values == null) {
            values = new ArrayList<>();
            valuesByPoint.put(coordinates, values);
            tree.set(coordinates.x(), coordinates.y(), values);
        }
        values.add(value);
        return true;
    }

    @Override
    public Optional<List<T>> find(Point<K> point) {
        Coordinates coordinates = coordinates(point);
        List<T> values = tree.get(coordinates.x(), coordinates.y(), null);
        return values == null ? Optional.empty() : Optional.of(new ArrayList<>(values));
    }

    public List<T> rangeSearch(double minX, double minY, double maxX, double maxY) {
        validateRange(minX, minY, maxX, maxY);
        List<T> matches = new ArrayList<>();
        for (com.github.varunpant.quadtree.Point<List<T>> point :
                tree.searchIntersect(minX, minY, maxX, maxY)) {
            matches.addAll(point.getValue());
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

    private Coordinates coordinates(Point<K> point) {
        return new Coordinates(
                coordinateToDouble.applyAsDouble(point.x()),
                coordinateToDouble.applyAsDouble(point.y())
        );
    }

    private static void validateRange(double minX, double minY, double maxX, double maxY) {
        if (minX > maxX || minY > maxY) {
            throw new IllegalArgumentException("Minimum range coordinates must not exceed maximum coordinates.");
        }
    }
}
