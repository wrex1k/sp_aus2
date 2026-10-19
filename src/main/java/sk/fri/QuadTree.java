package sk.fri;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

public class QuadTree<T, K> implements SpatialTree<T, K> {

    private static class Node<T, K> {
        private final T value;
        private final Point<K> point;

        private final Node<T, K>[] children = new Node[4];

        private Node(T value, Point<K> point) {
            this.value = value;
            this.point = point;
        }
    }

    private final Function<T, Point<K>> keyExtractor;
    private final Comparator<K> comparator;
    private Node<T, K> root;

    public QuadTree(Function<T, Point<K>> keyExtractor, Comparator<K> comparator) {
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

        while (true) {
            if (isSame(point, current.point)) {
                return false;
            }

            int q = quadrant(point, current.point);
            if (current.children[q] == null) {
                current.children[q] = new Node<>(value, point);
                return true;
            }
            current = current.children[q];
        }
    }

    @Override
    public Optional<T> find(Point<K> point) {
        Node<T, K> current = root;

        while (current != null) {
            if (isSame(point, current.point)) {
                return Optional.of(current.value);
            }
            current = current.children[quadrant(point, current.point)];
        }
        return Optional.empty();
    }

    private boolean isSame(Point<K> a, Point<K> b) {
        return comparator.compare(a.x(), b.x()) == 0 && comparator.compare(a.y(), b.y()) == 0;
    }

    private int quadrant(Point<K> point, Point<K> center) {
        int index = 0;
        if (comparator.compare(point.x(), center.x()) >= 0) {
            index += 1; // vychod
        }
        if (comparator.compare(point.y(), center.y()) >= 0) {
            index += 2; // sever
        }
        return index;
    }
}
