package sk.fri.adapters;

import DataTypes.ITreeData;
import KDtree.KDtree;
import sk.fri.Point;
import sk.fri.SpatialTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public final class KDAdapter<T, K> implements SpatialTree<T, K> {
    private final KDtree<StoredValue<T>, DataTypes.Point> tree =
            new KDtree<StoredValue<T>, DataTypes.Point>(2);
    private final Function<T, Point<K>> pointExtractor;
    private final ToDoubleFunction<K> coordinateToDouble;
    private final List<T> insertedValues = new ArrayList<>();

    public KDAdapter(
            Function<T, Point<K>> pointExtractor,
            ToDoubleFunction<K> coordinateToDouble
    ) {
        this.pointExtractor = pointExtractor;
        this.coordinateToDouble = coordinateToDouble;
    }

    @Override
    public boolean insert(T value) {
        Point<K> point = pointExtractor.apply(value);
        tree.insert(new StoredValue<>(value, toExternalPoint(point)));
        insertedValues.add(value);
        return true;
    }

    @Override
    public Optional<List<T>> find(Point<K> point) {
        List<StoredValue<T>> values = tree.findAllExact(toExternalPoint(point));
        if (values.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(values.stream().map(StoredValue::value).toList());
    }

    public List<T> rangeSearch(double minX, double minY, double maxX, double maxY) {
        if (minX > maxX || minY > maxY) {
            throw new IllegalArgumentException("Minimum range coordinates must not exceed maximum coordinates.");
        }

        List<T> matches = new ArrayList<>();
        for (T value : insertedValues) {
            Point<K> point = pointExtractor.apply(value);
            double x = coordinateToDouble.applyAsDouble(point.x());
            double y = coordinateToDouble.applyAsDouble(point.y());
            if (x >= minX && x <= maxX && y >= minY && y <= maxY) {
                matches.add(value);
            }
        }
        return matches;
    }

    @Override
    public List<T> rangeSearch(Point<K> min, Point<K> max) {
        return rangeSearch(
                coordinateToDouble.applyAsDouble(min.x()),
                coordinateToDouble.applyAsDouble(min.y()),
                coordinateToDouble.applyAsDouble(max.x()),
                coordinateToDouble.applyAsDouble(max.y())
        );
    }

    private DataTypes.Point toExternalPoint(Point<K> point) {
        return new DataTypes.Point(
                coordinateToDouble.applyAsDouble(point.x()),
                coordinateToDouble.applyAsDouble(point.y())
        );
    }

    private static final class StoredValue<T> implements ITreeData<StoredValue<T>, DataTypes.Point> {
        private final T value;
        private final DataTypes.Point point;

        private StoredValue(T value, DataTypes.Point point) {
            this.value = value;
            this.point = point;
        }

        private T value() {
            return value;
        }

        @Override
        public int compareTo(DataTypes.Point key, int dimension) {
            return point.compareTo(key, dimension);
        }

        @Override
        public boolean equals(StoredValue<T> other) {
            return value.equals(other.value);
        }

        @Override
        public boolean keyEqual(DataTypes.Point key) {
            return point.equals(key);
        }

        @Override
        public DataTypes.Point getKey() {
            return point;
        }

        @Override
        public void deleteSelf() {
        }
    }
}
