package sk.fri;

import sk.fri.poi.controller.PoiManagerController;
import sk.fri.poi.model.PoiManagerModel;
import sk.fri.poi.model.SpatialStructures;
import sk.fri.poi.view.ConsoleView;
import sk.fri.poi.view.PoiManagerView;

import java.nio.file.Path;

/**
 * Starts the console version of POI Manager.
 */
public class MainApplication {
    /**
     * Starts the console application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
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
