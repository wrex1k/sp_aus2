package sk.fri.tester;

/**
 * Represents a rectangular area defined by its minimum and maximum latitude and longitude.
 *
 * @param minLat the minimum latitude of the area
 * @param minLon the minimum longitude of the area
 * @param maxLat the maximum latitude of the area
 * @param maxLon the maximum longitude of the area
 */

public record Area(double minLat, double minLon, double maxLat, double maxLon) {
    boolean contains(CsvPoint point) {
        return point.lat() >= minLat && point.lat() <= maxLat
                && point.lon() >= minLon && point.lon() <= maxLon;
    }
}