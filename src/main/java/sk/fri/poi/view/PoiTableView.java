package sk.fri.poi.view;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.WeakSetChangeListener;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import sk.fri.poi.model.Poi;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/** Displays POI records and supports copying values from a selected row. */
public final class PoiTableView extends TableView<Poi> {
    private final ObservableSet<String> checkedIds = FXCollections.observableSet();
    private final CheckBox selectAll = new CheckBox();
    private boolean updatingAll;

    /**
     * Creates the table and its columns.
     *
     * @param isDeleted reports whether a POI is soft-deleted
     */
    public PoiTableView(Predicate<Poi> isDeleted) {
        setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        setPlaceholder(new Label("Nothing to display."));
        TableColumn<Poi, Poi> checkedColumn = new TableColumn<>();
        checkedColumn.setGraphic(selectAll);
        checkedColumn.setPrefWidth(45);
        checkedColumn.setSortable(false);
        checkedColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        checkedColumn.setCellFactory(column -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();
            private final SetChangeListener<String> checkListener = change -> updateCheckBox();
            {
                checkBox.setOnAction(event -> {
                    Poi poi = getItem();
                    if (poi != null) {
                        if (checkBox.isSelected()) {
                            checkedIds.add(poi.id());
                        } else {
                            checkedIds.remove(poi.id());
                        }
                    }
                });
                checkedIds.addListener(new WeakSetChangeListener<>(checkListener));
            }

            @Override
            protected void updateItem(Poi poi, boolean empty) {
                super.updateItem(poi, empty);
                setGraphic(empty || poi == null ? null : checkBox);
                updateCheckBox();
            }

            private void updateCheckBox() {
                checkBox.setSelected(getItem() != null && checkedIds.contains(getItem().id()));
            }
        });
        selectAll.setOnAction(event -> {
            boolean selecting = selectAll.isSelected();
            updatingAll = true;
            try {
                for (Poi poi : getItems()) {
                    if (selecting) {
                        checkedIds.add(poi.id());
                    } else {
                        checkedIds.remove(poi.id());
                    }
                }
            } finally {
                updatingAll = false;
                updateSelectAll();
            }
        });
        checkedIds.addListener((SetChangeListener<String>) change -> {
            if (!updatingAll) {
                updateSelectAll();
            }
        });
        getItems().addListener((javafx.collections.ListChangeListener<Poi>) change -> updateSelectAll());
        getColumns().add(checkedColumn);
        getColumns().add(column("ID", 260, Poi::id));
        getColumns().add(column("Name", 150, Poi::name));
        getColumns().add(column("Description", 200, Poi::description));
        getColumns().add(column("Latitude", 150, poi -> Double.toString(poi.latitude())));
        getColumns().add(column("Longitude", 150, poi -> Double.toString(poi.longitude())));
        getColumns().add(column("Address", 180, Poi::address));
        getColumns().add(column("Created At", 190, poi -> poi.createdAt().toString()));
        getColumns().add(column("Updated At", 190, poi -> poi.updatedAt().toString()));
        getColumns().add(column("Status", 90, poi -> isDeleted.test(poi) ? "Deleted" : "Active"));
        setRowFactory(table -> {
            TableRow<Poi> row = new TableRow<>() {
                @Override
                protected void updateItem(Poi poi, boolean empty) {
                    super.updateItem(poi, empty);
                    setStyle(!empty && poi != null && isDeleted.test(poi) ? "-fx-opacity: 0.55;" : "");
                }
            };
            MenuItem id = new MenuItem("Copy ID");
            id.setOnAction(event -> copy(row.getItem().id()));
            MenuItem name = new MenuItem("Copy name");
            name.setOnAction(event -> copy(row.getItem().name()));
            MenuItem latitude = new MenuItem("Copy latitude");
            latitude.setOnAction(event -> copy(Double.toString(row.getItem().latitude())));
            MenuItem longitude = new MenuItem("Copy longitude");
            longitude.setOnAction(event -> copy(Double.toString(row.getItem().longitude())));
            ContextMenu menu = new ContextMenu(id, name, latitude, longitude);
            row.emptyProperty().addListener((observable, oldValue, empty) ->
                    row.setContextMenu(empty ? null : menu));
            row.setOnMousePressed(event -> {
                if (event.getButton() == MouseButton.SECONDARY && !row.isEmpty()) {
                    getSelectionModel().select(row.getIndex());
                }
            });
            return row;
        });
    }

    /** Returns the checked POIs that are currently visible. */
    public List<Poi> getCheckedPois() {
        return getItems().stream().filter(poi -> checkedIds.contains(poi.id())).toList();
    }

    /** Provides checkbox changes to the view coordinator. */
    public ObservableSet<String> checkedIds() {
        return checkedIds;
    }

    /** Discards checks for records no longer in the current result context. */
    public void retainChecks(List<Poi> results) {
        java.util.Set<String> ids = results.stream().map(Poi::id).collect(java.util.stream.Collectors.toSet());
        checkedIds.retainAll(ids);
        updateSelectAll();
    }

    /** Updates the header checkbox for the displayed rows. */
    private void updateSelectAll() {
        long count = getItems().stream().filter(poi -> checkedIds.contains(poi.id())).count();
        selectAll.setIndeterminate(count > 0 && count < getItems().size());
        selectAll.setSelected(!getItems().isEmpty() && count == getItems().size());
    }

    /** Creates a column that reads a value using a POI accessor. */
    private TableColumn<Poi, String> column(String title, double width, Function<Poi, String> value) {
        TableColumn<Poi, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }

    /** Copies text to the system clipboard. */
    private void copy(String value) {
        ClipboardContent content = new ClipboardContent();
        content.putString(value);
        Clipboard.getSystemClipboard().setContent(content);
    }
}
