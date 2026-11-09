package sk.fri;

import javafx.application.Application;
import sk.fri.poi.controller.PoiManagerController;
import sk.fri.poi.model.PoiManagerModel;
import sk.fri.poi.model.SpatialStructures;
import sk.fri.poi.view.ConsoleView;
import sk.fri.poi.view.PoiManagerView;
import sk.fri.poi.view.PoiManagerFxApplication;

import java.nio.file.Path;

/**
 * Starts the JavaFX version of POI Manager.
 */
public class MainApplication {
    /**
     * Launches the JavaFX application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        /*Application.launch(PoiManagerFxApplication.class, args);*/

        PoiManagerView view = new ConsoleView();
        SpatialStructures structure = view.chooseStructure();
        Path csvPath = Path.of(view.askCsvPath());
        PoiManagerModel model = new PoiManagerModel(structure);
        PoiManagerController controller = new PoiManagerController(model, view, csvPath);
        if (controller.loadAtStartup()) {
            controller.run();
        }
    }
}
