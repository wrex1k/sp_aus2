package sk.fri.tester;

import sk.fri.structures.KDPoint;
import sk.fri.structures.Point;

import java.util.List;

/**
 * Represents a point with an ID, latitude, and longitude.
 *
 * @param id  the unique identifier of the point (not used in SpatialTrees)
 * @param lat the latitude of the point
 * @param lon the longitude of the point
 */
public record CsvPoint(long id, double lat, double lon) {

    /**
     * Converts this CsvPoint to a Point<Double> object.
     *
     * @return a Point<Double> representing the latitude and longitude of this CsvPoint
     */
    Point<Double> toPoint() {
        return new Point<>(lat, lon);
    }

    /**
     * Converts this CsvPoint to a KDPoint<Double> object.
     *
     * @return a KDPoint<Double> representing the latitude, longitude and random altitude of this CsvPoint
     */
    KDPoint<Double> toKDPoint() {

        return new KDPoint<>(List.of(lat, lon));
    }
}
