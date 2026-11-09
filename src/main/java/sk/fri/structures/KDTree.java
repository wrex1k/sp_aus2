package sk.fri.structures;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Implementation of a k-d tree for storing and searching objects by their positions.
 *
 * @param <T> type of stored value
 * @param <K> type of coordinates
 */
public class KDTree<T, K> extends SpatialTree<T, KDPoint<K>> {

    private final Function<T, KDPoint<K>> pointExtractor;
    private final Comparator<K> comparator;
    private KDNode<T, K> root;
    private final int dimensions;

    /**
     * Creates an empty k-d tree with k dimensions.
     *
     * @param pointExtractor function that extracts coordinates from an object
     * @param comparator comparator used to compare coordinate values
     * @param dimensions number of dimensions (k) for the k-d tree
     */
    public KDTree(Function<T, KDPoint<K>> pointExtractor, Comparator<K> comparator, int dimensions) {
        super("MyKDTree" + dimensions + "D");
        this.pointExtractor = pointExtractor;
        this.comparator = comparator;
        this.dimensions = dimensions;
    }

    /**
     * Validates that a point has the correct number of dimensions.
     *
     * @param point point to validate
     * @throws IllegalArgumentException if the point is null or has incorrect dimensions
     */
    private void validatePoint(KDPoint<K> point) {
        if (point == null || point.coordinates().size() != dimensions) {
            throw new IllegalArgumentException(
                    "Point must have exactly " + dimensions + " dimensions"
            );
        }
    }

    /**
     * Inserts an object into the tree.
     *
     * @param value object to insert
     * @return true if the object was inserted, false if its position already exists
     */
    @Override
    public boolean insert(T value) {
        KDPoint<K> point = pointExtractor.apply(value);
        validatePoint(point);

        if (root == null) {
            root = new KDNode<>(value, point);
            return true;
        }

        KDNode<T, K> current = root;
        int depth = 0;

        while (true) {
            if (isSame(current.point, point)) {
                current.values.add(value);
                return true;
            }

            if (compare(current.point, point, depth) > 0) {
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

            depth++;
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
    public Optional<List<T>> find(KDPoint<K> point) {
        validatePoint(point);

        KDNode<T, K> current = root;
        int depth = 0;

        while (current != null) {
            if (isSame(current.point, point)) {
                return Optional.of(current.values);
            }

            if (compare(current.point, point, depth) > 0) {
                current = current.left;
            } else {
                current = current.right;
            }

            depth++;
        }

        return Optional.empty();
    }

    /**
     * Searches for all objects whose points lie within the specified range.
     *
     * @param min minimum coordinates of the range
     * @param max maximum coordinates of the range
     * @return an Optional containing all matching objects
     * @throws IllegalArgumentException if either point has incorrect dimensions
     *         or minimum coordinates exceed maximum coordinates
     */
    @Override
    public Optional<List<T>> rangeSearch(KDPoint<K> min, KDPoint<K> max) {
        validateRange(min, max);
        List<T> matches = new ArrayList<>();
        rangeSearch(root, 0, min, max, matches);
        return Optional.of(matches);
    }

    /** Associates a node with its depth for iterative traversal. */
    private record NodeDepth<T, K>(KDNode<T, K> node, int depth) {}

    /**
     * Performs an iterative range search using a stack.
     * Only nodes that may contain points within the specified range are visited.
     *
     * @param node starting node
     * @param depth depth of the starting node
     * @param min minimum coordinates of the range
     * @param max maximum coordinates of the range
     * @param matches list where matching objects are stored
     */
    private void rangeSearch(
            KDNode<T, K> node,
            int depth,
            KDPoint<K> min,
            KDPoint<K> max,
            List<T> matches
    ) {
        Deque<NodeDepth<T, K>> stack = new ArrayDeque<>();

        if (node != null) {
            stack.push(new NodeDepth<>(node, depth));
        }

        while (!stack.isEmpty()) {
            NodeDepth<T, K> entry = stack.pop();
            KDNode<T, K> current = entry.node();
            int currentDepth = entry.depth();

            if (contains(min, max, current.point)) {
                matches.addAll(current.values);
            }

            int dimension = currentDepth % dimensions;
            K split = current.point.getCoordinates(dimension);

            if (comparator.compare(min.getCoordinates(dimension), split) <= 0
                    && current.left != null) {
                stack.push(new NodeDepth<>(current.left, currentDepth + 1));
            }

            if (comparator.compare(max.getCoordinates(dimension), split) >= 0
                    && current.right != null) {
                stack.push(new NodeDepth<>(current.right, currentDepth + 1));
            }
        }
    }

    /**
     * Validates that the minimum coordinates do not exceed the maximum coordinates.
     *
     * @param min minimum coordinates of the range
     * @param max maximum coordinates of the range
     * @throws IllegalArgumentException if any minimum coordinate exceeds its maximum
     */
    private void validateRange(KDPoint<K> min, KDPoint<K> max) {
        for (int i = 0; i < dimensions; i++) {
            if (comparator.compare(min.getCoordinates(i), max.getCoordinates(i)) > 0) {
                throw new IllegalArgumentException("Minimum range coordinates must not exceed maximum coordinates.");
            }
        }
    }

    /**
     * Checks whether a point lies within the specified range in all dimensions.
     *
     * @param min minimum coordinates of the range
     * @param max maximum coordinates of the range
     * @param point point to check
     * @return true if the point lies within the range, false otherwise
     */
    private boolean contains(KDPoint<K> min, KDPoint<K> max, KDPoint<K> point) {
        for (int i = 0; i < dimensions; i++) {
            K coordinate = point.getCoordinates(i);
            if (comparator.compare(coordinate, min.getCoordinates(i)) < 0
                    || comparator.compare(coordinate, max.getCoordinates(i)) > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether two points have identical coordinates.
     *
     * @param currentPoint first point
     * @param point second point
     * @return true if both coordinates are equal
     */
    private boolean isSame(KDPoint<K> currentPoint, KDPoint<K> point) {
        for (int i = 0; i < dimensions; i++) {
            if (comparator.compare(currentPoint.getCoordinates(i), point.getCoordinates(i)) != 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Compares two points using the coordinate selected by the current depth.
     *
     * @param currentPoint first point
     * @param point second point
     * @param depth depth of the current node
     * @return comparison result based on the dimension corresponding to the depth
     */
    private int compare(KDPoint<K> currentPoint, KDPoint<K> point, int depth) {
        return comparator.compare(
                currentPoint.getCoordinates(depth % dimensions),
                point.getCoordinates(depth % dimensions)
        );
    }
}