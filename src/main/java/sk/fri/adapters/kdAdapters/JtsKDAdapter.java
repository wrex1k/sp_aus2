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

public final class JtsKDAdapter<T, K> extends SpatialTree<T, Point<K>> {
    private record Coordinates(double x, double y) {
    }

    private final KdTree tree = new KdTree();
    private final Map<Coordinates, List<T>> entries = new HashMap<>();
    private final Function<T, Point<K>> pointExtractor;
    private final ToDoubleFunction<K> coordinateToDouble;

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

    private Coordinates coordinates(Point<K> point) {
        return new Coordinates(
                coordinateToDouble.applyAsDouble(point.x()),
                coordinateToDouble.applyAsDouble(point.y())
        );
    }
}
