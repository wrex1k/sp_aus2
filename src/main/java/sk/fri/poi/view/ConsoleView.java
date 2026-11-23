package sk.fri.poi.view;

import sk.fri.poi.model.Poi;
import sk.fri.poi.model.SpatialStructures;

import java.util.List;
import java.util.Scanner;

/**
 * Collects user input and displays POI Manager results in the console.
 */
public final class ConsoleView implements PoiManagerView {
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Prompts the user to select one of the available spatial structures.
     *
     * @return the selected tree implementation
     */
    public SpatialStructures chooseStructure() {
        while (true) {
            System.out.println("Choose a spatial structure:");
            System.out.println("1. KDTree");
            System.out.println("2. QuadTree");
            System.out.println("3. QuadKDTree");
            int choice = readInt("Choice: ");
            if (choice >= 1 && choice <= 3) {
                return switch (choice) {
                    case 1 -> SpatialStructures.KD_TREE;
                    case 2 -> SpatialStructures.QUAD_TREE;
                    default -> SpatialStructures.QUAD_KD_TREE;
                };
            }
            showMessage("Invalid choice.");
        }
    }

    /**
     * Prompts for the CSV path, using the default file when the input is empty.
     *
     * @return the selected CSV path
     */
    public String askCsvPath() {
        System.out.print("CSV file path (empty = src/main/resources/csv/slovakia_nodes.csv): ");
        String path = scanner.nextLine().trim();
        return path.isEmpty() ? "src/main/resources/csv/slovakia_nodes.csv" : path;
    }

    /**
     * Displays the main menu and reads the selected option.
     *
     * @return the selected menu option
     */
    public int showMenu() {
        System.out.println();
        System.out.println("1. Find POIs at a GPS position");
        System.out.println("2. Find POIs nearby");
        System.out.println("3. Create a POI");
        System.out.println("4. Edit a POI");
        System.out.println("5. Search names in an area");
        System.out.println("6. Delete a POI");
        System.out.println("7. Show all POIs");
        System.out.println("8. Generate test POIs");
        System.out.println("9. Save to CSV");
        System.out.println("0. Save and exit");
        System.out.println("10. Exit without saving");
        return readInt("Choice: ");
    }

    /**
     * Prompts for a latitude.
     *
     * @return the entered latitude
     */
    public double askLatitude() {
        return readDouble("Latitude: ");
    }

    /**
     * Prompts for a longitude.
     *
     * @return the entered longitude
     */
    public double askLongitude() {
        return readDouble("Longitude: ");
    }

    /**
     * Prompts for a search radius in meters.
     *
     * @return the entered radius
     */
    public double askRadius() {
        return readDouble("Radius in meters: ");
    }

    /**
     * Prompts for text to search for in POI names.
     *
     * @return the entered search text
     */
    public String askSearchText() {
        System.out.print("Text in POI name: ");
        return scanner.nextLine();
    }

    /**
     * Prompts for the four boundaries of a rectangular area.
     *
     * @return bounds ordered as minimum latitude, minimum longitude, maximum latitude, maximum longitude
     */
    public double[] askArea() {
        System.out.println("Enter the area boundaries:");
        double minLatitude = readDouble("Minimum latitude: ");
        double maxLatitude = readDouble("Maximum latitude: ");
        double minLongitude = readDouble("Minimum longitude: ");
        double maxLongitude = readDouble("Maximum longitude: ");
        return new double[]{minLatitude, minLongitude, maxLatitude, maxLongitude};
    }

    /**
     * Prompts for all fields required to create a POI.
     *
     * @return the entered POI values
     */
    public PoiManagerView.CreateInput askCreateInput() {
        String name;
        do {
            System.out.print("Name: ");
            name = scanner.nextLine();
        } while (name.isBlank());
        System.out.print("Description: ");
        String description = scanner.nextLine();
        double latitude = askLatitude();
        double longitude = askLongitude();
        System.out.print("Address: ");
        String address = scanner.nextLine();
        return new PoiManagerView.CreateInput(name, description, latitude, longitude, address);
    }

    /**
     * Prompts for optional replacement fields for an existing POI.
     *
     * @param current current POI values shown as defaults
     * @return the values entered by the user
     */
    public PoiManagerView.EditInput askEditInput(Poi current) {
        System.out.println("Leave a value empty to keep it; enter '-' to clear description or address.");
        System.out.print("Name [" + current.name() + "]: ");
        String name = scanner.nextLine();
        System.out.print("Description [" + current.description() + "]: ");
        String description = scanner.nextLine();
        Double latitude = readOptionalDouble("Latitude [" + current.latitude() + "]: ");
        Double longitude = readOptionalDouble("Longitude [" + current.longitude() + "]: ");
        System.out.print("Address [" + current.address() + "]: ");
        String address = scanner.nextLine();
        return new PoiManagerView.EditInput(
                name.isBlank() ? null : name,
                description.isBlank() ? null : description.equals("-") ? "" : description,
                latitude,
                longitude,
                address.isBlank() ? null : address.equals("-") ? "" : address
        );
    }

    /**
     * Prompts for the number of test POIs to generate.
     *
     * @return the entered count
     */
    public int askGeneratedCount() {
        return readInt("Number of points: ");
    }

    /**
     * Displays POI candidates and prompts the user to select one.
     *
     * @param candidates POIs to choose from
     * @return selected POI index, or -1 if the user cancels or there are no candidates
     */
    public int selectPoi(List<Poi> candidates) {
        if (candidates.isEmpty()) {
            showMessage("No POIs found.");
            return -1;
        }
        showPois(candidates);
        System.out.println("Enter a POI number (0 = cancel):");
        while (true) {
            int choice = readInt("Selection: ");
            if (choice == 0) {
                return -1;
            }
            if (choice > 0 && choice <= candidates.size()) {
                return choice - 1;
            }
            showMessage("Invalid choice.");
        }
    }

    /**
     * Displays a list of POIs or a message when it is empty.
     *
     * @param pois POIs to display
     */
    public void showPois(List<Poi> pois) {
        if (pois.isEmpty()) {
            showMessage("No POIs found.");
            return;
        }
        for (Poi poi : pois) {
            showPoi(poi);
        }
    }

    /**
     * Displays a simple informational or error message.
     *
     * @param message message to display
     */
    public void showMessage(String message) {
        System.out.println(message);
    }

    /**
     * Displays all fields of one POI.
     *
     * @param poi POI to display
     */
    private void showPoi(Poi poi) {
        System.out.println("ID: " + poi.id());
        System.out.println("Name: " + poi.name());
        System.out.println("Description: " + poi.description());
        System.out.println("Latitude: " + poi.latitude());
        System.out.println("Longitude: " + poi.longitude());
        System.out.println("Address: " + poi.address());
        System.out.println("Created at: " + poi.createdAt());
        System.out.println("Updated at: " + poi.updatedAt());
        System.out.println();
    }

    /**
     * Reads an integer, repeating the prompt for invalid input.
     *
     * @param prompt prompt to display
     * @return the entered integer
     */
    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException exception) {
                showMessage("Enter a whole number.");
            }
        }
    }

    /**
     * Reads a decimal number, repeating the prompt for invalid input.
     *
     * @param prompt prompt to display
     * @return the entered number
     */
    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException exception) {
                showMessage("Enter a number.");
            }
        }
    }

    /**
     * Reads an optional decimal number.
     *
     * @param prompt prompt to display
     * @return the entered number, or null when left empty
     */
    private Double readOptionalDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException exception) {
                showMessage("Enter a number or leave the value empty.");
            }
        }
    }
}
