package sk.fri.tester;

import sk.fri.structures.Point;
import sk.fri.structures.SpatialTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Measures insertion, exact lookup and range search times for spatial trees. */
public class TreeBenchmark {

    private static final int WARMUP_RUNS = 2;
    private static final int MEASURED_RUNS = 5;

    /** Holds measured durations for each operation on one tree. */
    private record TreeTimes(
            String name,
            List<Long> insertTimes,
            List<Long> findTimes,
            List<Long> areaSearchTimes
    ) {
    }

    /** Holds operation durations from one benchmark run in nanoseconds. */
    private record RunTimes(long insertTime, long findTime, long areaSearchTime) {
    }

    /**
     * Runs the benchmark for each available structure and prints median timings.
     *
     * @param points points used for insertion and lookup
     * @param areas rectangular areas used for range searches
     * @throws AssertionError if an inserted point cannot be found
     */
    public static void compareStructures(List<CsvPoint> points, List<Area> areas) {
        List<TreeTimes> results = new ArrayList<>();
        List<SpatialTree<CsvPoint, Point<Double>>> trees = TreeTester.get2DTrees();

        for (int i = 0; i < trees.size(); i++) {
            String name = trees.get(i).getName();
            List<Long> insertTimes = new ArrayList<>();
            List<Long> findTimes = new ArrayList<>();
            List<Long> areaSearchTimes = new ArrayList<>();

            for (int run = 1; run < WARMUP_RUNS + MEASURED_RUNS; run++) {
                SpatialTree<CsvPoint, Point<Double>> tree = TreeTester.get2DTrees().get(i);
                System.out.println(name + " (run " + run + "/" + (WARMUP_RUNS + MEASURED_RUNS) + ")..");

                RunTimes times = runOnce(tree, points, areas);

                if (run >= WARMUP_RUNS) {
                    insertTimes.add(times.insertTime());
                    findTimes.add(times.findTime());
                    areaSearchTimes.add(times.areaSearchTime());
                }
            }

            results.add(new TreeTimes(name, insertTimes, findTimes, areaSearchTimes));
        }

        printResults(results, points.size(), areas.size());
    }

    /** Measures one run using a fresh tree. */
    private static RunTimes runOnce(
            SpatialTree<CsvPoint, Point<Double>> tree,
            List<CsvPoint> points,
            List<Area> areas
    ) {
        List<CsvPoint> insertedPoints = new ArrayList<>(points.size());
        long insertStart = System.nanoTime();

        for (CsvPoint point : points) {
            if (tree.insert(point)) {
                insertedPoints.add(point);
            }
        }

        long insertTime = System.nanoTime() - insertStart;
        System.out.println(tree.getName() + " inserted " + insertedPoints.size()
                + " points and rejected " + (points.size() - insertedPoints.size()) + " insertions");
        long findStart = System.nanoTime();

        for (CsvPoint point : insertedPoints) {
            Optional<List<CsvPoint>> values = tree.find(point.toPoint());

            if (values.isEmpty() || !values.get().contains(point)) {
                throw new AssertionError(
                        tree.getName() + " could not find an inserted point: " + point
                );
            }
        }

        long findTime = System.nanoTime() - findStart;
        long areaSearchStart = System.nanoTime();

        for (Area area : areas) {
            tree.rangeSearch(
                    new Point<>(area.minLat(), area.minLon()),
                    new Point<>(area.maxLat(), area.maxLon())
            ).orElseThrow();
        }

        long areaSearchTime = System.nanoTime() - areaSearchStart;

        return new RunTimes(insertTime, findTime, areaSearchTime);
    }

    /** Prints median timings and the workload size for each tree. */
    private static void printResults(List<TreeTimes> results, int pointCount, int areaCount) {
        System.out.println("Median time across " + MEASURED_RUNS + " runs ("
                + pointCount + " insertion attempts, finds for successfully inserted points and "
                + areaCount + " area searches per run). Find and area-search workloads may differ"
                + " between structures when insertions are rejected.");
        System.out.printf("%-30s %14s %14s %18s %14s%n",
                "Structure", "Insert (ms)", "Find (ms)", "Area search (ms)", "Total (ms)");

        for (TreeTimes result : results) {
            double insertMillis = medianMillis(result.insertTimes());
            double findMillis = medianMillis(result.findTimes());
            double areaSearchMillis = medianMillis(result.areaSearchTimes());

            System.out.printf(
                    "%-30s %14.3f %14.3f %18.3f %14.3f%n",
                    result.name(),
                    insertMillis,
                    findMillis,
                    areaSearchMillis,
                    insertMillis + findMillis + areaSearchMillis
            );
        }
    }

    /** Returns the middle sorted duration converted from nanoseconds to milliseconds. */
    private static double medianMillis(List<Long> times) {
        List<Long> sortedTimes = times.stream().sorted().toList();
        return sortedTimes.get(sortedTimes.size() / 2) / 1_000_000.0;
    }
}
