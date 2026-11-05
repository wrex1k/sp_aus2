package Test;

import KDtree.KDtree;

import java.util.ArrayList;
import java.util.Random;

public class Measuring {
    private int pocetOperacii = 20000;
    private int pocetData= 20000;
    private int minX = 0;
    private int maxX = 50;
    private double minY = -250.0;
    private double maxY = 250.0;
    private ArrayList<String> stringy;
    private
    KDtree<TestovacieData, Key> tree;
    ArrayList<TestovacieData> data;
    Random random;
    private int id;
    public Measuring() {
        this.tree = new KDtree<TestovacieData, Key>(2);
        this.data = new ArrayList<>();
        this.stringy = new ArrayList<>();
        id = 1;
        random = new Random();
    }

    public void insert() {

        for (int i = 0; i < pocetData; i++) {
            /*
            double x1 = (ThreadLocalRandom.current().nextDouble(minX, maxX));
            double x2 = (ThreadLocalRandom.current().nextDouble(minX, maxX));
            double y1 = (ThreadLocalRandom.current().nextDouble(minY, maxY));
            double y2 = (ThreadLocalRandom.current().nextDouble(minY, maxY));
            while ((x1 <= x2 && y1 >= y2) || (x2 <= x1 && y2 >= y1)) {
                x1 = (ThreadLocalRandom.current().nextDouble(minX, maxX));
                x2 = (ThreadLocalRandom.current().nextDouble(minX, maxX));
                y1 = (ThreadLocalRandom.current().nextDouble(minY, maxY));
                y2 = (ThreadLocalRandom.current().nextDouble(minY, maxY));
            }

             */
            this.createData();
        }
        System.out.println("Pocet vlozenych dat:" + pocetOperacii+ ". Počet dát v strome:" + this.tree.numberOfData());
    }
    private void createData() {
        TestovacieData pieceOfData = new TestovacieData(new Key(random.nextInt(minX, maxX),
                random.nextInt(minX, maxX)));

        this.tree.insert(pieceOfData);
        this.data.add(pieceOfData);
        id++;
    }

    public void test() {
        int pocetPokus = 10000;
        int pocetDelete = 0;
        int pocetInsert = 0;
        for (int i = 0; i < pocetPokus; i++) {
            switch (random.nextInt(1, 4)) {
                case 1:
                    this.createData();
                    pocetInsert++;
                    if ((pocetOperacii+pocetInsert-pocetDelete) != this.tree.numberOfData()) {
                        System.out.println("Error");
                    }
                    break;
                case 2:
                    Key key = this.data.get(random.nextInt(0, this.data.size()-1)).getKey();
                    if (this.tree.findAllExact(key).isEmpty()) {
                        System.out.println("Error");
                    };


                    break;
                case 3:

                    TestovacieData pieceOfData = this.data.get(random.nextInt(0,this.data.size()-1));
                    pocetDelete++;
                    boolean ret = this.tree.deletePublic(pieceOfData);

                    if (! ret || (pocetOperacii+pocetInsert-pocetDelete) != this.tree.numberOfData()) {
                        System.out.println("Error");
                    }


                    this.data.remove(pieceOfData);

                    if (this.tree.findAllExact(this.data.get(random.nextInt(0, this.data.size()-1)).getKey()).isEmpty()) {
                        System.out.println("Error");
                    };


                    break;
            }
            ;
            //if (i % 100 == 0) {
                System.out.println("Teoreticky pocet dat:" + (pocetOperacii+pocetInsert-pocetDelete) + ". Počet dát v strome:" + this.tree.numberOfData());
            //}
        }

    }

    public int countOfData() {
        return this.tree.numberOfData();
    }


}
