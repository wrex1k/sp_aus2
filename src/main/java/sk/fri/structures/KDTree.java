package sk.fri.structures;

import sk.fri.Point;
import sk.fri.SpatialTree;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Implementation of a k-d tree for storing and searching objects by their positions.
 *
 * @param <T> type of stored value
 * @param <K> type of coordinates
 */
public class KDTree<T, K> implements SpatialTree<T, K> {

    private final Function<T, Point<K>> pointExtractor;
    private final Comparator<K> comparator;
    private KDNode<T, K> root;

    /**
     * Creates an empty k-d tree.
     *
     * @param pointExtractor function that extracts coordinates from an object
     * @param comparator comparator used to compare coordinate values
     */
    public KDTree(Function<T, Point<K>> pointExtractor, Comparator<K> comparator) {
        this.pointExtractor = pointExtractor;
        this.comparator = comparator;
    }

    /**
     * Inserts an object into the tree.
     *
     * @param value object to insert
     * @return true if the object was inserted, false if its position already exists
     */
    @Override
    public boolean insert(T value) {
        Point<K> point = pointExtractor.apply(value);

        if (root == null) {
            root = new KDNode<>(value, point);
            return true;
        }

        KDNode<T, K> current = (KDNode<T, K>) root;
        int level = 0;

        while (true) {
            if (isSame(current.point, point)) {
                current.values.add(value);
                return true;
            }

            if (compare(current.point, point, level) > 0) {
                if (current.left == null) {
                    current.left = new KDNode<>(value, point);
                    return true;
                }

                current = current.left;
            } else {
                if (current.right == null) {
                    current.right = new KDNode<>(value, point);
                    return true;
                }

                current = current.right;
            }

            level++;
        }
    }

    /**
     * Searches for objects stored at the specified point.
     *
     * @param point coordinates of the point to search for
     * @return an Optional containing the list of objects at the point,
     *         or an empty Optional if the point does not exist
     */
    @Override
    public Optional<List<T>> find(Point<K> point) {
        KDNode<T, K> current = (KDNode<T, K>) root;
        int level = 0;

        while (current != null) {
            if (isSame(current.point, point)) {
                return Optional.of(current.values);
            }

            if (compare(current.point, point, level) > 0) {
                current = current.left;
            } else {
                current = current.right;
            }

            level++;
        }

        return Optional.empty();
    }

    @Override
    public List<T> rangeSearch(Point<K> min, Point<K> max) {
        validateRange(min, max);
        List<T> matches = new ArrayList<>();
        rangeSearch(root, 0, min, max, matches);
        return matches;
    }

    private void rangeSearch(
            KDNode<T, K> node,
            int level,
            Point<K> min,
            Point<K> max,
            List<T> matches
    ) {
        if (node == null) {
            return;
        }
        if (contains(min, max, node.point)) {
            matches.addAll(node.values);
        }

        int axis = level % 2;
        K split = axis == 0 ? node.point.x() : node.point.y();
        K minCoordinate = axis == 0 ? min.x() : min.y();
        K maxCoordinate = axis == 0 ? max.x() : max.y();
        if (comparator.compare(minCoordinate, split) <= 0) {
            rangeSearch(node.left, level + 1, min, max, matches);
        }
        if (comparator.compare(maxCoordinate, split) >= 0) {
            rangeSearch(node.right, level + 1, min, max, matches);
        }
    }

    private void validateRange(Point<K> min, Point<K> max) {
        if (comparator.compare(min.x(), max.x()) > 0 || comparator.compare(min.y(), max.y()) > 0) {
            throw new IllegalArgumentException("Minimum range coordinates must not exceed maximum coordinates.");
        }
    }

    private boolean contains(Point<K> min, Point<K> max, Point<K> point) {
        return comparator.compare(point.x(), min.x()) >= 0
                && comparator.compare(point.x(), max.x()) <= 0
                && comparator.compare(point.y(), min.y()) >= 0
                && comparator.compare(point.y(), max.y()) <= 0;
    }

    /**
     * Checks whether two points have identical coordinates.
     *
     * @param currentPoint first point
     * @param point second point
     * @return true if both x and y coordinates are equal
     */
    private boolean isSame(Point<K> currentPoint, Point<K> point) {
        return comparator.compare(currentPoint.x(), point.x()) == 0
                && comparator.compare(currentPoint.y(), point.y()) == 0;
    }

    /**
     * Compares two points using the coordinate selected by the current level.
     *
     * @param currentPoint first point
     * @param point second point
     * @param level level of the current node
     * @return comparison result based on the x-coordinate at even levels
     *         or the y-coordinate at odd levels
     */
    private int compare(Point<K> currentPoint, Point<K> point, int level) {
        if (level % 2 == 0) {
            return comparator.compare(currentPoint.x(), point.x());
        }

        return comparator.compare(currentPoint.y(), point.y());
    }
}