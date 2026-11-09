package sk.fri.poi.view;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/** Contains the main window's grouped operation buttons. */
public final class PoiControlsView extends VBox {
    private final CheckBox showDeleted = new CheckBox("Show deleted");

    /**
     * Creates controls and connects them to the application actions.
     *
     * @param findAt exact position search
     * @param nearby nearby search
     * @param search name search
     * @param showAll show all POIs
     * @param create create a POI
     * @param edit edit a selected POI
     * @param delete delete a selected POI
     * @param generate generate test data
     * @param save save CSV
     * @param filterChanged apply the deleted-record filter
     */
    public PoiControlsView(Runnable findAt, Runnable nearby, Runnable search, Runnable showAll,
                           Runnable create, Runnable edit, Runnable delete, Runnable generate,
                           Runnable save, Runnable filterChanged) {
        setSpacing(4);
        setPadding(new Insets(8));
        showDeleted.setOnAction(event -> filterChanged.run());
        FlowPane groups = new FlowPane(12, 4);
        addGroup(groups, "Search", button("Find by GPS", findAt), button("Find nearby", nearby),
                button("Search by name", search), button("Show all", showAll));
        addGroup(groups, "Manage POIs", button("Add POI", create), button("Edit selected", edit),
                button("Delete selected", delete));
        addGroup(groups, "Data", button("Generate test POIs", generate), button("Save CSV", save), showDeleted);
        getChildren().add(groups);
    }

    /** Returns the current deleted-record filter state. */
    public boolean isShowDeleted() {
        return showDeleted.isSelected();
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
