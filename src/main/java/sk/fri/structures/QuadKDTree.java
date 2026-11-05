
package sk.fri.structures;

import sk.fri.Point;
import sk.fri.SpatialTree;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Implementation of a QuadTree that stores data in k-d trees.
 *
 * @param <T> type of stored value
 * @param <K> type of coordinates
 */
public class QuadKDTree<T, K> implements SpatialTree<T, K> {

    private final Function<T, Point<K>> pointExtractor;
    private final Comparator<K> comparator;
    private QuadKDNode<T, K> root;

    /**
     * Creates an empty Quad-KD tree.
     *
     * @param pointExtractor function that extracts coordinates from an object
     * @param comparator comparator used to compare coordinate values
     */
    public QuadKDTree(Function<T, Point<K>> pointExtractor, Comparator<K> comparator) {
        this.pointExtractor = pointExtractor;
        this.comparator = comparator;
    }

    /**
     * Inserts an object into the tree.
     *
     * @param value object to insert
     * @return true if the object was inserted
     */
    @Override
    public boolean insert(T value) {
        Point<K> point = pointExtractor.apply(value);

        if (root == null) {
            root = new QuadKDNode<>(value, point, pointExtractor, comparator);
            return true;
        }

        QuadKDNode<T, K> current = root;

        while (true) {
            if (isSame(current.point, point)) {
                return current.data.insert(value);
            }

            int q = quadrant(point, current.point);

            if (current.getChildren()[q] == null) {
                current.getChildren()[q] =
                        new QuadKDNode<>(value, point, pointExtractor, comparator);
                return true;
            }

            current = current.getChildren()[q];
        }
    }

    /**
     * Searches for objects stored at the specified point.
     *
     * @param point coordinates of the point to search for
     * @return an Optional containing the objects at the point,
     *         or an empty Optional if the point does not exist
     */
    @Override
    public Optional<List<T>> find(Point<K> point) {
        QuadKDNode<T, K> current = root;

        while (current != null) {
            if (isSame(current.point, point)) {
                return current.data.find(point);
            }

            current = current.getChildren()[quadrant(point, current.point)];
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

    private void rangeSearch(QuadKDNode<T, K> node, Point<K> min, Point<K> max, List<T> matches) {
        if (node == null) {
            return;
        }
        matches.addAll(node.data.rangeSearch(min, max));
        for (int quadrant = 0; quadrant < node.getChildren().length; quadrant++) {
            if (intersectsQuadrant(node.point, quadrant, min, max)) {
                rangeSearch(node.getChildren()[quadrant], min, max, matches);
            }
        }
    }

    private boolean intersectsQuadrant(Point<K> center, int quadrant, Point<K> min, Point<K> max) {
        boolean right = (quadrant & 1) != 0;
        boolean upper = (quadrant & 2) != 0;
        boolean intersectsX = right
                ? comparator.compare(max.x(), center.x()) >= 0
                : comparator.compare(min.x(), center.x()) < 0;
        boolean intersectsY = upper
                ? comparator.compare(max.y(), center.y()) >= 0
                : comparator.compare(min.y(), center.y()) < 0;
        return intersectsX && intersectsY;
    }

    private void validateRange(Point<K> min, Point<K> max) {
        if (comparator.compare(min.x(), max.x()) > 0 || comparator.compare(min.y(), max.y()) > 0) {
            throw new IllegalArgumentException("Minimum range coordinates must not exceed maximum coordinates.");
        }
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
