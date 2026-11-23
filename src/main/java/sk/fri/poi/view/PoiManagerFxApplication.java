package sk.fri.poi.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sk.fri.poi.controller.PoiManagerController;
import sk.fri.poi.model.Poi;
import sk.fri.poi.model.PoiManagerModel;
import sk.fri.poi.model.SpatialStructures;
import sk.fri.poi.view.PoiManagerView.CreateInput;
import sk.fri.poi.view.PoiManagerView.EditInput;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/** JavaFX entry point and coordinator for the POI Manager views. */
public final class PoiManagerFxApplication extends Application {
    private final Label status = new Label();
    private PoiManagerController controller;
    private PoiTableView table;
    private PoiMapView map;
    private PoiControlsView controls;
    private Path csvPath;
    private Stage stage;
    private Dialog<ButtonType> gpsDialog;
    private List<Poi> currentResults = List.of();
    private boolean checkedUpdatePending;
    private String statusMessage = "";
    private Supplier<List<Poi>> currentQuery;
    private boolean allResults;

    /**
     * Selects a spatial structure and CSV file before opening the main window.
     *
     * @param stage primary application window
     */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("POI Manager");
        stage.setScene(new Scene(new BorderPane(), 1200, 700));
        Optional<SpatialStructures> selectedStructure = chooseSpatialStructure(stage);
        if (selectedStructure.isEmpty()) {
            Platform.exit();
            return;
        }
        controller = new PoiManagerController(new PoiManagerModel(selectedStructure.get()));
        while (true) {
            Optional<CsvSelection> selection = chooseCsvFile(stage);
            if (selection.isEmpty()) {
                Platform.exit();
                return;
            }
            try {
                Path selectedPath = selection.get().path();
                if (selection.get().createNew()) {
                    controller.saveToCsv(selectedPath);
                } else {
                    controller.loadFromCsv(selectedPath);
                }
                csvPath = selectedPath;
                break;
            } catch (IOException | RuntimeException exception) {
                showError("Could not open CSV", exceptionDetails(exception));
            }
        }
        createLayout();
        showAllPois(1);
        stage.show();
    }

    /**
     * Asks which spatial structure to use before selecting the data file.
     *
     * @param owner window that owns the selection dialog
     * @return the selected structure, or empty if the dialog was cancelled
     */
    private Optional<SpatialStructures> chooseSpatialStructure(Stage owner) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                "KD Tree",
                "KD Tree",
                "Quad Tree",
                "Quad KD Tree"
        );
        dialog.initOwner(owner);
        dialog.setTitle("POI Manager");
        dialog.setHeaderText("Choose a spatial structure");
        dialog.setContentText("Structure:");
        return dialog.showAndWait().map(choice -> switch (choice) {
            case "KD Tree" -> SpatialStructures.KD_TREE;
            case "Quad Tree" -> SpatialStructures.QUAD_TREE;
            case "Quad KD Tree" -> SpatialStructures.QUAD_KD_TREE;
            default -> throw new IllegalStateException("Unknown spatial structure: " + choice);
        });
    }

    /**
     * Presents the two startup actions and opens the corresponding file chooser.
     *
     * @param owner window that owns the dialogs
     * @return the selected CSV path and whether it should be created, or empty if cancelled
     */
    private Optional<CsvSelection> chooseCsvFile(Stage owner) {
        ButtonType create = new ButtonType("New file", ButtonBar.ButtonData.YES);
        ButtonType open = new ButtonType("Open file", ButtonBar.ButtonData.NO);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("POI Manager");
        dialog.setHeaderText("Welcome in a POI Manager. Choose an action to start.");
        dialog.getDialogPane().getButtonTypes().addAll(create, open);
        dialog.setResultConverter(button -> button);
        Optional<ButtonType> choice = dialog.showAndWait();
        if (choice.isEmpty() || (choice.get() != create && choice.get() != open)) {
            return Optional.empty();
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle(choice.get() == create ? "New CSV file" : "Open CSV file");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        File selectedFile;
        if (choice.get() == create) {
            chooser.setInitialFileName("poi.csv");
            selectedFile = chooser.showSaveDialog(owner);
        } else {
            selectedFile = chooser.showOpenDialog(owner);
        }
        if (selectedFile == null) {
            return Optional.empty();
        }
        return Optional.of(new CsvSelection(selectedFile.toPath(), choice.get() == create));
    }

    /**
     * Creates the table, map and operation controls.
     */
    private void createLayout() {
        table = new PoiTableView(poi -> controller.isDeleted(poi));
        map = new PoiMapView(poi -> controller.isDeleted(poi), poi -> {
            table.getSelectionModel().select(poi);
            table.scrollTo(poi);
        }, this::createPoiAt);
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                updateMap());
        table.checkedIds().addListener((javafx.collections.SetChangeListener<String>) change -> {
            if (!checkedUpdatePending) {
                checkedUpdatePending = true;
                Platform.runLater(() -> {
                    checkedUpdatePending = false;
                    updateMap();
                    setStatus(statusMessage);
                });
            }
        });
        controls = new PoiControlsView(this::findAt, this::findNearby, this::searchByName, this::showAllPois,
                this::createPoi, this::editPoi, this::deletePoi, this::generatePois, this::savePois,
                this::applyFilter);
        SplitPane content = new SplitPane(table, map);
        content.setDividerPositions(0.65);
        BorderPane root = new BorderPane();
        root.setTop(new VBox(createMenuBar(), controls));
        root.setCenter(content);
        root.setBottom(status);
        BorderPane.setMargin(content, new Insets(0, 8, 8, 8));
        BorderPane.setMargin(status, new Insets(0, 8, 8, 8));
        stage.getScene().setRoot(root);
    }

    /**
     * Adds file actions to the menu bar.
     *
     * @return the menu bar containing file actions
     */
    private MenuBar createMenuBar() {
        Menu file = new Menu("File");
        MenuItem openExisting = new MenuItem("Open file");
        openExisting.setOnAction(event -> openExistingFile());
        MenuItem createNew = new MenuItem("New file");
        createNew.setOnAction(event -> createNewFile());
        file.getItems().addAll(openExisting, createNew);
        return new MenuBar(file);
    }

    /**
     * Opens and loads an existing CSV without discarding the current data on failure.
     */
    private void openExistingFile() {
        FileChooser chooser = csvFileChooser("Open CSV file");
        File selected = chooser.showOpenDialog(stage);
        if (selected == null || !confirmFileSwitch(selected.toPath(), false)) {
            return;
        }
        try {
            Path selectedPath = selected.toPath();
            controller.loadFromCsv(selectedPath);
            csvPath = selectedPath;
            resetResultsAfterFileSwitch("Opened " + csvPath + ".");
        } catch (IOException | RuntimeException exception) {
            showError("Could not open CSV", exceptionDetails(exception));
        }
    }

    /**
     * Creates a new empty CSV and makes it the active file.
     */
    private void createNewFile() {
        FileChooser chooser = csvFileChooser("Create CSV file");
        chooser.setInitialFileName("poi.csv");
        File selected = chooser.showSaveDialog(stage);
        if (selected == null || !confirmFileSwitch(selected.toPath(), true)) {
            return;
        }
        Path selectedPath = selected.toPath();
        PoiManagerController emptyController =
                new PoiManagerController(new PoiManagerModel(controller.getSpatialStructure()));
        try {
            emptyController.saveToCsv(selectedPath);
            controller = emptyController;
            csvPath = selectedPath;
            table.checkedIds().clear();
            resetResultsAfterFileSwitch("Created " + csvPath + ".");
        } catch (IOException | RuntimeException exception) {
            showError("Could not create CSV", exceptionDetails(exception));
        }
    }

    /**
     * Creates a file chooser configured for CSV files.
     *
     * @param title title displayed by the file chooser
     * @return a CSV file chooser
     */
    private FileChooser csvFileChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        if (csvPath != null && csvPath.toAbsolutePath().getParent() != null
                && csvPath.toAbsolutePath().getParent().toFile().isDirectory()) {
            chooser.setInitialDirectory(csvPath.toAbsolutePath().getParent().toFile());
        }
        return chooser;
    }

    /**
     * Confirms a file switch when the current data might be discarded.
     *
     * @param selectedPath path selected for the new active file
     * @param creating true when creating a new file, false when opening one
     * @return true if the file switch is allowed
     */
    private boolean confirmFileSwitch(Path selectedPath, boolean creating) {
        if (!creating && csvPath != null
                && csvPath.toAbsolutePath().normalize().equals(selectedPath.toAbsolutePath().normalize())) {
            return true;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.initOwner(stage);
        confirmation.setTitle("Switch CSV file");
        confirmation.setHeaderText(creating ? "Create a new CSV file?" : "Replace the current POI data?");
        confirmation.setContentText("The current POI data and unsaved changes to " + csvPath
                + " will be replaced. Save them first if you want to keep them.");
        return confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    /**
     * Resets selection and displays the default fraction after switching files.
     *
     * @param message status message to display after the switch
     */
    private void resetResultsAfterFileSwitch(String message) {
        table.checkedIds().clear();
        currentQuery = controller::getAllPoisIncludingDeleted;
        allResults = true;
        currentResults = List.of();
        showAllPois(1);
        setStatus(message);
    }

    /**
     * Formats an exception and its causes for an error dialog.
     *
     * @param exception exception to format
     * @return formatted exception and cause details
     */
    private String exceptionDetails(Throwable exception) {
        StringBuilder details = new StringBuilder();
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (details.length() > 0) {
                details.append("\nCaused by: ");
            }
            details.append(cause.getClass().getSimpleName());
            if (cause.getMessage() != null && !cause.getMessage().isBlank()) {
                details.append(": ").append(cause.getMessage());
            }
        }
        return details.toString();
    }

    /**
     * Holds a selected CSV path and whether it should be created.
     *
     * @param path selected CSV file path
     * @param createNew true when a new file should be created, false when an existing file is opened
     */
    private record CsvSelection(Path path, boolean createNew) {
    }

    /**
     * Opens a single non-modal GPS search dialog.
     */
    private void findAt() {
        if (gpsDialog != null && gpsDialog.isShowing()) {
            gpsDialog.getDialogPane().getScene().getWindow().requestFocus();
            return;
        }
        Dialog<ButtonType> dialog = createFormDialog("Find by GPS position", "Search");
        dialog.initModality(Modality.NONE);
        gpsDialog = dialog;
        GridPane form = createFormGrid();
        TextField latitude = addField(form, 0, "Latitude", "");
        TextField longitude = addField(form, 1, "Longitude", "");
        attachForm(dialog, form, () -> {
            double lat = coordinate(latitude, "Latitude", -90, 90);
            double lon = coordinate(longitude, "Longitude", -180, 180);
            startQuery(() -> controller.findAt(lat, lon), false, "Position search complete.");
        });
        dialog.setOnHidden(event -> gpsDialog = null);
        dialog.show();
    }

    /**
     * Searches around the selected POI to find candidates for editing and deletion.
     */
    private void findNearby() {
        Poi center = table.getSelectionModel().getSelectedItem();
        if (center == null) {
            showError("No POI selected", "Select a POI before searching nearby.");
            return;
        }
        Dialog<ButtonType> dialog = createFormDialog("Find nearby POIs", "Search");
        GridPane form = createFormGrid();
        TextField radius = addField(form, 0, "Radius in meters", "");
        attachForm(dialog, form, () -> {
            double distance = positiveDouble(radius, "Radius");
            startQuery(() -> controller.findNearby(center.latitude(), center.longitude(), distance),
                    false, "Nearby search complete. Check candidates to edit or delete.");
        });
        dialog.showAndWait();
    }

    /**
     * Opens a form to create a POI.
     */
    private void createPoi() {
        createPoiAt(null, null);
    }

    /**
     * Opens the existing creation form with coordinates from a map click.
     *
     * @param initialLatitude latitude selected on the map, or null if not set
     * @param initialLongitude longitude selected on the map, or null if not set
     */
    private void createPoiAt(Double initialLatitude, Double initialLongitude) {
        Dialog<ButtonType> dialog = createFormDialog("Create POI", "Create");
        GridPane form = createFormGrid();
        TextField name = addField(form, 0, "Name", "");
        TextField description = addField(form, 1, "Description", "");
        TextField latitude = addField(form, 2, "Latitude",
                initialLatitude == null ? "" : Double.toString(initialLatitude));
        TextField longitude = addField(form, 3, "Longitude",
                initialLongitude == null ? "" : Double.toString(initialLongitude));
        TextField address = addField(form, 4, "Address", "");
        attachForm(dialog, form, () -> {
            requireName(name);
            controller.create(new CreateInput(name.getText(), description.getText(),
                    coordinate(latitude, "Latitude", -90, 90),
                    coordinate(longitude, "Longitude", -180, 180), address.getText()));
            refreshResults();
            setStatus("POI created.");
        });
        dialog.showAndWait();
    }

    /**
     * Opens a form for the single checked active POI.
     */
    private void editPoi() {
        List<Poi> checked = table.getCheckedPois();
        if (checked.size() != 1) {
            showError("Select one POI", "Check exactly one active POI to edit.");
            return;
        }
        Poi selected = checked.get(0);
        if (controller.isDeleted(selected)) {
            showError("POI is deleted", "Deleted POIs cannot be edited.");
            return;
        }
        Dialog<ButtonType> dialog = createFormDialog("Edit POI", "Save");
        GridPane form = createFormGrid();
        TextField name = addField(form, 0, "Name", selected.name());
        TextField description = addField(form, 1, "Description", selected.description());
        TextField latitude = addField(form, 2, "Latitude", Double.toString(selected.latitude()));
        TextField longitude = addField(form, 3, "Longitude", Double.toString(selected.longitude()));
        TextField address = addField(form, 4, "Address", selected.address());
        attachForm(dialog, form, () -> {
            requireName(name);
            controller.update(selected, new EditInput(name.getText(), description.getText(),
                    coordinate(latitude, "Latitude", -90, 90),
                    coordinate(longitude, "Longitude", -180, 180), address.getText()));
            refreshResults();
            setStatus("POI updated.");
        });
        dialog.showAndWait();
    }

    /**
     * Searches names within an inclusive geographic area.
     */
    private void searchByName() {
        Dialog<ButtonType> dialog = createFormDialog("Search names in an area", "Search");
        GridPane form = createFormGrid();
        TextField text = addField(form, 0, "Search text", "");
        TextField minLatitude = addField(form, 1, "Minimum latitude", "");
        TextField minLongitude = addField(form, 2, "Minimum longitude", "");
        TextField maxLatitude = addField(form, 3, "Maximum latitude", "");
        TextField maxLongitude = addField(form, 4, "Maximum longitude", "");
        attachForm(dialog, form, () -> {
            if (text.getText().isBlank()) {
                throw new IllegalArgumentException("Search text must not be empty.");
            }
            double minLat = coordinate(minLatitude, "Minimum latitude", -90, 90);
            double minLon = coordinate(minLongitude, "Minimum longitude", -180, 180);
            double maxLat = coordinate(maxLatitude, "Maximum latitude", -90, 90);
            double maxLon = coordinate(maxLongitude, "Maximum longitude", -180, 180);
            if (minLat > maxLat || minLon > maxLon) {
                throw new IllegalArgumentException("Minimum coordinates cannot exceed maximum coordinates.");
            }
            String search = text.getText();
            startQuery(() -> controller.findByNameInArea(search, minLat, minLon, maxLat, maxLon),
                    false, "Name search complete.");
        });
        dialog.showAndWait();
    }

    /**
     * Confirms deletion of the checked active POIs.
     */
    private void deletePoi() {
        List<Poi> checked = table.getCheckedPois();
        List<Poi> selected = checked.stream().filter(poi -> !controller.isDeleted(poi)).toList();
        if (selected.isEmpty()) {
            showError("No active POIs selected", "Check at least one active POI to delete.");
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.initOwner(stage);
        confirmation.setTitle("Delete POI");
        confirmation.setHeaderText("Delete " + selected.size() + " active POI(s)?");
        confirmation.setContentText("Deleted POIs cannot be edited or deleted again.");
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        try {
            for (Poi poi : selected) {
                controller.delete(poi);
                table.checkedIds().remove(poi.id());
            }
            refreshResults();
            setStatus("Deleted POIs: " + selected.size() + ".");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            refreshResults();
            showError("Could not delete POI", exception.getMessage());
        }
    }

    /**
     * Prompts for a positive number of test POIs.
     */
    private void generatePois() {
        Dialog<ButtonType> dialog = createFormDialog("Generate test POIs", "Generate");
        GridPane form = createFormGrid();
        TextField count = addField(form, 0, "Count", "10");
        attachForm(dialog, form, () -> {
            int value;
            try {
                value = Integer.parseInt(count.getText().trim());
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Count must be a positive whole number.");
            }
            if (value <= 0) {
                throw new IllegalArgumentException("Count must be a positive whole number.");
            }
            int generated = controller.generateRandom(value);
            showAllPois(1);
            setStatus("Generated POIs: " + generated + ".");
        });
        dialog.showAndWait();
    }

    /**
     * Saves active records to the CSV file selected at startup.
     */
    private void savePois() {
        try {
            controller.saveToCsv(csvPath);
            setStatus("Saved POIs to " + csvPath + ".");
        } catch (IOException | IllegalArgumentException | IllegalStateException exception) {
            showError("Could not save CSV", exception.getMessage());
        }
    }

    /**
     * Shows the selected cumulative fraction of all records as the current filtering context.
     *
     * @param part selected fraction in tenths, from 1 through 10
     * @throws IllegalArgumentException if the fraction is outside the range 1 through 10
     */
    private void showAllPois(int part) {
        if (part < 1 || part > 10) {
            throw new IllegalArgumentException("POI list part must be between 1 and 10.");
        }
        Supplier<List<Poi>> query = () -> {
            List<Poi> all = controller.getAllPoisIncludingDeleted();
            int to = (int) (((long) all.size() * part + 9) / 10);
            return all.subList(0, to);
        };
        controls.setShowAllPart(part);
        String message = part == 10 ? "Showing all POIs."
                : "Showing first " + part + "/10 of all POIs.";
        startQuery(query, true, message);
    }

    /**
     * Records a query and displays its current results.
     *
     * @param query query that supplies the current result set
     * @param all true when the query represents all stored records
     * @param message status message associated with the query
     */
    private void startQuery(Supplier<List<Poi>> query, boolean all, String message) {
        List<Poi> results = query.get();
        currentQuery = query;
        allResults = all;
        statusMessage = message;
        currentResults = List.copyOf(results);
        table.retainChecks(currentResults);
        applyFilter();
    }

    /**
     * Re-runs the current query while retaining deleted records already in its results.
     */
    private void refreshResults() {
        List<Poi> updated = new ArrayList<>(currentQuery.get());
        if (!allResults) {
            Set<String> resultIds = new HashSet<>();
            updated.forEach(poi -> resultIds.add(poi.id()));
            for (Poi previous : currentResults) {
                if (controller.isDeleted(previous) && resultIds.add(previous.id())) {
                    updated.add(previous);
                }
            }
        }
        currentResults = List.copyOf(updated);
        table.retainChecks(currentResults);
        applyFilter();
    }

    /**
     * Filters the current results without replacing or rerunning the search.
     */
    private void applyFilter() {
        Poi selected = table.getSelectionModel().getSelectedItem();
        List<Poi> visible = currentResults.stream()
                .filter(poi -> controls.isShowDeleted() || !controller.isDeleted(poi))
                .toList();
        table.getItems().setAll(visible);
        if (selected != null) {
            visible.stream().filter(poi -> poi.id().equals(selected.id())).findFirst()
                    .ifPresent(poi -> table.getSelectionModel().select(poi));
        }
        updateMap();
        setStatus(statusMessage);
    }

    /**
     * Synchronizes markers with visible rows, row focus, and checked records.
     */
    private void updateMap() {
        map.showPois(table.getItems(), table.getSelectionModel().getSelectedItem(), table.checkedIds());
    }

    /**
     * Updates the status bar with the current result count.
     *
     * @param message status message to display
     */
    private void setStatus(String message) {
        statusMessage = message;
        status.setText(message + " Results: " + table.getItems().size()
                + "  Checked: " + table.getCheckedPois().size());
    }

    /**
     * Creates a dialog with submit and cancel buttons.
     *
     * @param title dialog title
     * @param submitText text displayed on the submit button
     * @return the configured form dialog
     */
    private Dialog<ButtonType> createFormDialog(String title, String submitText) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType(submitText, ButtonData.OK_DONE), ButtonType.CANCEL);
        return dialog;
    }

    /**
     * Adds inline errors and keeps the dialog open until its operation succeeds.
     *
     * @param dialog dialog to attach the form to
     * @param form form content to display
     * @param operation operation to run when the user submits the form
     */
    private void attachForm(Dialog<ButtonType> dialog, GridPane form, Runnable operation) {
        Label error = new Label();
        error.setStyle("-fx-text-fill: firebrick;");
        error.setWrapText(true);
        dialog.getDialogPane().setContent(new VBox(4, form, error));
        ButtonType submit = dialog.getDialogPane().getButtonTypes().stream()
                .filter(button -> button.getButtonData() == ButtonData.OK_DONE)
                .findFirst().orElseThrow();
        Button button = (Button) dialog.getDialogPane().lookupButton(submit);
        button.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                operation.run();
            } catch (IllegalArgumentException | IllegalStateException exception) {
                error.setText(exception.getMessage());
                event.consume();
            }
        });
    }

    /**
     * Creates a padded form grid.
     *
     * @return the configured form grid
     */
    private GridPane createFormGrid() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(12));
        return form;
    }

    /**
     * Adds a labeled text field to a form.
     *
     * @param form grid that receives the field
     * @param row row in which to place the field
     * @param label text displayed beside the field
     * @param initial initial field value
     * @return the created text field
     */
    private TextField addField(GridPane form, int row, String label, String initial) {
        TextField field = new TextField(initial);
        form.addRow(row, new Label(label), field);
        return field;
    }

    /**
     * Validates a required POI name before invoking the controller.
     *
     * @param field field containing the POI name
     * @throws IllegalArgumentException if the name is blank
     */
    private void requireName(TextField field) {
        if (field.getText().isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }
    }

    /**
     * Parses a finite number from an input field.
     *
     * @param field field containing the number
     * @param label field name used in validation messages
     * @return the parsed finite number
     * @throws IllegalArgumentException if the value is not a finite number
     */
    private double parseDouble(TextField field, String label) {
        double value;
        try {
            value = Double.parseDouble(field.getText().trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " must be a number.");
        }
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(label + " must be a finite number.");
        }
        return value;
    }

    /**
     * Parses and validates a geographic coordinate.
     *
     * @param field field containing the coordinate
     * @param label coordinate name used in validation messages
     * @param minimum lowest permitted coordinate value
     * @param maximum highest permitted coordinate value
     * @return the parsed coordinate
     * @throws IllegalArgumentException if the value is not a finite number within the permitted range
     */
    private double coordinate(TextField field, String label, double minimum, double maximum) {
        double value = parseDouble(field, label);
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(label + " must be between " + minimum + " and " + maximum + ".");
        }
        return value;
    }

    /**
     * Parses a strictly positive finite number.
     *
     * @param field field containing the number
     * @param label field name used in validation messages
     * @return the parsed positive number
     * @throws IllegalArgumentException if the value is not finite or is not greater than zero
     */
    private double positiveDouble(TextField field, String label) {
        double value = parseDouble(field, label);
        if (value <= 0) {
            throw new IllegalArgumentException(label + " must be greater than zero.");
        }
        return value;
    }

    /**
     * Displays an error alert.
     *
     * @param title alert title
     * @param message error details to display
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message == null ? "An unexpected error occurred." : message);
        alert.showAndWait();
    }
}
