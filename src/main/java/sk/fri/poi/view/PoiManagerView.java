package sk.fri.poi.view;

import sk.fri.poi.model.Poi;
import sk.fri.poi.model.SpatialStructures;

import java.util.List;

/**
 * Defines the user interaction required by the POI Manager controller.
 */
public interface PoiManagerView {
    /**
     * Holds values entered for a new POI.
     *
     * @param name POI name
     * @param description POI description
     * @param latitude latitude in decimal degrees
     * @param longitude longitude in decimal degrees
     * @param address optional address
     */
    record CreateInput(String name, String description, double latitude, double longitude, String address) {
    }

    /**
     * Holds optional replacement values entered while editing a POI.
     *
     * @param name replacement name
     * @param description replacement description
     * @param latitude replacement latitude
     * @param longitude replacement longitude
     * @param address replacement address
     */
    record EditInput(String name, String description, Double latitude, Double longitude, String address) {
    }

    /**
     * Prompts for a spatial tree implementation.
     *
     * @return the selected tree
     */
    SpatialStructures chooseStructure();

    /**
     * Prompts for the CSV file path.
     *
     * @return the selected path
     */
    String askCsvPath();

    /**
     * Displays the menu and reads the selected option.
     *
     * @return the selected option
     */
    int showMenu();

    /**
     * Prompts for a latitude.
     *
     * @return latitude in decimal degrees
     */
    double askLatitude();

    /**
     * Prompts for a longitude.
     *
     * @return longitude in decimal degrees
     */
    double askLongitude();

    /**
     * Prompts for a search radius.
     *
     * @return radius in meters
     */
    double askRadius();

    /**
     * Prompts for a name search string.
     *
     * @return search text
     */
    String askSearchText();

    /**
     * Prompts for rectangular area boundaries.
     *
     * @return bounds ordered as minimum latitude, minimum longitude, maximum latitude, maximum longitude
     */
    double[] askArea();

    /**
     * Prompts for values to create a POI.
     *
     * @return entered POI values
     */
    CreateInput askCreateInput();

    /**
     * Prompts for optional updated values for a POI.
     *
     * @param current current POI values
     * @return entered replacement values
     */
    EditInput askEditInput(Poi current);

    /**
     * Prompts for a count of test POIs.
     *
     * @return requested count
     */
    int askGeneratedCount();

    /**
     * Displays candidates and prompts for one selection.
     *
     * @param candidates POIs to choose from
     * @return selected index, or -1 when cancelled
     */
    int selectPoi(List<Poi> candidates);

    /**
     * Displays a list of POIs.
     *
     * @param pois POIs to display
     */
    void showPois(List<Poi> pois);

    /**
     * Displays a message to the user.
     *
     * @param message message to display
     */
    void showMessage(String message);
}
