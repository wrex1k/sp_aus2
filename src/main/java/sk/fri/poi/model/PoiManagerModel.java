package sk.fri.poi.model;

import sk.fri.adapters.KDTree2DAdapter;
import sk.fri.structures.KDPoint;
import sk.fri.structures.KDTree;
import sk.fri.structures.Point;
import sk.fri.structures.QuadKDTree;
import sk.fri.structures.QuadTree;
import sk.fri.structures.SpatialTree;

import java.io.IOException;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Holds POI data and provides the application's operations independently of its view.
 */
public final class PoiManagerModel {
    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    private final SpatialStructures structure;
    private final PoiCsvRepository csvRepository = new PoiCsvRepository();
    private final Set<String> deletedIds = new HashSet<>();
    private List<Poi> pois = new ArrayList<>();
    private SpatialTree<Poi, Point<Double>> tree;

    /**
     * Creates an empty model using the selected spatial tree.
     *
     * @param structure tree implementation to use
     */
    public PoiManagerModel(SpatialStructures structure) {
        this.structure = structure;
        this.tree = createTree();
    }

    /**
     * Returns the selected tree implementation.
     *
     * @return the selected spatial structure
     */
    public SpatialStructures getStructure() {
        return structure;
    }

    /**
     * Returns all POIs that have not been soft-deleted.
     *
     * @return an immutable list of active POIs
     */
    public List<Poi> getAll() {
        return pois.stream().filter(poi -> !deletedIds.contains(poi.id())).toList();
    }

    /**
     * Finds active POIs at an exact GPS position.
     *
     * @param latitude latitude in decimal degrees
     * @param longitude longitude in decimal degrees
     * @return POIs at the specified position
     */
    public List<Poi> findAt(double latitude, double longitude) {
        Poi.validateCoordinates(latitude, longitude);
        return activeOnly(tree.find(new Point<>(latitude, longitude)).orElseGet(List::of));
    }

    /**
     * Finds active POIs within a radius using spatial candidates and exact distances.
     *
     * @param latitude center latitude
     * @param longitude center longitude
     * @param radiusMeters search radius in meters
     * @return POIs no farther than the requested radius
     */
    public List<Poi> findNearby(double latitude, double longitude, double radiusMeters) {
        Poi.validateCoordinates(latitude, longitude);
        if (!Double.isFinite(radiusMeters) || radiusMeters < 0.0) {
            throw new IllegalArgumentException("Radius must be a non-negative number.");
        }

        double angularRadius = Math.min(radiusMeters / EARTH_RADIUS_METERS, Math.PI);
        double latitudeRadians = Math.toRadians(latitude);
        double minLatitude = Math.max(-90.0, latitude - Math.toDegrees(angularRadius));
        double maxLatitude = Math.min(90.0, latitude + Math.toDegrees(angularRadius));
        List<Poi> candidates;

        if (angularRadius >= Math.PI
                || latitudeRadians - angularRadius <= -Math.PI / 2.0
                || latitudeRadians + angularRadius >= Math.PI / 2.0) {
            candidates = tree.rangeSearch(
                    new Point<>(minLatitude, -180.0),
                    new Point<>(maxLatitude, 180.0)
            ).orElseGet(List::of);
        } else {
            double longitudeRadius = Math.toDegrees(
                    Math.asin(Math.sin(angularRadius) / Math.cos(latitudeRadians))
            );
            candidates = queryLongitudeRange(
                    minLatitude,
                    maxLatitude,
                    longitude - longitudeRadius,
                    longitude + longitudeRadius
            );
        }

        Map<String, Poi> nearby = new LinkedHashMap<>();
        for (Poi candidate : candidates) {
            if (!deletedIds.contains(candidate.id())
                    && distanceMeters(latitude, longitude, candidate.latitude(), candidate.longitude()) <= radiusMeters) {
                nearby.put(candidate.id(), candidate);
            }
        }
        return List.copyOf(nearby.values());
    }

    /**
     * Searches an inclusive rectangular area for active POIs with a matching name.
     *
     * @param searchText text that must appear in the POI name
     * @param minLatitude southern latitude boundary
     * @param minLongitude western longitude boundary
     * @param maxLatitude northern latitude boundary
     * @param maxLongitude eastern longitude boundary
     * @return matching POIs, ignoring case and diacritics
     */
    public List<Poi> findByNameInArea(
            String searchText,
            double minLatitude,
            double minLongitude,
            double maxLatitude,
            double maxLongitude
    ) {
        validateRange(minLatitude, minLongitude, maxLatitude, maxLongitude);
        if (searchText == null || searchText.isBlank()) {
            throw new IllegalArgumentException("Search text must not be empty.");
        }
        String normalizedSearch = normalize(searchText);
        return tree.rangeSearch(
                        new Point<>(minLatitude, minLongitude),
                        new Point<>(maxLatitude, maxLongitude)
                ).orElseGet(List::of)
                .stream()
                .filter(poi -> !deletedIds.contains(poi.id()))
                .filter(poi -> normalize(poi.name()).contains(normalizedSearch))
                .toList();
    }

