package sk.fri.structures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Base class for spatial tree nodes.
 *
 * @param <P> type of the node's point
 */
abstract class Node<P> {
    final P point;

    Node(P point) {
        this.point = point;
    }
}

/**
 * Node of a k-d tree, stores one object with its position
 * and references to both (left, right) children.
 *
 * @param <T> type of stored value
 * @param <K> type of coordinates
 */
final class KDNode<T, K> extends Node<KDPoint<K>> {
    KDNode<T, K> left;
    KDNode<T, K> right;
    final List<T> values = new ArrayList<>();

    KDNode(T value, KDPoint<K> point) {
        super(point);
        values.add(value);
    }
}

/**
 * Quadtree cell with bounds, stored points and optional quadrant children.
 *
 * @param <T> type of stored value
 * @param <K> type of coordinates
 */
final class QuadNode<T, K> {

    final Point<K> min;
    final Point<K> max;
    final Point<K> center;
    final List<T> values = new ArrayList<>();
    List<QuadNode<T, K>> children;

    QuadNode(Point<K> min, Point<K> max, Point<K> center) {
        this.min = min;
        this.max = max;
        this.center = center;
    }
}

/**
 * Node of a QuadTree whose leaves store values in a k-d tree.
 */
final class QuadKDNode<T, K> extends Node<Point<K>> {

    KDTree<T, K> data;
    List<T> values;
    int distinctPoints;

    final List<QuadKDNode<T, K>> children =
            new ArrayList<>(Collections.nCopies(4, null));

    QuadKDNode(
            T value,
            Point<K> point,
            Function<T, KDPoint<K>> pointExtractor,
            Comparator<K> comparator,
            int dimensions
    ) {
        super(point);

        data = new KDTree<>(pointExtractor, comparator, dimensions);
        data.insert(value);

        values = new ArrayList<>();
        values.add(value);

        distinctPoints = 1;
    }
}