package sk.fri.structures;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
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
    private static final int MAX_DEPTH = 32;

    private final Function<T, Point<K>> pointExtractor;
    private final Comparator<K> comparator;
    private final BiFunction<K, K, K> midpoint;
    private final QuadNode<T, K> root;

    /**
     * Creates an empty QuadTree.
     *
     * @param pointExtractor function that extracts coordinates from an object
     * @param comparator comparator used to compare coordinate values
     * @param min minimum coordinates of the root region
     * @param max maximum coordinates of the root region
     * @param midpoint computes the midpoint between two coordinate values
     */
    public QuadTree(Function<T, Point<K>> pointExtractor, Comparator<K> comparator, Point<K> min, Point<K> max, BiFunction<K, K, K> midpoint
    ) {
        super("MyQuadTree");
        if (comparator.compare(min.x(), max.x()) > 0
                || comparator.compare(min.y(), max.y()) > 0) {
            throw new IllegalArgumentException("Invalid quadtree configuration.");
        }

        this.pointExtractor = pointExtractor;
        this.comparator = comparator;
        this.midpoint = midpoint;
        Point<K> center = center(min, max);
        root = new QuadNode<>(min, max, center);
    }

    /**
     * Inserts an object into the tree.
     * If an object with the same coordinates already exists,
     * the new object is added to that node's list of values.
     *
     * @param value object to insert
     * @return true if the object was inserted, or false if it is outside the root region
     */
    @Override
    public boolean insert(T value) {
        Point<K> point = pointExtractor.apply(value);
        if (!contains(root.min, root.max, point)) {
            return false;
        }

        QuadNode<T, K> current = root;
        int depth = 0;
        while (true) {
            if (current.children == null) {
                if (current.values.isEmpty() || depth >= MAX_DEPTH || !canSplit(current)) {
                    current.values.add(value);
                    return true;
                }

                if (isSame(pointExtractor.apply(current.values.getFirst()), point)) {
                    current.values.add(value);
                    return true;
                }

                split(current);
            }

            current = current.children.get(quadrant(current.center, point));
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
        if (!contains(root.min, root.max, point)) {
            return Optional.empty();
        }

        QuadNode<T, K> node = root;
        while (node.children != null) {
            node = node.children.get(quadrant(node.center, point));
        }
        List<T> matches = new ArrayList<>();
        for (T value : node.values) {
            if (isSame(pointExtractor.apply(value), point)) {
                matches.add(value);
            }
        }
        return matches.isEmpty() ? Optional.empty() : Optional.of(matches);
    }

    /**
     * Searches for all objects within the specified rectangular range.
     * Uses an iterative traversal with a stack.
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
            QuadNode<T, K> node = stack.pop();
            if (!intersects(node, min, max)) {
                continue;
            }
            if (node.children == null) {
                for (T value : node.values) {
                    if (contains(min, max, pointExtractor.apply(value))) {
                        matches.add(value);
                    }
                }
            } else {
                for (QuadNode<T, K> child : node.children) {
                    stack.push(child);
                }
            }
        }
        return Optional.of(matches);
    }

    private void split(QuadNode<T, K> node) {
        Point<K> bottomCenter = new Point<>(node.center.x(), node.min.y());
        Point<K> leftCenter = new Point<>(node.min.x(), node.center.y());
        Point<K> rightCenter = new Point<>(node.max.x(), node.center.y());
        Point<K> topCenter = new Point<>(node.center.x(), node.max.y());

        // South-west, south-east, north-west, north-east.
        node.children = List.of(
                newNode(node.min, node.center),
                newNode(bottomCenter, rightCenter),
                newNode(leftCenter, topCenter),
                newNode(node.center, node.max)
        );

        for (T value : node.values) {
            Point<K> point = pointExtractor.apply(value);
            node.children.get(quadrant(node.center, point)).values.add(value);
        }
        node.values.clear();
    }

    private QuadNode<T, K> newNode(Point<K> min, Point<K> max) {
        return new QuadNode<>(min, max, center(min, max));
    }

    private Point<K> center(Point<K> min, Point<K> max) {
        return new Point<>(
                midpoint.apply(min.x(), max.x()),
                midpoint.apply(min.y(), max.y())
        );
    }

    private boolean canSplit(QuadNode<T, K> node) {
        return comparator.compare(node.center.x(), node.min.x()) > 0
                && comparator.compare(node.center.x(), node.max.x()) < 0
                && comparator.compare(node.center.y(), node.min.y()) > 0
                && comparator.compare(node.center.y(), node.max.y()) < 0;
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

    private boolean intersects(QuadNode<T, K> node, Point<K> min, Point<K> max) {
        return comparator.compare(max.x(), node.min.x()) >= 0
                && comparator.compare(min.x(), node.max.x()) <= 0
                && comparator.compare(max.y(), node.min.y()) >= 0
                && comparator.compare(min.y(), node.max.y()) <= 0;
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

    /**
     * Validates that the minimum coordinates do not exceed the maximum coordinates.
     *
     * @param min minimum coordinates of the search range
     * @param max maximum coordinates of the search range
     * @throws IllegalArgumentException if a bound is null or any minimum coordinate exceeds its maximum
     */
    private void validateRange(Point<K> min, Point<K> max) {
        if (min == null || max == null
                || comparator.compare(min.x(), max.x()) > 0
                || comparator.compare(min.y(), max.y()) > 0) {
            throw new IllegalArgumentException("Invalid search range.");
        }
    }
}