    /**
     * Creates and inserts a new POI.
     *
     * @param name required name
     * @param description optional description
     * @param latitude latitude in decimal degrees
     * @param longitude longitude in decimal degrees
     * @param address optional address
     * @return the created POI
     * @throws IllegalArgumentException if POI data is invalid
     * @throws IllegalStateException if the selected tree rejects the POI
     */
    public Poi create(String name, String description, double latitude, double longitude, String address) {
        Poi poi = Poi.create(name, description, latitude, longitude, address);
        if (!tree.insert(poi)) {
            throw new IllegalStateException("The spatial tree rejected the POI.");
        }
        pois.add(poi);
        return poi;
    }

    /**
     * Updates an active POI and rebuilds the tree while retaining soft-deleted entries.
     *
     * @param id ID of the POI to update
     * @param name new name
     * @param description new description
     * @param latitude new latitude
     * @param longitude new longitude
     * @param address new address
     * @return the updated POI
     * @throws IllegalArgumentException if the POI is not active or new values are invalid
     */
    public Poi update(
            String id,
            String name,
            String description,
            double latitude,
            double longitude,
            String address
    ) {
        int index = findIndex(id);
        Poi previous = pois.get(index);
        Poi updated = new Poi(
                previous.id(), name, description, latitude, longitude, address,
                previous.createdAt(), Instant.now()
        );
        List<Poi> updatedPois = new ArrayList<>(pois);
        updatedPois.set(index, updated);
        replaceContents(updatedPois);
        return updated;
    }

    /**
     * Marks an active POI as deleted without removing it from the spatial tree.
     *
     * @param poi POI to soft-delete
     * @throws IllegalArgumentException if the POI is null or is not active in this model
     */
    public void delete(Poi poi) {
        if (poi == null) {
            throw new IllegalArgumentException("POI is required.");
        }
        findIndex(poi.id());
        deletedIds.add(poi.id());
    }

