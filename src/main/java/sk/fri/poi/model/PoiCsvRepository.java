package sk.fri.poi.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            throw new IOException("CSV file does not exist: " + path.toAbsolutePath());
        }
        if (!Files.isRegularFile(path)) {
            throw new IOException("CSV path is not a regular file: " + path.toAbsolutePath());
        }
        if (!Files.isReadable(path)) {
            throw new IOException("CSV file is not readable: " + path.toAbsolutePath());
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header != null && !header.isEmpty() && header.charAt(0) == '\uFEFF') {
                header = header.substring(1);
            }
            if (!HEADER.equals(header)) {
                throw new IOException("Invalid CSV header in " + path.toAbsolutePath()
                        + " at line 1. Expected: " + HEADER
                        + "; found: " + (header == null ? "<empty file>" : quote(header)));
            }

            List<Poi> pois = new ArrayList<>();
            Set<String> ids = new HashSet<>();
            String line;
            int rowNumber = 1;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (line.isBlank()) {
                    continue;
                }

                String[] columns = line.split(";", -1);
                if (columns.length != 8) {
                    throw new IOException("Invalid CSV in " + path.toAbsolutePath() + " at line " + rowNumber
                            + ": expected 8 semicolon-separated columns but found " + columns.length
                            + ". Row: " + quote(line));
                }
                if (!ids.add(columns[0])) {
                    throw new IOException("Duplicate POI ID in " + path.toAbsolutePath()
                            + " at line " + rowNumber + ": " + quote(columns[0]));
                }
                try {
                    pois.add(new Poi(
                            columns[0],
                            columns[1],
                            columns[2],
                            parseDouble(columns[3], "lat", path, rowNumber),
                            parseDouble(columns[4], "lon", path, rowNumber),
                            columns[5],
                            parseInstant(columns[6], "created_at", path, rowNumber),
                            parseInstant(columns[7], "updated_at", path, rowNumber)
                    ));
                } catch (IllegalArgumentException exception) {
                    throw new IOException("Invalid POI data in " + path.toAbsolutePath()
                            + " at line " + rowNumber + ": " + exception.getMessage()
                            + ". ID=" + quote(columns[0]) + ", name=" + quote(columns[1])
                            + ", lat=" + quote(columns[3]) + ", lon=" + quote(columns[4]), exception);
                }
            }
            return pois;
        } catch (IOException exception) {
            if (exception.getMessage() != null
                    && exception.getMessage().contains(path.toAbsolutePath().toString())) {
                throw exception;
            }
            throw new IOException("Could not read CSV file " + path.toAbsolutePath()
                    + ": " + exception.getMessage(), exception);
        }
    }

    private static double parseDouble(String value, String column, Path path, int row) throws IOException {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw invalidValue(column, value, path, row, "expected a decimal number", exception);
        }
    }

    private static Instant parseInstant(String value, String column, Path path, int row) throws IOException {
        try {
            return Instant.parse(value);
        } catch (RuntimeException exception) {
            throw invalidValue(column, value, path, row, "expected an ISO-8601 timestamp", exception);
        }
    }

    private static IOException invalidValue(
            String column, String value, Path path, int row, String expected, Exception cause) {
        return new IOException("Invalid CSV value in " + path.toAbsolutePath() + " at line " + row
                + ", column '" + column + "': " + quote(value) + " (" + expected + ").", cause);
    }

    private static String quote(String value) {
        String visible = value.length() > 300 ? value.substring(0, 300) + "..." : value;
        return "\"" + visible.replace("\"", "\"\"") + "\"";
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
