package sk.fri.adapters;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdTree;
import sk.fri.Point;
import sk.fri.SpatialTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public final class JtsKDAdapter<T, K> implements SpatialTree<T, K> {
    private record Coordinates(double x, double y) {
    }

    private final KdTree tree = new KdTree();
    private final Map<Coordinates, List<T>> entries = new HashMap<>();
    private final Function<T, Point<K>> pointExtractor;
    private final ToDoubleFunction<K> coordinateToDouble;

    public JtsKDAdapter(Function<T, Point<K>> pointExtractor, ToDoubleFunction<K> coordinateToDouble) {
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
        Coordinates target = coordinates(point);
        for (Object result : tree.query(new Envelope(target.x(), target.x(), target.y(), target.y()))) {
            if (result instanceof KdNode node) {
                Coordinate coordinate = node.getCoordinate();
                if (coordinate.x == target.x() && coordinate.y == target.y()) {
                    List<T> values = entries.get(target);
                    if (values != null) {
                        return Optional.of(new ArrayList<>(values));
                    }
                }
            }
        }
        return Optional.empty();
    }

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
    public List<T> rangeSearch(Point<K> min, Point<K> max) {
        Coordinates minCoordinates = coordinates(min);
        Coordinates maxCoordinates = coordinates(max);
        return rangeSearch(minCoordinates.x(), minCoordinates.y(), maxCoordinates.x(), maxCoordinates.y());
    }

    private Coordinates coordinates(Point<K> point) {
        return new Coordinates(
                coordinateToDouble.applyAsDouble(point.x()),
                coordinateToDouble.applyAsDouble(point.y())
        );
    }
}