    /**
     * Generates and inserts random test POIs into the selected tree.
     *
     * @param count number of POIs to create
     * @return number of generated POIs
     * @throws IllegalArgumentException if count is negative
     * @throws IllegalStateException if the selected tree rejects a POI
     */
    public int generateRandom(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("The number of POIs cannot be negative.");
        }
        for (int index = 0; index < count; index++) {
            String id = UUID.randomUUID().toString();
            Instant now = Instant.now();
            Poi poi = new Poi(
                    id,
                    "TestPoint-" + id.substring(0, 8),
                    "Generated test point",
                    ThreadLocalRandom.current().nextDouble(-90.0, 90.0),
                    ThreadLocalRandom.current().nextDouble(-180.0, 180.0),
                    "",
                    now,
                    now
            );
            if (!tree.insert(poi)) {
                throw new IllegalStateException("The spatial tree rejected the POI.");
            }
            pois.add(poi);
        }
        return count;
    }

    /**
     * Loads a CSV file and resets the deleted-ID set to match its active records.
     *
     * @param path CSV file path
     * @throws IOException if the file cannot be read or contains invalid data
     */
    public void loadFromCsv(Path path) throws IOException {
        List<Poi> loadedPois = csvRepository.read(path);
        replaceContents(loadedPois);
        deletedIds.clear();
    }

    /**
     * Saves active POIs to a CSV file.
     *
     * @param path destination CSV file
     * @throws IOException if the file cannot be written
     */
    public void saveToCsv(Path path) throws IOException {
        csvRepository.write(path, getAll());
    }

    /**
     * Queries a longitude interval, splitting it when it crosses the date line.
     *
     * @param minLatitude minimum latitude
     * @param maxLatitude maximum latitude
     * @param minLongitude minimum longitude, possibly below -180
     * @param maxLongitude maximum longitude, possibly above 180
     * @return matching tree entries
     */
    private List<Poi> queryLongitudeRange(
            double minLatitude,
            double maxLatitude,
            double minLongitude,
            double maxLongitude
    ) {
        if (minLongitude < -180.0) {
            return combine(
                    tree.rangeSearch(
                            new Point<>(minLatitude, -180.0),
                            new Point<>(maxLatitude, maxLongitude)
                    ).orElseGet(List::of),
                    tree.rangeSearch(
                            new Point<>(minLatitude, minLongitude + 360.0),
                            new Point<>(maxLatitude, 180.0)
                    ).orElseGet(List::of)
            );
        }
        if (maxLongitude > 180.0) {
            return combine(
                    tree.rangeSearch(
                            new Point<>(minLatitude, minLongitude),
                            new Point<>(maxLatitude, 180.0)
                    ).orElseGet(List::of),
                    tree.rangeSearch(
                            new Point<>(minLatitude, -180.0),
                            new Point<>(maxLatitude, maxLongitude - 360.0)
                    ).orElseGet(List::of)
            );
        }
        return tree.rangeSearch(
                new Point<>(minLatitude, minLongitude),
                new Point<>(maxLatitude, maxLongitude)
        ).orElseGet(List::of);
    }

    /**
     * Combines tree results while avoiding duplicate POI IDs.
     *
     * @param first results from the first longitude interval
     * @param second results from the second longitude interval
     * @return combined results
     */
    private List<Poi> combine(List<Poi> first, List<Poi> second) {
        Map<String, Poi> results = new LinkedHashMap<>();
        for (Poi poi : first) {
            results.put(poi.id(), poi);
        }
        for (Poi poi : second) {
            results.put(poi.id(), poi);
        }
        return new ArrayList<>(results.values());
    }

    /**
     * Builds a replacement tree before updating the model's stored records.
     *
     * @param replacements all physical records to keep in the tree
     * @throws IllegalArgumentException if duplicate IDs are present
     * @throws IllegalStateException if the selected tree rejects a record
     */
    private void replaceContents(Collection<Poi> replacements) {
        Map<String, Poi> unique = new LinkedHashMap<>();
        for (Poi poi : replacements) {
            if (unique.putIfAbsent(poi.id(), poi) != null) {
                throw new IllegalArgumentException("Duplicate POI ID: " + poi.id());
            }
        }

        SpatialTree<Poi, Point<Double>> replacementTree = createTree();
        for (Poi poi : unique.values()) {
            if (!replacementTree.insert(poi)) {
                throw new IllegalStateException("The spatial tree rejected POI with ID " + poi.id() + ".");
            }
        }
        tree = replacementTree;
        pois = new ArrayList<>(unique.values());
    }

    /**
     * Finds an active POI's index by ID.
     *
     * @param id POI ID
     * @return index in the stored records
     * @throws IllegalArgumentException if the POI is absent or soft-deleted
     */
    private int findIndex(String id) {
        for (int index = 0; index < pois.size(); index++) {
            if (pois.get(index).id().equals(id)) {
                if (deletedIds.contains(id)) {
                    throw new IllegalArgumentException("POI " + id + " has been deleted.");
                }
                return index;
            }
        }
        throw new IllegalArgumentException("POI " + id + " does not exist.");
    }

    /**
     * Creates the selected existing spatial-tree implementation.
     *
     * @return an empty spatial tree for POIs
     */
    private SpatialTree<Poi, Point<Double>> createTree() {
        return switch (structure) {
            case KD_TREE -> new KDTree2DAdapter<>(
                    new KDTree<>(
                            poi -> new KDPoint<>(List.of(poi.latitude(), poi.longitude())),
                            Double::compare,
                            2
                    )
            );
            case QUAD_TREE -> new QuadTree<>(
                    poi -> new Point<>(poi.latitude(), poi.longitude()),
                    Double::compare
            );
            case QUAD_KD_TREE -> new QuadKDTree<>(
                    poi -> new Point<>(poi.latitude(), poi.longitude()),
                    poi -> new KDPoint<>(List.of(poi.latitude(), poi.longitude())),
                    Double::compare
            );
        };
    }

    /**
     * Validates an inclusive geographic rectangle.
     *
     * @param minLatitude minimum latitude
     * @param minLongitude minimum longitude
     * @param maxLatitude maximum latitude
     * @param maxLongitude maximum longitude
     * @throws IllegalArgumentException if coordinates are invalid or reversed
     */
    private static void validateRange(
            double minLatitude,
            double minLongitude,
            double maxLatitude,
            double maxLongitude
    ) {
        Poi.validateCoordinates(minLatitude, minLongitude);
        Poi.validateCoordinates(maxLatitude, maxLongitude);
        if (minLatitude > maxLatitude || minLongitude > maxLongitude) {
            throw new IllegalArgumentException("Minimum coordinates cannot exceed maximum coordinates.");
        }
    }

    /**
     * Removes accents and folds case for name matching.
     *
     * @param value text to normalize
     * @return normalized text
     */
    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
    }

    /**
     * Calculates great-circle distance using the haversine formula.
     *
     * @param firstLatitude first point latitude
     * @param firstLongitude first point longitude
     * @param secondLatitude second point latitude
     * @param secondLongitude second point longitude
     * @return distance in meters
     */
    private static double distanceMeters(
            double firstLatitude,
            double firstLongitude,
            double secondLatitude,
            double secondLongitude
    ) {
        double latitudeDifference = Math.toRadians(secondLatitude - firstLatitude);
        double longitudeDifference = Math.toRadians(secondLongitude - firstLongitude);
        double firstLatitudeRadians = Math.toRadians(firstLatitude);
        double secondLatitudeRadians = Math.toRadians(secondLatitude);
        double haversine = Math.pow(Math.sin(latitudeDifference / 2.0), 2)
                + Math.cos(firstLatitudeRadians) * Math.cos(secondLatitudeRadians)
                * Math.pow(Math.sin(longitudeDifference / 2.0), 2);
        return 2.0 * EARTH_RADIUS_METERS * Math.asin(Math.sqrt(Math.min(1.0, haversine)));
    }

    /**
     * Removes soft-deleted entries from a list of tree results.
     *
     * @param candidates tree search results
     * @return only active POIs
     */
    private List<Poi> activeOnly(List<Poi> candidates) {
        return candidates.stream().filter(poi -> !deletedIds.contains(poi.id())).toList();
    }
}
