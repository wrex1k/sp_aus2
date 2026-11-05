package Test;

import DataTypes.*;
import KDtree.KDtree;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

class KDtreeTest {

    int pocetOperacii = 1000;
    double minX = -250.0;
    double maxX = 250.0;
    double minY = -250.0;
    double maxY = 250.0;
    KDtree<BasicData, Point> tree;
    ArrayList<BasicData> data;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        tree = new KDtree<BasicData, Point>(2);
        data = new ArrayList<>()    ;
    }

    @org.junit.jupiter.api.Test
    void insert() {
        /*
        tester.insert(250.0, 250.0, 500.0, 500.0, new BasicData(250.0, 250.0, 500.0, 500.0) );
        tester.insert(250.0, 250.0, 0.0, 0.0, new BasicData(250.0, 250.0, 0.0, 0.0) );
        tester.insert(145.0, 125.0, 14.0, 16.0, new BasicData(145.0, 125.0, 14.0, 16.0)  );
        tester.insert(445.0, 125.0, 499.0, 499.0, new BasicData(445.0, 125.0, 499.0, 499.0) );
        tester.insert(75.0, 305.0, 90.0, 125.0, new BasicData(75.0, 305.0, 90.0, 125.0) );
        tester.insert(5.0, 5.0, 45.0, 45.0, new BasicData(5.0, 5.0, 45.0, 45.0) );
        tester.insert(275.0, 280.0, 300.0, 320.0, new BasicData(275.0, 280.0, 300.0, 320.0) );
        tester.insert(250.0, 250.0,0.0, 0.0, new BasicData(250.0, 250.0,0.0, 0.0 ));

         */
        /*
        tester.insert(250.0, 250.0, 500.0, 500.0, new BasicData(new Point(250.0, 250.0)));
        tester.insert(250.0, 250.0, 0.0, 0.0, new BasicData((new Point(250.0, 250.0))));
        tester.insert(350.0, 175.0, 14.0, 16.0, new BasicData((new Point(350.0, 175.0))));
        tester.insert(50.0, 200.0, 499.0, 499.0, new BasicData((new Point(50.0, 200.0))));
        tester.insert(70.0, 80.0, 90.0, 125.0, new BasicData((new Point(70.0, 80.0))));
        tester.insert(50.0, 90.0, 45.0, 45.0, new BasicData((new Point(50.0, 90.0))));
        tester.insert(10.0, 10.0, 300.0, 320.0, new BasicData((new Point(10.0, 10.0))));
        tester.insert(20.0, 250.0,0.0, 0.0, new BasicData((new Point(20.0, 250.0))));
        System.out.println("Number of data:" + tester.numberOfData());

         */

            Random random = new Random(1);
            int id = 1;
            /*
            for (int i = 0; i < pocetOperacii; i++) {
                id++;
                double x1 = (random.nextDouble(minX, maxX));
                double x2 = (random.nextDouble(minX, maxX));
                double y1 = (random.nextDouble(minY, maxY));
                double y2 = (random.nextDouble(minY, maxY));
                while ((x1 <= x2 && y1 >= y2) || (x2 <= x1 && y2 >= y1)) {
                    x1 = (random.nextDouble(minX, maxX));
                    x2 = (random.nextDouble(minX, maxX));
                    y1 = (random.nextDouble(minY, maxY));
                    y2 = (random.nextDouble(minY, maxY));
                }
                BasicData data1 = new BasicData(new Point(x1, y1));

             */
            /*
            Point bottomLeft = new Point(x1, y1);
            Point topRight = new Point(x2, y2);
            GPSsuradnice suradniceBottom = new GPSsuradnice(bottomLeft);
            GPSsuradnice suradniceTop = new GPSsuradnice(topRight);
            Nehnutelnost nehnutelnost = new Nehnutelnost(id, bottomLeft, topRight, 65, "Ahoj");
            Data pieceOfData1 = new Data(nehnutelnost, suradniceBottom);
            Data pieceOfData2 = new Data(nehnutelnost, suradniceTop);
            this.tree.insert(pieceOfData1);
            this.data.add(pieceOfData1);
            this.tree.insert(pieceOfData2);
            this.data.add(pieceOfData2);



                this.tree.insert(data1);
                this.data.add(data1);
            }

             */
        BasicData data1 = new BasicData(new Point(4.29, 9));
        this.tree.insert(data1);
        this.data.add(data1);
        BasicData data2 = new BasicData(new Point(2.1, 2));
        this.tree.insert(data2);
        this.data.add(data2);
        BasicData data3 = new BasicData(new Point(2.3, 2));
        this.tree.insert(data3);
        this.data.add(data3);
        BasicData data4 = new BasicData(new Point(2.7, 2));
        this.tree.insert(data4);
        this.data.add(data4);
        BasicData data5 = new BasicData(new Point(2.54, 4));
        this.tree.insert(data5);
        this.data.add(data5);

        /*
        BasicData pieceOfData0 = new BasicData(new Point(-94, 146));
        BasicData pieceOfData1 = new BasicData(new Point(-191, -225));
        BasicData pieceOfData2 = new BasicData(new Point(107, 106));
        BasicData pieceOfData3 = new BasicData(new Point(-68, 122));
        this.tree.insert(pieceOfData0);
        this.data.add(pieceOfData0);
        this.tree.insert(pieceOfData1);
        this.data.add(pieceOfData1);
        this.tree.insert(pieceOfData2);
        this.data.add(pieceOfData2);
        this.tree.insert(pieceOfData3);
        this.data.add(pieceOfData3);

         */

            //Assertions.assertEquals(this.tree.numberOfData(), pocetOperacii);
            System.out.println(this.tree.numberOfData());
/*
            for (int i  = 0; i < pocetOperacii; i++) {
                Point points = this.data.get(random.nextInt(0, this.data.size() - 1)).getKey();
                if (this.tree.findAll(points) == null) {
                    System.out.println("error");
                }

            }
            int todelete = pocetOperacii;
            for (int i = 0; i < todelete; i++) {
                BasicData data = this.data.get(random.nextInt(0, this.data.size()));
                if (i == 5) {
                    System.out.println("hello");
                }
                this.tree.deletePublic(data);
                this.data.remove(data);
                System.out.println(this.tree.numberOfData());
            }
            System.out.println(this.tree.numberOfData());
            Assertions.assertEquals(this.tree.numberOfData(), pocetOperacii - todelete);


 */
        this.tree.deletePublic(data1);
            //Point[] points = {new Point(107, 106)};
            //Assertions.assertNotNull(this.tree.findAll(points));


    }

    @org.junit.jupiter.api.Test
    void find() {
        //príprava
        /*
        tester.insert(250.0, 250.0, 500.0, 500.0, new BasicData(250.0, 250.0, 500.0, 500.0) );
        tester.insert(10.0, 10.0, 0.0, 0.0, new BasicData(10.0, 10.0, 0.0, 0.0) );
        tester.insert(145.0, 125.0, 14.0, 16.0, new BasicData(145.0, 125.0, 14.0, 16.0)  );
        tester.insert(445.0, 125.0, 499.0, 499.0, new BasicData(445.0, 125.0, 499.0, 499.0) );
        tester.insert(75.0, 305.0, 90.0, 125.0, new BasicData(75.0, 305.0, 90.0, 125.0) );
        tester.insert(5.0, 5.0, 45.0, 45.0, new BasicData(5.0, 5.0, 45.0, 45.0) );
        tester.insert(275.0, 280.0, 300.0, 320.0, new BasicData(275.0, 280.0, 300.0, 320.0) );
        tester.insert(10.0, 10.0,0.0, 0.0, new BasicData(10.0, 10.0,0.0, 0.0 ));

        tester.insert(0.00005, 0.00005,0.0, 0.0, new BasicData(0.00005, 0.00005,0.0, 0.0 ));
        tester.insert(0.00005, 0.00005,0.0, 0.0, new BasicData(0.00005, 0.00005,0.0, 0.0 ));

        ArrayList<BasicData> pozemky = new ArrayList<>();
        pozemky.addAll(tester.findAll(0.0, 0.0, 500.0, 500.0));

        Assertions.assertEquals(pozemky.size(), 2);
*/




    }

    @org.junit.jupiter.api.Test
    void delete() {
        //príprava
        /*
        BasicData data1 = new BasicData(250.0, 250.0, 500.0, 500.0);
        BasicData data2 = new BasicData(250.0, 250.0, 375.0, 375.0);
        BasicData data3 = new BasicData(250.0, 250.0, 375.0, 375.0);
        BasicData data4 = new BasicData(250.0, 250.0, 300.0, 300.0);
        tester.insert(250.0, 250.0, 500.0, 500.0, data1  );
        tester.insert(250.0, 250.0, 375.0, 375.0,  data2);
        tester.insert(250.0, 250.0, 375.0, 375.0,  data3 );
        tester.insert(250.0, 250.0, 300.0, 300.0, data4 );

        ArrayList<BasicData> pozemky = new ArrayList<>();
        pozemky.addAll(tester.findAll(0.0, 0.0, 500.0, 500.0));

        tester.delete(data3);
        tester.delete(data2);
        tester.delete(data4);
        ArrayList<BasicData> pozemkyplus = new ArrayList<>();
        pozemkyplus.addAll(tester.findAll(0.0, 0.0, 500.0, 500.0));
        Assertions.assertEquals( 7, pozemkyplus.size());
*/
        /*
        for (int i = 0; i < 1000; i++) {
            Data data = this.data.get(ThreadLocalRandom.current().nextInt(0, this.data.size()-1));
            this.tree.deletePublic(data);

        }
        Assertions.assertEquals(this.tree.numberOfData(), pocetOperacii*2 - 1000);

         */
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
    }
}
