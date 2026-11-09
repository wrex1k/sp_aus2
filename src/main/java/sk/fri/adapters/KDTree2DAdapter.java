package sk.fri.adapters;

import sk.fri.structures.KDPoint;
import sk.fri.structures.Point;
import sk.fri.structures.KDTree;
import sk.fri.structures.SpatialTree;

import java.util.List;
import java.util.Optional;

/**
 * Adapts a two-dimensional KD tree to the point-based spatial tree interface.
 *
 * @param <T> type of stored values
 * @param <K> type of coordinates
 */
public final class KDTree2DAdapter<T, K> extends SpatialTree<T, Point<K>> {

    private final KDTree<T, K> tree;

    /**
     * Wraps an existing two-dimensional KD tree.
     *
     * @param tree tree to adapt
     */
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

    /** Converts a two-dimensional point to the KD tree's coordinate type. */
    private KDPoint<K> toKDPoint(Point<K> point) {
        return new KDPoint<>(List.of(point.x(), point.y()));
    }
}
