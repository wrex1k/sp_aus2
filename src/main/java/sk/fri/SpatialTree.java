package sk.fri;

import java.util.List;
import java.util.Optional;

/**
 * Represents a spatial tree that stores values associated with points
 * in a two-dimensional coordinate system.
 *
 * @param <T> type of stored values
 * @param <K> type of coordinates
 */
public interface SpatialTree<T, K> {

    /**
     * Inserts a value into the tree.
     *
     * @param value value to insert
     * @return true if the value was successfully inserted, false otherwise
     */
    boolean insert(T value);

    /**
     * Finds values associated with the specified point.
     *
     * @param point point to search for
     * @return an Optional containing a list of matching values,
     *         or an empty Optional if no values are found
     */
    Optional<List<T>> find(Point<K> point);

    /**
     * Finds all values whose points lie within the specified rectangular region.
     *
     * @param min minimum coordinates of the region
     * @param max maximum coordinates of the region
     * @return list of values whose points lie within the region
     */
    List<T> rangeSearch(Point<K> min, Point<K> max);
}