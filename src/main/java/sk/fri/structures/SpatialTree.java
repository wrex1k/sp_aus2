package sk.fri.structures;

import java.util.List;
import java.util.Optional;

/**
 * Represents a spatial tree that stores values associated
 * with points in a multidimensional space. The tree supports
 * insertion, point-based search, and range search operations.
 *
 * @param <T> type of stored values
 * @param <P> type of points
 */
public abstract class SpatialTree<T, P> {
    private final String name;

    protected SpatialTree(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the tree implementation.
     *
     * @return the name of the tree
     */
    public final String getName() {
        return name;
    }

    /**
     * Inserts a value into the tree.
     *
     * @param value value to insert
     * @return true if the value was successfully inserted, false otherwise
     */
    public abstract boolean insert(T value);

    /**
     * Finds values associated with the specified point.
     *
     * @param point point to search for
     * @return an Optional containing a list of matching values,
     *         or an empty Optional if no values are found
     */
    public abstract Optional<List<T>> find(P point);

    /**
     * Finds all values whose points lie within the specified rectangular region.
     *
     * @param min minimum coordinates of the region
     * @param max maximum coordinates of the region
     * @return list of values whose points lie within the region
     */
    public abstract Optional<List<T>> rangeSearch(P min, P max);
}