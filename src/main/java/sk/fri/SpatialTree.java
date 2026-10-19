package sk.fri;

import java.util.Optional;

public interface SpatialTree<T, K> {
    boolean insert(T value);

    Optional<T> find(Point<K> point);
}
