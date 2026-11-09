package sk.fri.poi.view;

import javafx.concurrent.Worker;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import sk.fri.poi.model.Poi;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Displays the current POIs on an interactive OpenStreetMap map. */
public final class PoiMapView extends StackPane {
    private final WebEngine engine;
    private final Label coordinates = new Label("Latitude: -  Longitude: -");
    private final Label mapError = new Label();
    private final Predicate<Poi> isDeleted;
    private final Consumer<Poi> onSelect;
    private final BiConsumer<Double, Double> onAdd;
    private List<Poi> items = List.of();
    private Poi selected;
    private Set<String> checkedIds = Set.of();
    private boolean ready;

    /**
     * Creates the map and its restricted selection and location callbacks.
     *
     * @param isDeleted reports soft-deleted records
     * @param onSelect selects a displayed POI
     * @param onAdd opens the POI form at a map position
     */
    public PoiMapView(Predicate<Poi> isDeleted, Consumer<Poi> onSelect,
                      BiConsumer<Double, Double> onAdd) {
        this.isDeleted = isDeleted;
        this.onSelect = onSelect;
        this.onAdd = onAdd;
        setMinSize(220, 220);
        WebView web = new WebView();
        engine = web.getEngine();
        engine.setUserAgent(engine.getUserAgent() + " POIManager/1.0");
        coordinates.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-padding: 5;");
        coordinates.setMouseTransparent(true);
        mapError.setWrapText(true);
        mapError.setStyle("-fx-background-color: white; -fx-text-fill: firebrick; -fx-padding: 8;");
        mapError.setVisible(false);
        StackPane.setAlignment(coordinates, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(coordinates, new Insets(0, 5, 25, 0));
        StackPane.setAlignment(mapError, Pos.TOP_CENTER);
        getChildren().addAll(web, coordinates, mapError);

        engine.setOnAlert(event -> handleMapEvent(event.getData()));
        engine.getLoadWorker().stateProperty().addListener((observable, oldState, state) -> {
            if (state == Worker.State.SUCCEEDED) {
                ready = Boolean.TRUE.equals(engine.executeScript("typeof updatePois === 'function'"));
                if (ready) {
                    updateMarkers();
                } else {
                    showMapError("Map could not be loaded. Check your internet connection and restart.");
                }
            } else if (state == Worker.State.FAILED) {
                showMapError("Map could not be loaded. Check your internet connection and restart.");
            }
        });
        engine.loadContent(MAP_HTML);
    }

    /**
     * Updates marker positions and colors without changing map zoom or position.
     *
     * @param items records displayed in the table
     * @param selected focused row, or null
     * @param checkedIds IDs selected for bulk operations
     */
    public void showPois(List<Poi> items, Poi selected, Set<String> checkedIds) {
        this.items = List.copyOf(items);
        this.selected = selected;
        this.checkedIds = Set.copyOf(checkedIds);
        updateMarkers();
    }

    /** Sends only validated numeric coordinates, indices, and color codes to JavaScript. */
    private void updateMarkers() {
        if (!ready) {
            return;
        }
        StringBuilder script = new StringBuilder("updatePois([");
        for (int index = 0; index < items.size(); index++) {
            if (index > 0) {
                script.append(',');
            }
            Poi poi = items.get(index);
            int color = poi.equals(selected) || checkedIds.contains(poi.id()) ? 2 : isDeleted.test(poi) ? 1 : 0;
            script.append('[').append(poi.latitude()).append(',').append(poi.longitude())
                    .append(',').append(index).append(',').append(color).append(']');
        }
        script.append("])");
        engine.executeScript(script.toString());
    }

    /** Handles only the known map events and validates their parameters. */
    private void handleMapEvent(String message) {
        String[] parts = message.split(":", -1);
        try {
            if (parts.length == 2 && parts[0].equals("select")) {
                int index = Integer.parseInt(parts[1]);
                if (index >= 0 && index < items.size()) {
                    Poi poi = items.get(index);
                    Platform.runLater(() -> onSelect.accept(poi));
                }
            } else if (message.equals("tiles-error")) {
                showMapError("Some map tiles could not be loaded. Check your internet connection.");
            } else if (parts.length == 3 && (parts[0].equals("move") || parts[0].equals("add"))) {
                double latitude = Double.parseDouble(parts[1]);
                double longitude = Double.parseDouble(parts[2]);
                if (Double.isFinite(latitude) && latitude >= -90 && latitude <= 90
                        && Double.isFinite(longitude) && longitude >= -180 && longitude <= 180) {
                    if (parts[0].equals("move")) {
                        coordinates.setText("Latitude: " + latitude + "  Longitude: " + longitude);
                    } else {
                        Platform.runLater(() -> onAdd.accept(latitude, longitude));
                    }
                }
            }
        } catch (NumberFormatException exception) {
            showMapError("The map sent an invalid event.");
        }
    }

    /** Displays a map-loading or communication error. */
    private void showMapError(String message) {
        mapError.setText(message);
        mapError.setVisible(true);
    }

    private static final String MAP_HTML = """
            <!doctype html>
            <html><head><meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
              integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=" crossorigin="">
            <style>html,body,#map{height:100%;margin:0} .leaflet-control-attribution{font-size:10px}</style>
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
              integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" crossorigin=""></script>
            </head><body><div id="map"></div>
            <script>
            if (typeof L !== 'undefined') {
              const map = L.map('map').setView([48.72, 19.48], 7);
              const tiles = L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19, attribution: '&copy; OpenStreetMap contributors'
              }).addTo(map);
              let tileErrorReported = false;
              tiles.on('tileerror', () => {
                if (!tileErrorReported) {
                  tileErrorReported = true;
                  alert('tiles-error');
                }
              });
              let markers = [], current = [], cycle = -1;
              const selectMarker = e => {
                L.DomEvent.stopPropagation(e);
                const point = map.latLngToContainerPoint(e.latlng);
                const hits = current.filter(p => map.latLngToContainerPoint([p[0], p[1]]).distanceTo(point) <= 10);
                if (hits.length) {
                  const previous = hits.findIndex(p => p[2] === cycle);
                  cycle = hits[(previous + 1) % hits.length][2];
                  alert('select:' + cycle);
                };
              }
              window.updatePois = function (points) {
                markers.forEach(marker => map.removeLayer(marker));
                current = points;
                markers = points.map(p => {
                  const color = p[3] === 2 ? '#ff8c00' : p[3] === 1 ? '#d02020' : '#4682b4';
                  const marker = L.circleMarker([p[0], p[1]], {radius: 6, color: color,
                    fillColor: color, fillOpacity: 1, weight: 1}).addTo(map);
                  marker.on('click', selectMarker);
                  return marker;
                });
              };
              map.on('click', e => {
                const point = e.latlng.wrap();
                alert('add:' + point.lat + ':' + point.lng);
              });
              map.on('mousemove', e => {
                const point = e.latlng.wrap();
                alert('move:' + point.lat + ':' + point.lng);
              });
            }
            </script></body></html>
            """;
}
