package sk.fri;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

public class KDTree<T, K> implements SpatialTree<T, K> {

    private static class Node<T, K> {
        private final T value;
        private final Point<K> point;
        private Node<T, K> left;
        private Node<T, K> right;

        private Node(T value, Point<K> point) {
            this.value = value;
            this.point = point;
        }
    }

    private final Function<T, Point<K>> keyExtractor;
    private final Comparator<K> comparator;
    private Node<T, K> root;

    public KDTree(Function<T, Point<K>> keyExtractor, Comparator<K> comparator) {
        this.keyExtractor = keyExtractor;
        this.comparator = comparator;
    }

    @Override
    public boolean insert(T value) {
        Point<K> point = keyExtractor.apply(value);

        if (root == null) {
            root = new Node<>(value, point);
            return true;
        }

        Node<T, K> current = root;
        int depth = 0;

        while (true) {
            if (isSame(point, current.point)) {
                return false;
            }

            if (compare(point, current.point, depth) < 0) {
                if (current.left == null) {
                    current.left = new Node<>(value, point);
                    return true;
                }
                current = current.left;
            } else {
                if (current.right == null) {
                    current.right = new Node<>(value, point);
                    return true;
                }
                current = current.right;
            }
            depth++;
        }
    }

    @Override
    public Optional<T> find(Point<K> point) {
        Node<T, K> current = root;
        int depth = 0;

        while (current != null) {
            if (isSame(point, current.point)) {
                return Optional.of(current.value);
            }

            if (compare(point, current.point, depth) < 0) {
                current = current.left;
            } else {
                current = current.right;
            }
            depth++;
        }
        return Optional.empty();
    }

    private boolean isSame(Point<K> a, Point<K> b) {
        return comparator.compare(a.x(), b.x()) == 0
                && comparator.compare(a.y(), b.y()) == 0;
    }

    private int compare(Point<K> a, Point<K> b, int depth) {
        if (depth % 2 == 0) {
            return comparator.compare(a.x(), b.x());
        }
        return comparator.compare(a.y(), b.y());
    }
}
