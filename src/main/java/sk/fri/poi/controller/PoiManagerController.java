package sk.fri.poi.controller;

import sk.fri.poi.model.Poi;
import sk.fri.poi.model.PoiManagerModel;
import sk.fri.poi.model.SpatialStructures;
import sk.fri.poi.view.PoiManagerView;
import sk.fri.poi.view.PoiManagerView.CreateInput;
import sk.fri.poi.view.PoiManagerView.EditInput;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Coordinates requests between the POI model and a user interface.
 */
public final class PoiManagerController {
    private final PoiManagerModel model;
    private final PoiManagerView view;
    private final Path csvPath;

    /**
     * Creates a controller for a model, view, and CSV file.
     *
     * @param model application model
     * @param view user interface
     * @param csvPath CSV file used for loading and saving
     */
    public PoiManagerController(PoiManagerModel model, PoiManagerView view, Path csvPath) {
        this.model = model;
        this.view = view;
        this.csvPath = csvPath;
    }

    /**
     * Creates a controller for a graphical view that sends values directly to its operations.
     *
     * @param model application model
     */
    public PoiManagerController(PoiManagerModel model) {
        this(model, null, null);
    }

    /**
     * Loads the CSV file before showing the main menu.
     *
     * @return true when loading succeeds
     */
    public boolean loadAtStartup() {
        try {
            model.loadFromCsv(csvPath);
            view.showMessage("Loaded POIs: " + model.getAll().size());
            return true;
        } catch (IOException | RuntimeException exception) {
            view.showMessage("Could not load CSV: " + exception.getMessage());
            return false;
        }
    }

    /**
     * Runs the main menu until the user chooses an exit option.
     */
    public void run() {
        boolean running = true;
        while (running) {
            try {
                int choice = view.showMenu();
                switch (choice) {
                    case 1 -> findAt();
                    case 2 -> findNearby();
                    case 3 -> create();
                    case 4 -> update();
                    case 5 -> findByName();
                    case 6 -> delete();
                    case 7 -> view.showPois(model.getAll());
                    case 8 -> generate();
                    case 9 -> save();
                    case 0 -> {
                        save();
                        running = false;
                    }
                    case 10 -> running = false;
                    default -> view.showMessage("Invalid choice.");
                }
            } catch (IOException | IllegalArgumentException | IllegalStateException exception) {
                view.showMessage("Operation failed: " + exception.getMessage());
            }
        }
    }

    /**
     * Returns all active POIs.
     *
     * @return active POIs
     */
    public List<Poi> getAllPois() {
        return model.getAll();
    }

    /**
     * Returns all stored POIs, including soft-deleted records.
     *
     * @return all stored POIs
     */
    public List<Poi> getAllPoisIncludingDeleted() {
        return model.getAllIncludingDeleted();
    }

    /**
     * Checks whether a POI is soft-deleted.
     *
     * @param poi POI to check
     * @return true if the POI is deleted
     */
    public boolean isDeleted(Poi poi) {
        return model.isDeleted(poi);
    }

    /**
     * Changes the spatial index while preserving all POIs.
     *
     * @param structure replacement spatial structure
     */
    public void setSpatialStructure(SpatialStructures structure) {
        model.setStructure(structure);
    }

    /** Returns the active spatial structure. */
    public SpatialStructures getSpatialStructure() {
        return model.getStructure();
    }

    /**
     * Finds POIs at an exact GPS position.
     *
     * @param latitude latitude in decimal degrees
     * @param longitude longitude in decimal degrees
     * @return matching POIs
     */
    public List<Poi> findAt(double latitude, double longitude) {
        return model.findAt(latitude, longitude);
    }

    /**
     * Finds POIs within a radius of a GPS position.
     *
     * @param latitude center latitude
     * @param longitude center longitude
     * @param radiusMeters search radius in meters
     * @return matching POIs
     */
    public List<Poi> findNearby(double latitude, double longitude, double radiusMeters) {
        return model.findNearby(latitude, longitude, radiusMeters);
    }

    /**
     * Creates a POI from values supplied by the view.
     *
     * @param input new POI values
     * @return created POI
     */
    public Poi create(CreateInput input) {
        return model.create(
                input.name(), input.description(), input.latitude(), input.longitude(), input.address()
        );
    }

