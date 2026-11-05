import DataTypes.BasicData;
import DataTypes.Point;
import KDtree.KDtree;
import Test.Measuring;

import java.util.concurrent.ThreadLocalRandom;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {


    public static void main(String[] args) {
        Measuring test = new Measuring();
        test.insert();
        test.test();

        //System.out.println("Pocet dat v štruktúre:" + test.countOfData());
    }
}