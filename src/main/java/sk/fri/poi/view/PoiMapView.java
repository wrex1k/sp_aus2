package sk.fri.poi.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import sk.fri.poi.model.Poi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Displays an offline map of Slovakia and the POIs in the current table. */
public final class PoiMapView extends StackPane {

    private static final double LAT = 48.68, LON = 19.70;
    private static final double MIN_ZOOM = 0.65, MAX_ZOOM = 24;
    private static final Color ACTIVE = Color.STEELBLUE;
    private static final Color DELETED = Color.INDIANRED;
    private static final Color SELECTED = Color.ORANGE;

    private final Canvas canvas = new Canvas();
    private final Label coordinates = new Label();

    private final Predicate<Poi> isDeleted;
    private final Consumer<Poi> onSelect;
    private final BiConsumer<Double, Double> onAdd;
    private final List<City> cities = List.of(
            new City("Bratislava", 48.1486, 17.1077, -57, -8),
            new City("Trnava", 48.3774, 17.5872, 6, -7),
            new City("Trenčín", 48.8945, 18.0444, -43, -8),
            new City("Nitra", 48.3064, 18.0764, 6, 13),
            new City("Žilina", 49.2231, 18.7394, 6, -8),
            new City("Martin", 49.0650, 18.9210, 6, 13),
            new City("Ružomberok", 49.0748, 19.3075, 6, -8),
            new City("Banská Bystrica", 48.7363, 19.1462, -88, 14),
            new City("Poprad", 49.0556, 20.2970, -31, -8),
            new City("Prešov", 48.9984, 21.2339, -44, -8),
            new City("Košice", 48.7164, 21.2611, 6, 13)
    );

    private final double[][] border;
    private List<Poi> points = new ArrayList<>();
    private Set<String> checkedIds = Set.of();
    private Poi selected;

    private double centerLat = LAT, centerLon = LON, zoom = 1;
    private double pressX, pressY, previousX = Double.NaN, previousY = Double.NaN;
    private int previousIndex = -1;
    private boolean dragging;