    /**
     * Updates a POI using values supplied by the view.
     *
     * @param current POI to update
     * @param input replacement values
     * @return updated POI
     */
    public Poi update(Poi current, EditInput input) {
        return model.update(
                current.id(),
                input.name() == null ? current.name() : input.name(),
                input.description() == null ? current.description() : input.description(),
                input.latitude() == null ? current.latitude() : input.latitude(),
                input.longitude() == null ? current.longitude() : input.longitude(),
                input.address() == null ? current.address() : input.address()
        );
    }

    /**
     * Searches names within an inclusive rectangular area.
     *
     * @param searchText text to find in POI names
     * @param minLatitude minimum latitude
     * @param minLongitude minimum longitude
     * @param maxLatitude maximum latitude
     * @param maxLongitude maximum longitude
     * @return matching POIs
     */
    public List<Poi> findByNameInArea(
            String searchText,
            double minLatitude,
            double minLongitude,
            double maxLatitude,
            double maxLongitude
    ) {
        return model.findByNameInArea(searchText, minLatitude, minLongitude, maxLatitude, maxLongitude);
    }

    /**
     * Soft-deletes the selected POI.
     *
     * @param poi POI to delete
     */
    public void delete(Poi poi) {
        model.delete(poi);
    }

    /**
     * Generates test POIs through the model.
     *
     * @param count number of POIs to generate
     * @return number of generated POIs
     */
    public int generateRandom(int count) {
        return model.generateRandom(count);
    }

    /**
     * Loads POIs from a selected CSV file.
     *
     * @param path CSV file path
     * @throws IOException if the file cannot be read
     */
    public void loadFromCsv(Path path) throws IOException {
        model.loadFromCsv(path);
    }

    /**
     * Saves active POIs to a selected CSV file.
     *
     * @param path CSV file path
     * @throws IOException if the file cannot be written
     */
    public void saveToCsv(Path path) throws IOException {
        model.saveToCsv(path);
    }

    /**
     * Finds and displays POIs at the entered GPS position.
     */
    private void findAt() {
        view.showPois(model.findAt(view.askLatitude(), view.askLongitude()));
    }

    /**
     * Finds and displays POIs within the entered radius.
     */
    private void findNearby() {
        double latitude = view.askLatitude();
        double longitude = view.askLongitude();
        double radius = view.askRadius();
        view.showPois(model.findNearby(latitude, longitude, radius));
    }

    /**
     * Creates a POI from the values entered the view.
     */
    private void create() {
        CreateInput input = view.askCreateInput();
        Poi created = create(input);
        view.showMessage("Created POI with ID: " + created.id());
    }

    /**
     * Finds a nearby active POI, collects its new values, and updates it.
     */
    private void update() {
        double latitude = view.askLatitude();
        double longitude = view.askLongitude();
        List<Poi> candidates = model.findNearby(latitude, longitude, view.askRadius());
        int selected = view.selectPoi(candidates);
        if (selected < 0) {
            return;
        }

        Poi current = candidates.get(selected);
        EditInput input = view.askEditInput(current);
        Poi updated = update(current, input);
        view.showMessage("Updated POI with ID: " + updated.id());
    }

    /**
     * Searches names in the area entered in the view.
     */
    private void findByName() {
        String searchText = view.askSearchText();
        double[] area = view.askArea();
        view.showPois(model.findByNameInArea(searchText, area[0], area[1], area[2], area[3]));
    }

    /**
     * Selects a nearby POI and marks it as deleted in the model.
     */
    private void delete() {
        double latitude = view.askLatitude();
        double longitude = view.askLongitude();
        List<Poi> candidates = model.findNearby(latitude, longitude, view.askRadius());
        int selected = view.selectPoi(candidates);
        if (selected >= 0) {
            Poi poi = candidates.get(selected);
            delete(poi);
            view.showMessage("Deleted POI with ID: " + poi.id());
        }
    }

    /**
     * Generates and reports the requested number of test POIs.
     */
    private void generate() {
        int count = generateRandom(view.askGeneratedCount());
        view.showMessage("Generated POIs: " + count);
    }

    /**
     * Saves active POIs to the configured CSV file.
     *
     * @throws IOException if the file cannot be written
     */
    private void save() throws IOException {
        saveToCsv(csvPath);
        view.showMessage("Saved POIs: " + model.getAll().size());
    }
}
