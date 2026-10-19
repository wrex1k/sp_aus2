package sk.fri;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class TreeTester {
    private static final int OPERATIONS = 1_000_000;
    private static final int RANGE = 100;

    private record SensorReading(int temperature, int humidity) {
        Point<Integer> toPoint() {
            return new Point<>(temperature, humidity);
        }
    }

    public static void main(String[] args) {
        testTree("KDTree", new KDTree<>(SensorReading::toPoint, Integer::compare));
        testTree("QuadTree", new QuadTree<>(SensorReading::toPoint, Integer::compare));
    }

    private static void testTree(String name, SpatialTree<SensorReading, Integer> tree) {
        Set<Point<Integer>> reference = new HashSet<>();
        Random random = new Random(101);

        int operations = 0;

        while (operations < OPERATIONS) {
            int temperature = random.nextInt(RANGE);
            int humidity = random.nextInt(RANGE);
            Point<Integer> point = new Point<>(temperature, humidity);

            if (random.nextBoolean()) {
                boolean treeResult = tree.insert(new SensorReading(temperature, humidity));
                boolean referenceResult = reference.add(point);

                if (treeResult != referenceResult) {
                    throw new AssertionError("Insert failed at operation " + operations);
                }
            } else {
                boolean treeResult = tree.find(point).isPresent();
                boolean referenceResult = reference.contains(point);

                if (treeResult != referenceResult) {
                    throw new AssertionError("Find failed at operation " + operations);
                }
            }

            operations++;
        }

        System.out.println(
                name + ": passed " + operations
                        + " operations; points: " + reference.size()
        );
    }
}