    /**
     * Creates the map and connects its selection and creation actions.
     *
     * @param isDeleted reports whether a POI is soft-deleted
     * @param onSelect selects a POI clicked on the map
     * @param onAdd creates a POI at the clicked latitude and longitude
     * @throws IllegalStateException if the offline border resource cannot be loaded
     */
    public PoiMapView(Predicate<Poi> isDeleted, Consumer<Poi> onSelect,
                      BiConsumer<Double, Double> onAdd) {
        this.isDeleted = isDeleted;
        this.onSelect = onSelect;
        this.onAdd = onAdd;

        try {
            border = loadBorder();
        } catch (IOException e) {
            throw new IllegalStateException("Could not load Slovakia map.", e);
        }

        setMinSize(220, 220);
        coordinates.setStyle("-fx-background-color: white; -fx-padding: 5;");

        StackPane.setAlignment(coordinates, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(coordinates, new Insets(7));

        getChildren().addAll(canvas, coordinates);

        canvas.setOnMouseMoved(e -> updateCoordinates(e.getX(), e.getY()));
        canvas.setOnMousePressed(e -> {
            pressX = e.getX();
            pressY = e.getY();
            dragging = false;
        });
        canvas.setOnMouseDragged(e -> {
            if (Math.hypot(e.getX() - pressX, e.getY() - pressY) > 4)
                dragging = true;
            updateCoordinates(e.getX(), e.getY());
        });
        canvas.setOnMouseReleased(e -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            if (dragging) {
                centerLon -= (e.getX() - pressX) / (scale() * lonScale());
                centerLat += (e.getY() - pressY) / scale();
                centerLat = Math.max(-85, Math.min(85, centerLat));
                previousIndex = -1;
                render();
            } else handleClick(e.getX(), e.getY());
        });
        canvas.setOnScroll(e -> {
            double[] anchor = unproject(e.getX(), e.getY());
            zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM,
                    zoom * (e.getDeltaY() > 0 ? 1.2 : 1 / 1.2)));
            centerLon = anchor[1] - (e.getX() - canvas.getWidth() / 2) / (scale() * lonScale());
            centerLat = anchor[0] + (e.getY() - canvas.getHeight() / 2) / scale();
            previousIndex = -1;
            render();
            e.consume();
        });
    }

    /**
     * Updates the POIs and selection displayed on the map.
     *
     * @param items POIs in the current table
     * @param selected focused POI, or null if none is selected
     * @param checkedIds IDs selected using the table checkboxes
     */
    public void showPois(List<Poi> items, Poi selected, Set<String> checkedIds) {
        points = new ArrayList<>(items);
        this.selected = selected;
        this.checkedIds = Set.copyOf(checkedIds);
        previousIndex = -1;
        render();
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());
        canvas.relocate(0, 0);
        render();
    }

    /** Draws the border, cities and visible POIs at the current zoom and center. */
    private void render() {
        double w = canvas.getWidth(), h = canvas.getHeight();
        if (w < 2 || h < 2) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web("#eaf1f3"));
        gc.fillRect(0, 0, w, h);

        gc.beginPath();
        gc.moveTo(x(border[0][0]), y(border[0][1]));
        for (int i = 1; i < border.length; i++)
            gc.lineTo(x(border[i][0]), y(border[i][1]));
        gc.closePath();
        gc.setFill(Color.web("#f4f7ed"));
        gc.fill();
        gc.setStroke(Color.web("#748874"));
        gc.setLineWidth(1.2);
        gc.stroke();

        gc.setFont(Font.font(11));
        for (City city : cities) {
            double cx = x(city.lon), cy = y(city.lat);
            gc.setFill(Color.web("#65716d"));
            gc.fillOval(cx - 2, cy - 2, 4, 4);
            gc.setFill(Color.web("#343c39"));
            gc.fillText(city.name, cx + city.dx, cy + city.dy);
        }

        for (Poi poi : points) {
            double px = x(poi.longitude()), py = y(poi.latitude());
            if (px < -10 || py < -10 || px > w + 10 || py > h + 10) continue;

            gc.setFill(poi.equals(selected) || checkedIds.contains(poi.id())
                    ? SELECTED : isDeleted.test(poi) ? DELETED : ACTIVE);
            gc.fillOval(px - 4, py - 4, 8, 8);
        }
    }

    /** Cycles through overlapping POIs or starts creation inside the border. */
    private void handleClick(double px, double py) {
        List<Integer> hits = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            Poi poi = points.get(i);
            if (Math.hypot(x(poi.longitude()) - px, y(poi.latitude()) - py) <= 10)
                hits.add(i);
        }

        if (!hits.isEmpty()) {
            boolean same = Math.hypot(px - previousX, py - previousY) <= 10;
            int previous = same ? hits.indexOf(previousIndex) : -1;
            int index = hits.get((previous + 1) % hits.size());
            previousX = px;
            previousY = py;
            previousIndex = index;
            onSelect.accept(points.get(index));
            return;
        }

        double[] position = unproject(px, py);
        if (contains(position[1], position[0]))
            onAdd.accept(position[0], position[1]);
    }

    /** Updates the displayed coordinates from a canvas position. */
    private void updateCoordinates(double px, double py) {
        double[] position = unproject(px, py);
        coordinates.setText("Latitude: " + position[0] + "  Longitude: " + position[1]);
    }

    /** Converts a canvas position to latitude and longitude. */
    private double[] unproject(double px, double py) {
        return new double[]{
                centerLat - (py - canvas.getHeight() / 2) / scale(),
                centerLon + (px - canvas.getWidth() / 2) / (scale() * lonScale())
        };
    }

    /** Converts longitude to a horizontal canvas position. */
    private double x(double lon) {
        return canvas.getWidth() / 2 + (lon - centerLon) * lonScale() * scale();
    }

    /** Converts latitude to a vertical canvas position. */
    private double y(double lat) {
        return canvas.getHeight() / 2 - (lat - centerLat) * scale();
    }

    /** Returns the current canvas scale in pixels per latitude degree. */
    private double scale() {
        return Math.min(
                canvas.getWidth() / (6.4 * lonScale()),
                canvas.getHeight() / 2.15
        ) * zoom;
    }

    /** Returns the longitude correction for the current center latitude. */
    private double lonScale() {
        return Math.cos(Math.toRadians(centerLat));
    }

    /**
     * Tests whether a geographic position is inside the Slovakia border polygon.
     *
     * @param lon longitude in decimal degrees
     * @param lat latitude in decimal degrees
     * @return true if the position is inside the polygon
     */
    public boolean contains(double lon, double lat) {
        boolean inside = false;
        for (int i = 0, j = border.length - 1; i < border.length; j = i++) {
            double[] a = border[i], b = border[j];
            if ((a[1] > lat) != (b[1] > lat)
                    && lon < (b[0] - a[0]) * (lat - a[1]) / (b[1] - a[1]) + a[0])
                inside = !inside;
        }
        return inside;
    }

    /** Loads longitude-latitude pairs from the bundled border resource. */
    private static double[][] loadBorder() throws IOException {
        InputStream input = PoiMapView.class.getResourceAsStream("/map/slovakia-border.txt");
        if (input == null) throw new IOException("Missing Slovakia map resource.");

        try (BufferedReader reader = new BufferedReader(
                new java.io.InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] values = line.split("\\s+");
                double[][] result = new double[values.length][2];
                for (int i = 0; i < values.length; i++) {
                    String[] pair = values[i].split(",", -1);
                    if (pair.length != 2) throw new IOException("Invalid map coordinate.");
                    result[i][0] = Double.parseDouble(pair[0]);
                    result[i][1] = Double.parseDouble(pair[1]);
                }
                if (result.length >= 4) return result;
            }
        } catch (NumberFormatException e) {
            throw new IOException("Invalid Slovakia map coordinates.", e);
        }
        throw new IOException("Slovakia border not found.");
    }

    /** Holds a city's coordinates and label offsets on the canvas. */
    private record City(String name, double lat, double lon, double dx, double dy) {}
}