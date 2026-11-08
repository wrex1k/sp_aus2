package sk.fri.tester;

/*import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for loading test points and areas from CSV files.
 */
public class LoadCSV {
    /**
     * Maximum number of points to load from the CSV file.
     */
    public static final int MAX_POINTS = 10_000;
    public static final int MAX_AREAS = 1000;

    private static final String CSV_PATH = "/csv/slovakia_nodes.csv";
    private static final String TEST_AREAS_CSV_PATH = "/csv/testovacie_oblasti.csv";

    private static InputStream openCsvResource(String path) {
        InputStream inputStream = LoadCSV.class.getResourceAsStream(path);
        if (inputStream == null) {
            throw new IllegalStateException("CSV resource not found: " + path);
        }
        return inputStream;
    }

    public static List<CsvPoint> loadTestPoints() {
        List<CsvPoint> testPoints = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(openCsvResource(CSV_PATH), StandardCharsets.UTF_8))) {
            reader.readLine();
            String line;
            while (testPoints.size() < MAX_POINTS && (line = reader.readLine()) != null) {
                String[] columns = line.split(";", -1);
                testPoints.add(new CsvPoint(
                        Long.parseLong(columns[0]),
                        Double.parseDouble(columns[1]),
                        Double.parseDouble(columns[2])
                ));
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        System.out.println("successfully imported: " + testPoints.size() + " test points | point example: " + testPoints.getFirst().toString());
        return testPoints;
    }

    public static List<Area> loadTestAreas() {
        List<Area> testAreas = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(openCsvResource(TEST_AREAS_CSV_PATH), StandardCharsets.UTF_8))) {
            reader.readLine();

            String line;
            while (testAreas.size() < MAX_AREAS && (line = reader.readLine()) != null) {
                String[] columns = line.split(";", -1);
                testAreas.add(new Area(
                        Double.parseDouble(columns[1]),
                        Double.parseDouble(columns[2]),
                        Double.parseDouble(columns[3]),
                        Double.parseDouble(columns[4])
                ));
            }
        } catch (IOException exception) {
            throw new RuntimeException("Unable to read interval test areas.", exception);
        }
        System.out.println("successfully imported: " + testAreas.size() + " test areas | area example: " + testAreas.getFirst().toString());
        return testAreas;
    }
}
