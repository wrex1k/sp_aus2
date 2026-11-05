package sk.fri.structures;

import sk.fri.Point;
import sk.fri.SpatialTree;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * QuadTree implementation of a spatial tree.
 * Stores objects according to their two-dimensional coordinates
 * and supports inserting objects and searching for exact points.
 *
 * @param <T> type of stored value, for example: Person
 * @param <K> type of coordinates, for example: Double
 */
public class QuadTree<T, K> implements SpatialTree<T, K> {
    private final Function<T, Point<K>> pointExtractor;
    private final Comparator<K> comparator;
    private QuadNode<T, K> root;

    /**
     * Creates an empty QuadTree.
     *
     * @param pointExtractor function that extracts coordinates from an object
     * @param comparator comparator used to compare coordinate values
     */
    public QuadTree(Function<T, Point<K>> pointExtractor, Comparator<K> comparator) {
        this.pointExtractor = pointExtractor;
        this.comparator = comparator;
    }

    /**
     * Inserts an object into the tree.
     * If an object with the same coordinates already exists,
     * the new object is added to that node's list of values.
     *
     * @param value object to insert
     * @return true if the object was inserted
     */
    @Override
    public boolean insert(T value) {
        Point<K> point = pointExtractor.apply(value);

        if (root == null) {
            root = new QuadNode<>(value, point);
            return true;
        }

        QuadNode<T, K> current = (QuadNode<T, K>) root;

        while (true) {
            if (isSame(current.point, point)) {
                current.values.add(value);
                return true;
            }

            int q = quadrant(current.point, point);

            if (current.getChildren()[q] == null) {
                current.getChildren()[q] = new QuadNode<>(value, point);
                return true;
            }

            current = current.getChildren()[q];
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
        QuadNode<T, K> current = (QuadNode<T, K>) root;

        while (current != null) {
            if (isSame(current.point, point)) {
                return Optional.of(current.values);
            }

            current = current.getChildren()[quadrant(current.point, point)];
        }

        return Optional.empty();
    }

    @Override
    public List<T> rangeSearch(Point<K> min, Point<K> max) {
        validateRange(min, max);
        List<T> matches = new ArrayList<>();
        rangeSearch(root, min, max, matches);
        return matches;
    }

    private void rangeSearch(QuadNode<T, K> node, Point<K> min, Point<K> max, List<T> matches) {
        if (node == null) {
            return;
        }
        if (contains(min, max, node.point)) {
            matches.addAll(node.values);
        }
        for (int quadrant = 0; quadrant < node.getChildren().length; quadrant++) {
            if (intersectsQuadrant(node.point, quadrant, min, max)) {
                rangeSearch(node.getChildren()[quadrant], min, max, matches);
            }
        }
    }

    private boolean intersectsQuadrant(Point<K> center, int quadrant, Point<K> min, Point<K> max) {
        boolean greaterX = (quadrant & 1) == 0;
        boolean greaterY = (quadrant & 2) == 0;
        boolean intersectsX = greaterX
                ? comparator.compare(max.x(), center.x()) > 0
                : comparator.compare(min.x(), center.x()) <= 0;
        boolean intersectsY = greaterY
                ? comparator.compare(max.y(), center.y()) > 0
                : comparator.compare(min.y(), center.y()) <= 0;
        return intersectsX && intersectsY;
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
     * Determines which child quadrant contains the specified point
     * relative to the center point.
     *
     * @param point point whose quadrant is being determined
     * @param center reference point used to determine the quadrant
     * @return quadrant index from 0 to 3
     */
    private int quadrant(Point<K> point, Point<K> center) {
        int index = 0;

        if (comparator.compare(point.x(), center.x()) >= 0) {
            index += 1;
        }

        if (comparator.compare(point.y(), center.y()) >= 0) {
            index += 2;
        }

        return index;
    }
}