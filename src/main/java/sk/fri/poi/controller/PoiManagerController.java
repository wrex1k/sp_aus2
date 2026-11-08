package sk.fri.poi.controller;

import sk.fri.poi.model.Poi;
import sk.fri.poi.model.PoiManagerModel;
import sk.fri.poi.view.PoiManagerView;
import sk.fri.poi.view.PoiManagerView.CreateInput;
import sk.fri.poi.view.PoiManagerView.EditInput;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Coordinates requests between the POI model and the console view.
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
        Poi created = model.create(
                input.name(), input.description(), input.latitude(), input.longitude(), input.address()
        );
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
        Poi updated = model.update(
                current.id(),
                input.name() == null ? current.name() : input.name(),
                input.description() == null ? current.description() : input.description(),
                input.latitude() == null ? current.latitude() : input.latitude(),
                input.longitude() == null ? current.longitude() : input.longitude(),
                input.address() == null ? current.address() : input.address()
        );
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
            model.delete(poi);
            view.showMessage("Deleted POI with ID: " + poi.id());
        }
    }

    /**
     * Generates and reports the requested number of test POIs.
     */
    private void generate() {
        int count = model.generateRandom(view.askGeneratedCount());
        view.showMessage("Generated POIs: " + count);
    }

    /**
     * Saves active POIs to the configured CSV file.
     *
     * @throws IOException if the file cannot be written
     */
    private void save() throws IOException {
        model.saveToCsv(csvPath);
        view.showMessage("Saved POIs: " + model.getAll().size());
    }
}
