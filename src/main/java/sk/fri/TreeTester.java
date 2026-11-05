package sk.fri;

import sk.fri.adapters.AlwexQuadTreeAdapter;
import sk.fri.adapters.JtsKDAdapter;
import sk.fri.adapters.KDAdapter;
import sk.fri.adapters.RyanQuadTreeAdapter;
import sk.fri.adapters.TinSpinKDAdapter;
import sk.fri.adapters.VarunPantQuadTreeAdapter;
import sk.fri.structures.KDTree;
import sk.fri.structures.QuadKDTree;
import sk.fri.structures.QuadTree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

public class TreeTester {
    private static final int DATA_POINTS = 10_000;
    private static final int WARMUP_RUNS = 3;
    private static final int MEASURED_RUNS = 5;
    private static final int RANGE_QUERIES_PER_RUN = 100;
    private static final String CSV_PATH = "/csv/slovakia_nodes.csv";
    private static final String RANGE_QUERY_CSV_PATH = "/csv/testovacie_oblasti.csv";


    private static final HashMap<Long, HashMap<String, Integer>> csvPointHashMap = new HashMap<>();

    private record CsvPoint(long id, double lat, double lon) {
        Point<Double> toPoint() {
            return new Point<>(lat, lon);
        }
    }

    private record RangeQuery(long id, double minLat, double minLon, double maxLat, double maxLon) {
        boolean contains(CsvPoint point) {
            return point.lat() >= minLat && point.lat() <= maxLat
                    && point.lon() >= minLon && point.lon() <= maxLon;
        }
    }

    private record RangeSearchCase(
            String name,
            SpatialTree<CsvPoint, Double> tree
    ) {
    }

    private static final class Benchmark {
        private final String name;
        private final Supplier<SpatialTree<CsvPoint, Double>> treeFactory;
        private final List<Long> insertTimes = new ArrayList<>();
        private final List<Long> findTimes = new ArrayList<>();
        private final List<Long> rangeSearchTimes = new ArrayList<>();

        private Benchmark(String name, Supplier<SpatialTree<CsvPoint, Double>> treeFactory) {
            this.name = name;
            this.treeFactory = treeFactory;
        }
    }

    public static void main(String[] args) {
        List<CsvPoint> data = loadCsvData();
        List<RangeQuery> rangeQueries = loadRangeQueries();
        System.out.println("Loaded CSV points: " + data.size());
        System.out.println("Loaded interval test areas: " + rangeQueries.size());
        verifyRangeSearches(data, rangeQueries);
        compareStructures(data, selectRangeQueries(rangeQueries));

        csvPointHashMap.forEach((id, results) ->
                System.out.println(id + " " + results.entrySet().stream()
                        .map(e -> "[" + e.getKey() + ", " + e.getValue() + "]").toList())
        );
    }

