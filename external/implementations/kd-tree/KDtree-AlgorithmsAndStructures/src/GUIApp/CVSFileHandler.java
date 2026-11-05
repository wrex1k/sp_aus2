package GUIApp;

import DataTypes.*;
import KDtree.KDtree;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CVSFileHandler {
    private static final String CSV_SEPARATOR = ",";

    public static KDtree[] readCSVFile(String filePath, char treeDataType) throws IOException {
        Path path = Path.of(filePath);

        KDtree<Data, GPSsuradnice> tree;
        KDtree<Data, GPSsuradnice> treeParcely;
        KDtree<Data, GPSsuradnice> treeNehnutelnosti;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            line = reader.readLine();
            List<String> treePara = Arrays.asList(line.split(CSV_SEPARATOR));
            tree = new KDtree<Data, GPSsuradnice>(Integer.parseInt(treePara.get(0)));
            treeParcely = new KDtree<Data, GPSsuradnice>(Integer.parseInt(treePara.get(0)));
            treeNehnutelnosti = new KDtree<Data, GPSsuradnice>(Integer.parseInt(treePara.get(0)));
            tree.setCurrentId(Integer.parseInt(treePara.get(1)));
            ArrayList<Land> zoznamVlozenych = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                List<String> fields = Arrays.asList(line.split(CSV_SEPARATOR));
                GPSsuradnice suradnice = new GPSsuradnice(new Point(Double.parseDouble(fields.get(4)),Double.parseDouble(fields.get(5))), new Point(Double.parseDouble(fields.get(6)),Double.parseDouble(fields.get(7))));
                GPSsuradnice key = new GPSsuradnice(new Point(Double.parseDouble(fields.get(8)),Double.parseDouble(fields.get(9))));
                ArrayList<Data> listOfLand = new ArrayList<>();
                Nehnutelnost hnut = new Nehnutelnost(Integer.parseInt(fields.get(1)), suradnice.getBottomLeft(), suradnice.getTopRight(), Integer.parseInt(fields.get(2)), fields.get(3));
                Parcela parc = new Parcela(Integer.parseInt(fields.get(1)), suradnice.getBottomLeft(), suradnice.getTopRight(), Integer.parseInt(fields.get(2)), fields.get(3));
                /*
                Data foundNeh = tree.findOne(new Data(hnut, key));
                Data foundParc = tree.findOne(new Data(parc, key));
                if (foundNeh != null ) {
                    hnut = (Nehnutelnost) foundNeh.getLand();
                } else if (foundParc != null) {
                    parc = (Parcela) foundParc.getLand();
                }

                 */
                if (fields.get(0).equals("N") && treeDataType == 'N') {
                    tree.insert(new Data(hnut, key));
                } else if (fields.get(0).equals("P") && treeDataType == 'P') {
                    tree.insert(new Data(parc, key));
                } else if (treeDataType == 'F') {
                    if (fields.get(0).equals("N")) {
                        if (zoznamVlozenych.contains(hnut)) {
                            hnut = (Nehnutelnost) zoznamVlozenych.get(zoznamVlozenych.indexOf(hnut));
                        } else {
                            zoznamVlozenych.add(hnut);
                        }
                        listOfLand.addAll(tree.insert(new Data(hnut, key)).getListOfData());
                        treeNehnutelnosti.insert(new Data(hnut, key));
                        for (Data nodeData : listOfLand) {
                            nodeData.getLand().nastavRefernciu(hnut);
                        };
                    } else if (fields.get(0).equals("P")) {
                        if (zoznamVlozenych.contains(parc)) {
                            parc = (Parcela) zoznamVlozenych.get(zoznamVlozenych.indexOf(parc));
                        } else {
                            zoznamVlozenych.add(parc);
                        }
                        listOfLand.addAll(tree.insert(new Data(parc, key)).getListOfData());
                        treeParcely.insert(new Data(parc, key));
                        for (Data nodeData : listOfLand) {
                            nodeData.getLand().nastavRefernciu(parc);
                        };
                    }
                }

            }
        }
        KDtree[] stromy = new KDtree[3];
        stromy[0] = tree;
        stromy[1] = treeNehnutelnosti;
        stromy[2] = treeParcely;
        return stromy;
    }

    public static void writeCSVFile(String filePath, KDtree<Data, GPSsuradnice> tree) throws IOException {
        Path path = Path.of(filePath);

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            List<String> treeInfo = Arrays.asList(String.valueOf(tree.getMaxDimension()), String.valueOf(tree.getCurrentId()));
            String treLine = String.join(CSV_SEPARATOR, treeInfo);
            writer.write(treLine);
            writer.newLine();

            ArrayList<Data> data = tree.allData();
            for (Data land: data) {
                String type;
                if (land.getLand() instanceof Nehnutelnost) {
                    type = "N";
                } else {
                    type = "P";
                }
                List<String> information = Arrays.asList(type, String.valueOf(land.getLand().getId()), String.valueOf(land.getLand().getCisloSupis()),
                        land.getLand().getPopis(), String.valueOf(land.getLand().getKey().getBottomLeft().getX()),  String.valueOf(land.getLand().getKey().getBottomLeft().getY()),
                        String.valueOf(land.getLand().getKey().getTopRight().getX()), String.valueOf(land.getLand().getKey().getTopRight().getY()),
                                String.valueOf(land.getKey().getBottomLeft().getX()), String.valueOf(land.getKey().getBottomLeft().getY()));
                String line = String.join(CSV_SEPARATOR, information);
                writer.write(line);
                writer.newLine();
            }
        }


    }

}
