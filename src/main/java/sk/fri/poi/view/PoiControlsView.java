package sk.fri.poi.view;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/** Contains the main window's grouped operation buttons. */
public final class PoiControlsView extends VBox {
    private final CheckBox showDeleted = new CheckBox("Show deleted");
    private final MenuButton showAllMenu = new MenuButton("Show (1/10)");

    /**
     * Creates controls and connects them to the application actions.
     *
     * @param findAt exact position search
     * @param nearby nearby search
     * @param search name search
     * @param showAll updates the visible fraction of all POIs
     * @param create create a POI
     * @param edit edit a selected POI
     * @param delete delete a selected POI
     * @param generate generate test data
     * @param save save CSV
     * @param filterChanged apply the deleted-record filter
     */
    public PoiControlsView(Runnable findAt, Runnable nearby, Runnable search,
                           java.util.function.IntConsumer showAll,
                           Runnable create, Runnable edit, Runnable delete, Runnable generate,
                           Runnable save, Runnable filterChanged) {
        setSpacing(4);
        setPadding(new Insets(8));
        showDeleted.setOnAction(event -> filterChanged.run());
        FlowPane groups = new FlowPane(12, 4);
        for (int part = 1; part <= 10; part++) {
            int selectedPart = part;
            MenuItem item = new MenuItem(part == 10 ? "All POI" : part + "/10");
            item.setOnAction(event -> {
                showAllMenu.setText(showAllLabel(selectedPart));
                showAll.accept(selectedPart);
            });
            showAllMenu.getItems().add(item);
        }
        addGroup(groups, "Search", button("Find by GPS", findAt), button("Find nearby", nearby),
                button("Search by name", search), showAllMenu);
        addGroup(groups, "Manage POIs", button("Add POI", create), button("Edit selected", edit),
                button("Delete selected", delete));
        addGroup(groups, "Data", button("Generate test POIs", generate), button("Save CSV", save), showDeleted);
        getChildren().add(groups);
    }

    /** Returns the current deleted-record filter state. */
    public boolean isShowDeleted() {
        return showDeleted.isSelected();
    }

    /**
     * Updates the label for the selected cumulative fraction of all POIs.
     *
     * @param part numerator out of ten; ten represents all POIs
     */
    public void setShowAllPart(int part) {
        showAllMenu.setText(showAllLabel(part));
    }

    /** Formats the selected fraction for the Show menu. */
    private String showAllLabel(int part) {
        return part == 10 ? "Show (All POI)" : "Show (" + part + "/10)";
    }

    /** Creates an action button. */
    private Button button(String text, Runnable action) {
        Button button = new Button(text);
        button.setOnAction(event -> action.run());
        return button;
    }

    /** Adds a labeled group to the wrapping control row. */
    private void addGroup(FlowPane row, String title, Node... controls) {
        Label label = new Label(title);
        label.setStyle("-fx-font-weight: bold;");
        row.getChildren().add(label);
        row.getChildren().addAll(controls);
    }
}