    private static List<CsvPoint> loadCsvData() {
        List<CsvPoint> data = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        openCsvResource(CSV_PATH),
                        StandardCharsets.UTF_8
                ))) {
            reader.readLine();
            String line;
            while (data.size() < DATA_POINTS && (line = reader.readLine()) != null) {
                String[] columns = line.split(";", -1);
                data.add(new CsvPoint(
                        Long.parseLong(columns[0]),
                        Double.parseDouble(columns[1]),
                        Double.parseDouble(columns[2])
                ));
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        return data;
    }

    private static List<RangeQuery> loadRangeQueries() {
        List<RangeQuery> rangeQueries = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        openCsvResource(RANGE_QUERY_CSV_PATH),
                        StandardCharsets.UTF_8
                ))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(";", -1);
                rangeQueries.add(new RangeQuery(
                        Long.parseLong(columns[0]),
                        Double.parseDouble(columns[1]),
                        Double.parseDouble(columns[2]),
                        Double.parseDouble(columns[3]),
                        Double.parseDouble(columns[4])
                ));
            }
        } catch (IOException exception) {
            throw new RuntimeException("Unable to read interval test areas.", exception);
        }
        return rangeQueries;
    }

    private static InputStream openCsvResource(String path) {
        InputStream inputStream = TreeTester.class.getResourceAsStream(path);
        if (inputStream == null) {
            throw new IllegalStateException("CSV resource not found: " + path);
        }
        return inputStream;
    }

    private static void verifyRangeSearches(List<CsvPoint> data, List<RangeQuery> rangeQueries) {
        List<RangeSearchCase> cases = List.of(
                new RangeSearchCase("My KDTree",
                        new KDTree<>(CsvPoint::toPoint, Double::compare)),
                new RangeSearchCase("My QuadTree",
                        new QuadTree<>(CsvPoint::toPoint, Double::compare)),
                new RangeSearchCase("My Quad-KDTree",
                        new QuadKDTree<>(CsvPoint::toPoint, Double::compare)),
                new RangeSearchCase("Existing external KDTree",
                        new KDAdapter<>(CsvPoint::toPoint, Double::doubleValue)),
                new RangeSearchCase("TinSpin KDTree",
                        new TinSpinKDAdapter<>(CsvPoint::toPoint, Double::doubleValue)),
                new RangeSearchCase("JTS KDTree",
                        new JtsKDAdapter<>(CsvPoint::toPoint, Double::doubleValue)),
                new RangeSearchCase("Varun Pant QuadTree",
                        new VarunPantQuadTreeAdapter<>(CsvPoint::toPoint, Double::doubleValue)),
                new RangeSearchCase("Alwex QuadTree",
                        new AlwexQuadTreeAdapter<>(CsvPoint::toPoint, Double::doubleValue)),
                new RangeSearchCase("Ryan Pelletier QuadTree",
                        new RyanQuadTreeAdapter<>(CsvPoint::toPoint, Double::doubleValue))
        );
        for (RangeSearchCase testCase : cases) {
            verifyRangeSearch(testCase.name(), testCase.tree(), data, rangeQueries);
        }
        System.out.println("Range-search checks passed for all spatial trees.");
    }

    private static List<RangeQuery> selectRangeQueries(List<RangeQuery> rangeQueries) {
        int queryCount = Math.min(RANGE_QUERIES_PER_RUN, rangeQueries.size());
        List<RangeQuery> selected = new ArrayList<>(queryCount);
        for (int index = 0; index < queryCount; index++) {
            int queryIndex = index * rangeQueries.size() / queryCount;
            selected.add(rangeQueries.get(queryIndex));
        }
        return selected;
    }

    private static void verifyRangeSearch(
            String name,
            SpatialTree<CsvPoint, Double> tree,
            List<CsvPoint> data,
            List<RangeQuery> rangeQueries
    ) {

        List<CsvPoint> inserted = data.subList(0, Math.min(1_000, data.size()));
        for (CsvPoint point : inserted) {
            tree.insert(point);
        }
        Set<CsvPoint> insertedPoints = new HashSet<>(inserted);

        for (RangeQuery query : rangeQueries) {
            Set<CsvPoint> expected = new HashSet<>();
            for (CsvPoint point : inserted) {
                if (query.contains(point)) {
                    expected.add(point);
                }
            }

            List<CsvPoint> results = tree.rangeSearch(
                    new Point<>(query.minLat(), query.minLon()),
                    new Point<>(query.maxLat(), query.maxLon())
            );
            if (!results.isEmpty()) {
                HashMap<String, Integer> arr = new HashMap<>();
                arr.put(name, results.toArray().length);

                csvPointHashMap.computeIfAbsent(query.id(), id -> new HashMap<>())
                        .put(name, results.size());

            }

            Set<CsvPoint> uniqueResults = new HashSet<>(results);
            if (results.size() != expected.size()
                    || uniqueResults.size() != results.size()
                    || !uniqueResults.equals(expected)
                    || results.stream().anyMatch(point ->
                    !insertedPoints.contains(point) || !query.contains(point))) {
                throw new AssertionError(name + " returned incorrect results for interval "
                        + query.id() + ": expected " + expected.size() + " points, got " + results.size());
            }
        }
    }

    private static void compareStructures(List<CsvPoint> data, List<RangeQuery> rangeQueries) {
        List<Benchmark> benchmarks = List.of(
                new Benchmark("My KDTree", () -> new KDTree<>(CsvPoint::toPoint, Double::compare)),
                new Benchmark("My QuadTree", () -> new QuadTree<>(CsvPoint::toPoint, Double::compare)),
                new Benchmark("My Quad-KDTree", () -> new QuadKDTree<>(CsvPoint::toPoint, Double::compare)),
                new Benchmark("Existing external KDTree", () -> new KDAdapter<>(CsvPoint::toPoint, Double::doubleValue)),
                new Benchmark("TinSpin KDTree", () -> new TinSpinKDAdapter<>(CsvPoint::toPoint, Double::doubleValue)),
                new Benchmark("JTS KDTree", () -> new JtsKDAdapter<>(CsvPoint::toPoint, Double::doubleValue)),
                new Benchmark("Varun Pant QuadTree", () -> new VarunPantQuadTreeAdapter<>(CsvPoint::toPoint, Double::doubleValue)),
                new Benchmark("Alwex QuadTree", () -> new AlwexQuadTreeAdapter<>(CsvPoint::toPoint, Double::doubleValue)),
                new Benchmark("Ryan Pelletier QuadTree", () -> new RyanQuadTreeAdapter<>(CsvPoint::toPoint, Double::doubleValue))
        );

        for (int run = 0; run < WARMUP_RUNS; run++) {
            runAll(benchmarks, data, rangeQueries, run, false);
        }
        for (int run = 0; run < MEASURED_RUNS; run++) {
            runAll(benchmarks, data, rangeQueries, run + WARMUP_RUNS, true);
        }

        System.out.println("Median time across " + MEASURED_RUNS + " runs (" + data.size()
                + " inserts, " + data.size() + " finds and " + rangeQueries.size()
                + " range searches per run)");
        System.out.printf("%-30s %14s %14s %18s %14s%n",
                "Structure", "Insert (ms)", "Find (ms)", "Range search (ms)", "Total (ms)");
        for (Benchmark benchmark : benchmarks) {
            double insertMillis = medianMillis(benchmark.insertTimes);
            double findMillis = medianMillis(benchmark.findTimes);
            double rangeSearchMillis = medianMillis(benchmark.rangeSearchTimes);
            System.out.printf(
                    "%-30s %14.3f %14.3f %18.3f %14.3f%n",
                    benchmark.name,
                    insertMillis,
                    findMillis,
                    rangeSearchMillis,
                    insertMillis + findMillis + rangeSearchMillis
            );
        }
    }

    private static void runAll(
            List<Benchmark> benchmarks,
            List<CsvPoint> data,
            List<RangeQuery> rangeQueries,
            int run,
            boolean measure
    ) {
        for (int offset = 0; offset < benchmarks.size(); offset++) {
            Benchmark benchmark = benchmarks.get((run + offset) % benchmarks.size());
            runOnce(benchmark, data, rangeQueries, measure);
        }
    }

    private static void runOnce(
            Benchmark benchmark,
            List<CsvPoint> data,
            List<RangeQuery> rangeQueries,
            boolean measure
    ) {
        SpatialTree<CsvPoint, Double> tree = benchmark.treeFactory.get();

        long insertStart = System.nanoTime();
        for (CsvPoint point : data) {
            if (!tree.insert(point)) {
                throw new AssertionError(benchmark.name + " rejected an inserted point: " + point);
            }
        }
        long insertTime = System.nanoTime() - insertStart;

        long findStart = System.nanoTime();
        for (CsvPoint point : data) {
            Optional<List<CsvPoint>> values = tree.find(point.toPoint());
            if (values.isEmpty() || !values.get().contains(point)) {
                throw new AssertionError(benchmark.name + " could not find an inserted point: " + point);
            }
        }
        long findTime = System.nanoTime() - findStart;

        long rangeSearchStart = System.nanoTime();
        for (RangeQuery query : rangeQueries) {
            tree.rangeSearch(
                    new Point<>(query.minLat(), query.minLon()),
                    new Point<>(query.maxLat(), query.maxLon())
            );
        }
        long rangeSearchTime = System.nanoTime() - rangeSearchStart;

        if (measure) {
            benchmark.insertTimes.add(insertTime);
            benchmark.findTimes.add(findTime);
            benchmark.rangeSearchTimes.add(rangeSearchTime);
        }
    }

    private static double medianMillis(List<Long> times) {
        List<Long> sortedTimes = times.stream().sorted().toList();
        return sortedTimes.get(sortedTimes.size() / 2) / 1_000_000.0;
    }

}
