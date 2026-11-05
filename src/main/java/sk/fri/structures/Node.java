package sk.fri.structures;

import sk.fri.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Abstract class of node for Spatial Tree.
 *
 * @param <T> type of stored value, for example: Car
 * @param <K> type of coordinates, for example: Double x, Double y
 */
public abstract class Node<T, K> {
    final T value;
    final Point<K> point;

    Node(T value, Point<K> point) {
        this.value = value;
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
final class KDNode<T, K> extends Node<T, K> {
    KDNode<T, K> left;
    KDNode<T, K> right;
    final List<T> values = new ArrayList<>();

    KDNode(T value, Point<K> point) {
        super(value, point);
        values.add(value);
    }
}

/**
 * Node of a QuadTree, stores one object with its position
 * and references to four (north-west, north-east, south-west, south-east) children.
 *
 * @param <T> type of stored value
 * @param <K> type of coordinates
 */
class QuadNode<T, K> extends Node<T, K> {

    final List<T> values = new ArrayList<>();
    final QuadNode<T, K>[] children = new QuadNode[4];

    QuadNode(T value, Point<K> point) {
        super(value, point);
        values.add(value);
    }

    /**
     * Return an array of childrens.
     *
     * @return array of childrens, where empty positions contain null
     */
    public QuadNode<T, K>[] getChildren() {
        return children;
    }
}

/**
 * Node of a QuadTree that stores a k-d tree instead of one object
 * and references to four (north-west, north-east, south-west, south-east) children.
 *
 * @param <T> type of stored value
 * @param <K> type of coordinates
 */
final class QuadKDNode<T, K> extends Node<T, K> {

    final KDTree<T, K> data;
    final QuadKDNode<T, K>[] children = new QuadKDNode[4];

    QuadKDNode(T value, Point<K> point,
               Function<T, Point<K>> pointExtractor,
               Comparator<K> comparator) {
        super(value, point);
        this.data = new KDTree<>(pointExtractor, comparator);
        this.data.insert(value);
    }

    /**
     * Returns the array of child nodes.
     *
     * @return array of children, where empty positions contain null
     */
    public QuadKDNode<T, K>[] getChildren() {
        return children;
    }
}