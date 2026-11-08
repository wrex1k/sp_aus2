package sk.fri.poi.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes POI records in the application's semicolon-separated CSV format.
 */
final class PoiCsvRepository {
    private static final String HEADER = "id;name;description;lat;lon;address;created_at;updated_at";

    /**
     * Reads and validates all records from a CSV file.
     *
     * @param path CSV file path
     * @return the POIs stored in the file
     * @throws IOException if the file format or any record is invalid
     */
    List<Poi> read(Path path) throws IOException {
        if (!Files.exists(path)) {
            return List.of();
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (!HEADER.equals(header)) {
                throw new IOException("Invalid CSV header. Expected: " + HEADER);
            }

            List<Poi> pois = new ArrayList<>();
            String line;
            int rowNumber = 1;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (line.isBlank()) {
                    continue;
                }

                String[] columns = line.split(";", -1);
                if (columns.length != 8) {
                    throw new IOException("CSV row " + rowNumber + " must contain exactly 8 columns.");
                }
                try {
                    pois.add(new Poi(
                            columns[0],
                            columns[1],
                            columns[2],
                            Double.parseDouble(columns[3]),
                            Double.parseDouble(columns[4]),
                            columns[5],
                            Instant.parse(columns[6]),
                            Instant.parse(columns[7])
                    ));
                } catch (IllegalArgumentException exception) {
                    throw new IOException("Invalid data in CSV row " + rowNumber + ": " + exception.getMessage(), exception);
                }
            }
            return pois;
        }
    }

    /**
     * Writes POI records using the required CSV header and column order.
     *
     * @param path destination CSV file
     * @param pois POIs to write
     * @throws IOException if the file cannot be written
     */
    void write(Path path, List<Poi> pois) throws IOException {
        Path absolutePath = path.toAbsolutePath();
        Path parent = absolutePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(absolutePath, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();
            for (Poi poi : pois) {
                writer.write(String.join(";",
                        poi.id(),
                        poi.name(),
                        poi.description(),
                        Double.toString(poi.latitude()),
                        Double.toString(poi.longitude()),
                        poi.address(),
                        poi.createdAt().toString(),
                        poi.updatedAt().toString()
                ));
                writer.newLine();
            }
        }
    }
}
