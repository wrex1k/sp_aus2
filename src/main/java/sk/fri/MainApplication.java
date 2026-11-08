package sk.fri;

import sk.fri.tester.CsvPoint;
import sk.fri.tester.Area;

import sk.fri.tester.TreeBenchmark;
import sk.fri.tester.TreeTester;

import java.util.List;

import static sk.fri.tester.LoadCSV.loadTestPoints;
import static sk.fri.tester.LoadCSV.loadTestAreas;

public class MainApplication {
    public static void main(String[] args) {
        List<CsvPoint> testPoints = loadTestPoints();
        List<Area> testAreas = loadTestAreas();

        TreeTester.testTrees(testPoints, testAreas);
        TreeBenchmark.compareStructures(testPoints, testAreas);
    }
}
