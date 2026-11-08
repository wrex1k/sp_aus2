package sk.fri.structures;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * A point-region tree whose leaves contain two-dimensional k-d trees.
 *
 * @param <T> type of stored value
 * @param <K> type of coordinates
 */
public class QuadKDTree<T, K> extends SpatialTree<T, Point<K>> {

    private static final int LEAF_CAPACITY = 32;
    private static final int MAX_DEPTH = 32;

    private final Function<T, Point<K>> pointExtractor;
    private final Function<T, KDPoint<K>> kdPointExtractor;
    private final Comparator<K> comparator;

    private QuadKDNode<T, K> root;

    /**
     * Creates an empty two-dimensional Quad-KD tree.
     *
     * @param pointExtractor function that extracts coordinates from an object
     * @param kdPointExtractor function that extracts coordinates as a KDPoint
     * @param comparator comparator used to compare coordinate values
     */
    public QuadKDTree(
            Function<T, Point<K>> pointExtractor,
            Function<T, KDPoint<K>> kdPointExtractor,
            Comparator<K> comparator
    ) {
        super("MyQuadKDTree");
        this.pointExtractor = pointExtractor;
        this.kdPointExtractor = kdPointExtractor;
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
        KDPoint<K> kdPoint = kdPointExtractor.apply(value);

        validatePoint(point, kdPoint);

        if (root == null) {
            root = newNode(value, point);
            return true;
        }

        QuadKDNode<T, K> current = root;
        int depth = 0;

        while (true) {
            if (current.data != null) {
                boolean exists = current.data.find(kdPoint).isPresent();

                if (exists || current.distinctPoints < LEAF_CAPACITY
                        || depth >= MAX_DEPTH) {

                    current.data.insert(value);
                    current.values.add(value);

                    if (!exists) {
                        current.distinctPoints++;
                    }

                    return true;
                }

                split(current);
            }

            int quadrant = quadrant(point, current.point);
            QuadKDNode<T, K> child = current.children.get(quadrant);

            if (child == null) {
                current.children.set(quadrant, newNode(value, point));
                return true;
            }

            current = child;
            depth++;
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
        if (point == null) {
            throw new IllegalArgumentException("Point cannot be null.");
        }

        QuadKDNode<T, K> current = root;

        while (current != null) {
            if (current.data != null) {
                return current.data.find(toKDPoint(point));
            }

            current = current.children.get(quadrant(point, current.point));
        }

        return Optional.empty();
    }

    /**
     * Searches for all objects whose points lie within the specified range.
     *
     * @param min minimum coordinates of the range
     * @param max maximum coordinates of the range
     * @return an Optional containing all matching objects
     */
    @Override
    public Optional<List<T>> rangeSearch(Point<K> min, Point<K> max) {
        validateRange(min, max);

        List<T> matches = new ArrayList<>();
        Deque<QuadKDNode<T, K>> stack = new ArrayDeque<>();

        if (root != null) {
            stack.push(root);
        }

        KDPoint<K> kdMin = toKDPoint(min);
        KDPoint<K> kdMax = toKDPoint(max);

        while (!stack.isEmpty()) {
            QuadKDNode<T, K> node = stack.pop();

            if (node.data != null) {
                matches.addAll(node.data.rangeSearch(kdMin, kdMax).orElseThrow());
                continue;
            }

            for (int quadrant = 0; quadrant < 4; quadrant++) {
                QuadKDNode<T, K> child = node.children.get(quadrant);

                if (child != null && intersectsQuadrant(node.point, quadrant, min, max)) {
                    stack.push(child);
                }
            }
        }

        return Optional.of(matches);
    }

    /**
     * Creates a new leaf node.
     *
     * @param value object to store
     * @param point coordinates of the object
     * @return a new Quad-KD tree node
     */
    private QuadKDNode<T, K> newNode(T value, Point<K> point) {
        return new QuadKDNode<>(
                value,
                point,
                kdPointExtractor,
                comparator,
                2
        );
    }

    /**
     * Splits a leaf and redistributes its values into child nodes.
     *
     * @param node leaf node to split
     */
    private void split(QuadKDNode<T, K> node) {
        for (T value : node.values) {
            Point<K> point = pointExtractor.apply(value);
            int quadrant = quadrant(point, node.point);

            QuadKDNode<T, K> child = node.children.get(quadrant);

            if (child == null) {
                node.children.set(quadrant, newNode(value, point));
                continue;
            }

            KDPoint<K> kdPoint = kdPointExtractor.apply(value);
            boolean exists = child.data.find(kdPoint).isPresent();

            child.data.insert(value);
            child.values.add(value);

            if (!exists) {
                child.distinctPoints++;
            }
        }

        node.data = null;
        node.values = null;
        node.distinctPoints = 0;
    }

    /**
     * Validates coordinates returned by both extractors.
     *
     * @param point two-dimensional point
     * @param kdPoint k-d tree point
     * @throws IllegalArgumentException if coordinates do not match
     */
    private void validatePoint(Point<K> point, KDPoint<K> kdPoint) {
        if (point == null || kdPoint == null
                || kdPoint.coordinates().size() != 2
                || comparator.compare(point.x(), kdPoint.getCoordinates(0)) != 0
                || comparator.compare(point.y(), kdPoint.getCoordinates(1)) != 0) {

            throw new IllegalArgumentException(
                    "Both extractors must return the same 2D coordinates."
            );
        }
    }

    /**
     * Converts a two-dimensional point to a KDPoint.
     *
     * @param point point to convert
     * @return converted two-dimensional point
     */
    private KDPoint<K> toKDPoint(Point<K> point) {
        return new KDPoint<>(List.of(point.x(), point.y()));
    }

    /**
     * Checks whether a search range intersects a quadrant.
     *
     * @param center reference point used to determine the quadrant
     * @param quadrant quadrant index
     * @param min minimum coordinates of the search range
     * @param max maximum coordinates of the search range
     * @return true if the quadrant intersects the search range
     */
    private boolean intersectsQuadrant(
            Point<K> center,
            int quadrant,
            Point<K> min,
            Point<K> max
    ) {
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

    /**
     * Validates that the minimum coordinates do not exceed the maximum coordinates.
     *
     * @param min minimum coordinates of the range
     * @param max maximum coordinates of the range
     */
    private void validateRange(Point<K> min, Point<K> max) {
        if (min == null || max == null
                || comparator.compare(min.x(), max.x()) > 0
                || comparator.compare(min.y(), max.y()) > 0) {

            throw new IllegalArgumentException("Invalid search range.");
        }
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
            index++;
        }

        if (comparator.compare(point.y(), center.y()) >= 0) {
            index += 2;
        }

        return index;
    }
}