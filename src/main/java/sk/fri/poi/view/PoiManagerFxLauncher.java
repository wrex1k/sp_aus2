package sk.fri.poi.view;

import javafx.application.Application;

/** Entry point used to launch the JavaFX POI Manager. */
public final class PoiManagerFxLauncher {
    private PoiManagerFxLauncher() {
    }

    public static void main(String[] args) {
        Application.launch(PoiManagerFxApplication.class, args);
    }
}
