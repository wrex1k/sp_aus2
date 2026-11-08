package sk.fri.adapters;

import sk.fri.structures.KDPoint;
import sk.fri.structures.Point;
import sk.fri.structures.KDTree;
import sk.fri.structures.SpatialTree;

import java.util.List;
import java.util.Optional;

public final class KDTree2DAdapter<T, K> extends SpatialTree<T, Point<K>> {

    private final KDTree<T, K> tree;

    public KDTree2DAdapter(KDTree<T, K> tree) {
        super(tree.getName());
        this.tree = tree;
    }

    @Override
    public boolean insert(T value) {
        return tree.insert(value);
    }

    @Override
    public Optional<List<T>> find(Point<K> point) {
        return tree.find(toKDPoint(point));
    }

    @Override
    public Optional<List<T>> rangeSearch(Point<K> min, Point<K> max) {
        return tree.rangeSearch(toKDPoint(min), toKDPoint(max));
    }

    private KDPoint<K> toKDPoint(Point<K> point) {
        return new KDPoint<>(List.of(point.x(), point.y()));
    }
}
