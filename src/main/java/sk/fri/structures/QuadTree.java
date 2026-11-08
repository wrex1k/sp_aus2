package sk.fri.structures;

import java.util.*;
import java.util.function.Function;

/**
 * QuadTree implementation of a spatial tree.
 * Stores objects according to their two-dimensional coordinates
 * and supports inserting objects and searching for exact points.
 *
 * @param <T> type of stored value, for example: Person
 * @param <K> type of coordinates, for example: Double
 */
public class QuadTree<T, K> extends SpatialTree<T, Point<K>> {
    private final Function<T, Point<K>> pointExtractor;
    private final Comparator<K> comparator;
    private QuadNode<T, K> root;

    private static final int MAX_DEPTH = 32;

    /**
     * Creates an empty QuadTree.
     *
     * @param pointExtractor function that extracts coordinates from an object
     * @param comparator comparator used to compare coordinate values
     */
    public QuadTree(Function<T, Point<K>> pointExtractor, Comparator<K> comparator) {
        super("MyQuadTree");
        this.pointExtractor = pointExtractor;
        this.comparator = comparator;
    }

    /**
     * Inserts an object into the tree.
     * If an object with the same coordinates already exists,
     * the new object is added to that node's list of values.
     *
     * @param value object to insert
     * @return true if the object was inserted, or false if the maximum depth was reached
     */
    @Override
    public boolean insert(T value) {
        Point<K> point = pointExtractor.apply(value);

        if (root == null) {
            root = new QuadNode<>(value, point);
            return true;
        }

        QuadNode<T, K> current = root;
        int depth = 0;
        while (true) {
            if (isSame(current.point, point)) {
                current.values.add(value);
                return true;
            }

            if (depth >= MAX_DEPTH) {
                return false;
            }

            int q = quadrant(current.point, point);

            if (current.children.get(q) == null) {
                current.children.set(q, new QuadNode<>(value, point));
                return true;
            }

            current = current.children.get(q);
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
    public Optional<List<T>> find(Point<K> point) {
        QuadNode<T, K> current = root;

        while (current != null) {
            if (isSame(current.point, point)) {
                return Optional.of(current.values);
            }

            current = current.children.get(quadrant(current.point, point));
        }

        return Optional.empty();
    }

    /**
     * Searches for all objects within the specified rectangular range.
     * Uses an iterative traversal with a stack to avoid recursion.
     *
     * @param min minimum coordinates of the search range
     * @param max maximum coordinates of the search range
     * @return an Optional containing all matching objects
     */
    @Override
    public Optional<List<T>> rangeSearch(Point<K> min, Point<K> max) {
        validateRange(min, max);

        List<T> matches = new ArrayList<>();
        Deque<QuadNode<T, K>> stack = new ArrayDeque<>();

        if (root != null) {
            stack.push(root);
        }

        while (!stack.isEmpty()) {
            QuadNode<T, K> current = stack.pop();

            if (contains(min, max, current.point)) {
                matches.addAll(current.values);
            }

            for (int quadrant = 0; quadrant < current.children.size(); quadrant++) {
                QuadNode<T, K> child = current.children.get(quadrant);

                if (child != null && intersectsQuadrant(current.point, quadrant, min, max)) {
                    stack.push(child);
                }
            }
        }

        return Optional.of(matches);
    }

    /**
     * Checks whether the specified search range intersects a quadrant.
     *
     * @param center center point of the current node
     * @param quadrant quadrant index from 0 to 3
     * @param min minimum coordinates of the search range
     * @param max maximum coordinates of the search range
     * @return true if the search range intersects the quadrant
     */
    private boolean intersectsQuadrant(Point<K> center, int quadrant, Point<K> min, Point<K> max) {
        boolean intersectsX = (quadrant & 1) == 0
                ? comparator.compare(min.x(), center.x()) < 0
                : comparator.compare(max.x(), center.x()) >= 0;

        boolean intersectsY = (quadrant & 2) == 0
                ? comparator.compare(min.y(), center.y()) < 0
                : comparator.compare(max.y(), center.y()) >= 0;

        return intersectsX && intersectsY;
    }

    /**
     * Validates that the minimum coordinates do not exceed the maximum coordinates.
     *
     * @param min minimum coordinates of the search range
     * @param max maximum coordinates of the search range
     * @throws IllegalArgumentException if any minimum coordinate exceeds its maximum
     */
    private void validateRange(Point<K> min, Point<K> max) {
        if (comparator.compare(min.x(), max.x()) > 0 || comparator.compare(min.y(), max.y()) > 0) {
            throw new IllegalArgumentException("Minimum range coordinates must not exceed maximum coordinates.");
        }
    }

    /**
     * Checks whether a point is inside the specified rectangular range.
     * The range boundaries are inclusive.
     *
     * @param min minimum coordinates of the search range
     * @param max maximum coordinates of the search range
     * @param point point to check
     * @return true if the point is inside or on the range boundaries
     */
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
    private int quadrant(Point<K> center, Point<K> point) {
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