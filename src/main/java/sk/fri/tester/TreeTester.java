package sk.fri.tester;

import sk.fri.adapters.KDTree2DAdapter;
import sk.fri.adapters.kdAdapters.JtsKDAdapter;
import sk.fri.adapters.kdAdapters.KDAdapter;
import sk.fri.adapters.kdAdapters.TinSpinKDAdapter;
import sk.fri.adapters.quadAdapters.AlwexQuadTreeAdapter;
import sk.fri.adapters.quadAdapters.RyanQuadTreeAdapter;
import sk.fri.adapters.quadAdapters.VarunPantQuadTreeAdapter;
import sk.fri.structures.*;

import java.util.*;

public class TreeTester {

    public static Random random = new Random();

    static List<SpatialTree<CsvPoint, Point<Double>>> get2DTrees() {
        return List.of(
                new KDTree2DAdapter<>(new KDTree<>(CsvPoint::toKDPoint, Double::compare, 2)), //kdtree using 2d (kdpoint) (k=2)
                new QuadTree<>(CsvPoint::toPoint, Double::compare),
                new QuadKDTree<>(CsvPoint::toPoint, CsvPoint::toKDPoint, Double::compare), //quadtree using 2d (point) and 2d (kdpoint) (k=2)

                new KDAdapter<>(CsvPoint::toPoint, Double::doubleValue),
                new TinSpinKDAdapter<>(CsvPoint::toPoint, Double::doubleValue),
                new JtsKDAdapter<>(CsvPoint::toPoint, Double::doubleValue),
                new VarunPantQuadTreeAdapter<>(CsvPoint::toPoint, Double::doubleValue),
                new AlwexQuadTreeAdapter<>(CsvPoint::toPoint, Double::doubleValue),
                new RyanQuadTreeAdapter<>(CsvPoint::toPoint, Double::doubleValue)
        );
    }

    public static void testTrees(List<CsvPoint> points, List<Area> areas) {

        for (SpatialTree<CsvPoint, Point<Double>> tree : get2DTrees()) {
            verifyRandomInsertAndFind(tree, points);
            System.out.println("PASSED");
        }

        for (SpatialTree<CsvPoint, Point<Double>> tree : get2DTrees()) {
            verifyRangeSearch(tree, points, areas);
            System.out.println("PASSED");
        }

        System.out.println("all spatial tree tests passed successfully");
    }

    public static void verifyRandomInsertAndFind(
            SpatialTree<CsvPoint, Point<Double>> tree,
            List<CsvPoint> points
    ) {
        System.out.println("verifying insert and find for " + tree.getName() + "..");

        List<CsvPoint> insertedPoints = new ArrayList<>();
        int rejectedPoints = 0;

        for (int i = 0; i < points.size()/10; i++) {
            CsvPoint point = points.get(random.nextInt(points.size()));

            if (!tree.insert(point)) {
                rejectedPoints++;
                continue;
            }
            insertedPoints.add(point);

            if (i % 10 == 0) {
                CsvPoint expected = insertedPoints.get(
                        random.nextInt(insertedPoints.size())
                );

                List<CsvPoint> results = tree.find(expected.toPoint()).orElseThrow(
                        () -> new AssertionError(tree.getName() + " could not find " + expected)
                );
                if (!results.contains(expected)) {
                    throw new AssertionError(tree.getName() + " returned incorrect results for " + expected);
                }
            }
        }
        System.out.println(tree.getName() + " rejected " + rejectedPoints + " insertions");
    }

    private static void verifyRangeSearch(
            SpatialTree<CsvPoint, Point<Double>> tree,
            List<CsvPoint> points,
            List<Area> areas
    ) {
        System.out.println("verifying area search for " + tree.getName() + "..");

        List<CsvPoint> insertedPoints = new ArrayList<>();
        int rejectedPoints = 0;
        for (CsvPoint point : points) {
            if (tree.insert(point)) {
                insertedPoints.add(point);
            } else {
                rejectedPoints++;
            }
        }
        System.out.println(tree.getName() + " rejected " + rejectedPoints + " insertions");

        for (Area area : areas) {
            Set<CsvPoint> expectedPoints = new HashSet<>();

            for (CsvPoint point : insertedPoints) {
                if (area.contains(point)) {
                    expectedPoints.add(point);
                }
            }

            List<CsvPoint> results = tree.rangeSearch(
                    new Point<>(area.minLat(), area.minLon()),
                    new Point<>(area.maxLat(), area.maxLon())
            ).orElseThrow();

            if (results.size() != expectedPoints.size()
                    || !new HashSet<>(results).equals(expectedPoints)) {
                throw new AssertionError(tree.getName() + " returned incorrect results for interval, expected:"
                        + expectedPoints.size()
                        + " points, got " + results.size());
            }
        }
    }
}