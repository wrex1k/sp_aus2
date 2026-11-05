package GUIApp;

import DataTypes.Land;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class App {

    private JPanel hlavny;
    private JButton option1;
    private JButton option3;
    private JButton option4;
    private JButton back1;
    private JList listNehnutelnosti;
    private JTextField widthLetter1;
    private JTextField widthNumber1;
    private JTextField heightNumber1;
    private JTextField heightLetter1;
    private JButton submit1;
    private JButton back3;
    private JList listObjects;
    private JTextField widthLetter4;
    private JTextField widthNumber4;
    private JTextField heightLetter4;
    private JTextField heightNumber4;
    private JTextField widthLetter3;
    private JTextField widthNumber3;
    private JTextField heightLetter3;
    private JTextField heightNumber3;
    private JButton submit3;
    private JPanel menu;
    private JPanel searchNehnutelnost;
    private JPanel searchObjects;
    private JPanel addNehnutelnost;
    private JButton back4;
    private JButton submit4;
    private JTextField widthLetter5;
    private JTextField widthNumber5;
    private JTextField heightLetter5;
    private JTextField heightNumber5;
    private JTextField widthLetter6;
    private JTextField widthNumber6;
    private JTextField heightLetter6;
    private JTextField heightNumber6;
    private JTextField cislo5;
    private JTextField popis5;
    private JPanel searchParcela;
    private JButton back2;
    private JList listParciel;
    private JTextField widthLetter2;
    private JTextField heightNumber2;
    private JTextField widthNumber2;
    private JTextField heightLetter2;
    private JButton submit2;
    private JButton option2;
    private JPanel addParcela;
    private JButton back5;
    private JButton submit5;
    private JTextField cislo7;
    private JTextField popis7;
    private JTextField widthLetter8;
    private JTextField heightNumber8;
    private JTextField widthNumber8;
    private JTextField heightLetter8;
    private JTextField widthLetter7;
    private JTextField widthNumber7;
    private JTextField heightNumber7;
    private JTextField heightLetter7;
    private JButton option5;
    private JButton option6;
    private JButton option7;
    private JButton option8;
    private JButton option9;
    private JPanel editNehnutelnost;
    private JButton back6;
    private JButton submit6;
    private JTextField widthLetter9;
    private JTextField widthNumber9;
    private JTextField heightLetter9;
    private JTextField heightNumber9;
    private JPanel editParcela;
    private JList listEditNehnutelnost;
    private JButton back7;
    private JButton submit7;
    private JTextField widthLetter10;
    private JTextField widthNumber10;
    private JTextField heightLetter10;
    private JTextField heightNumber10;
    private JList listEditParcela;
    private JPanel editNehnutelnostPlus;
    private JPanel editParcelaPlus;
    private JPanel deleteNehnutelnost;
    private JPanel deleteParcela;
    private JButton back6plus;
    private JButton submit6plus;
    private JTextField editCislo1;
    private JTextField editPopis1;
    private JTextField editWidthLetter2;
    private JTextField editWidthNumber2;
    private JTextField editHeightLetter2;
    private JTextField editHeightNumber2;
    private JTextField editWidthLetter1;
    private JTextField editWidthNumber1;
    private JTextField editHeightLetter1;
    private JTextField editHeightNumber1;
    private JButton back7plus;
    private JButton submit7plus;
    private JTextField editWidthLetter3;
    private JTextField editWidthLetter4;
    private JTextField editWidthNumber3;
    private JTextField editWidthNumber4;
    private JTextField editHeightLetter3;
    private JTextField editHeightLetter4;
    private JTextField editHeightNumber3;
    private JTextField edithHeightNumber4;
    private JTextField editPopis3;
    private JTextField editCislo3;
    private JList listDeleteNehnutelnost;
    private JList listDeleteParcela;
    private JButton back9;
    private JButton back8;
    private JButton submit9;
    private JButton submit8;
    private JTextField deleteWidthLetter1;
    private JTextField deleteWidthNumber1;
    private JTextField deleteHeightNumber1;
    private JTextField deleteHeightLetter1;
    private JTextField deleteWidthLetter2;
    private JTextField deleteWidthNumber2;
    private JTextField deleteHeightLetter2;
    private JTextField deleteHeightNumber2;
    private JButton option12;
    private JButton option13;
    private JButton option10;
    private JButton option11;
    private JPanel generateData;
    private JButton back10;
    private JTextField numberOfNehnutelnost;
    private JTextField numberOfParcela;
    private JButton submit10;
    private JPanel setBounds;
    private JButton back11;
    private JButton submit11;
    private JTextField systemHeight;
    private JTextField systemWidth;
    private JTextField systemX;
    private JTextField systemY;
    private JSlider sliderPercenta;
    private JLabel prekryvLabel;
    private JButton buttonVsetko;
    private CardLayout cl;

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new App();
            }
        });



    }

    public App() {
        JFrame frame = new JFrame("Databázová aplikácia.");
        frame.setContentPane(this.getPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        cl = (CardLayout)hlavny.getLayout();

        Manager manager = new Manager();

        listObjects.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = listObjects.getSelectedIndex();
                Land l = (Land) listObjects.getSelectedValue();

            }
        });


        back1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cl.show(hlavny, "menu");
            }
        });
        back2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cl.show(hlavny, "menu");
            }
        });
        back3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cl.show(hlavny, "menu");
            }
        });
        back4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cl.show(hlavny, "menu");
            }
        });
        back5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cl.show(hlavny, "menu");
            }
        });
        back6.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cl.show(hlavny, "menu");
            }
        });
        back6plus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cl.show(hlavny, "menu");
            }
        });
        back7.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cl.show(hlavny, "menu");
            }
        });
        back7plus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cl.show(hlavny, "menu");
            }
        });
        back8.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cl.show(hlavny, "menu");
            }
        });
        back9.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { cl.show(hlavny, "menu");
            }
        });
        back10.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cl.show(hlavny, "menu");
            }
        });
        back11.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cl.show(hlavny, "menu");
            }
        });
        option1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cl.show(hlavny, "searchNehnutelnost");
            }
        });
        option2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cl.show(hlavny, "searchParcela");
            }
        });
        option3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cl.show(hlavny, "searchObjects");
            }
        });
        option4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cl.show(hlavny, "addNehnutelnost");
            }
        });
        option5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cl.show(hlavny, "addParcela" );
            }
        });
        option6.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cl.show(hlavny, "editNehnutelnost");
            }
        });
        option7.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cl.show(hlavny, "editParcela");
            }
        });
        option8.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cl.show(hlavny, "deleteNehnutelnost");
            }
        });
        option9.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cl.show(hlavny, "deleteParcela");
            }
        });
        option10.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                manager.loadData();
            }
        });
        option11.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                manager.saveData();
            }
        });
        option12.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                manager.clear();
            }
        });
        option13.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cl.show(hlavny, "generateData");
            }
        });

        submit1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //vyhladavanie nehnutelnosti
                ArrayList<Land> nehnutelnosti = manager.find(widthLetter1.getText().toCharArray()[0], Double.parseDouble(widthNumber1.getText().toString()), heightLetter1.getText().toCharArray()[0], Double.parseDouble(heightNumber1.getText().toString()), 'N');

                DefaultListModel<Land> listModel = new DefaultListModel<>();
                for (Land object : nehnutelnosti) {
                    listModel.addElement(object);
                }
                listNehnutelnosti.setModel(listModel);
            }
        });
        submit2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //vyhladanie parcely
                ArrayList<Land> parcely = manager.find(widthLetter2.getText().toCharArray()[0], Double.parseDouble(widthNumber2.getText().toString()), heightLetter2.getText().toCharArray()[0], Double.parseDouble(heightNumber2.getText().toString()), 'P');
                DefaultListModel<Land> listModel = new DefaultListModel<>();
                for (Land object : parcely) {
                    listModel.addElement(object);
                }
                listParciel.setModel(listModel);
            }
        });
        submit3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //vyhladanie podla dvoch bodov
                ArrayList<Land> objects = manager.find(widthLetter3.getText().toCharArray()[0], Double.parseDouble(widthNumber3.getText().toString()), heightLetter3.getText().toCharArray()[0], Double.parseDouble(heightNumber3.getText().toString()), widthLetter4.getText().toCharArray()[0], Double.parseDouble(widthNumber4.getText().toString()), heightLetter4.getText().toCharArray()[0], Double.parseDouble(heightNumber4.getText().toString()), 'E');
                DefaultListModel<Land> listModel = new DefaultListModel<>();
                for (Land object : objects) {
                    listModel.addElement(object);
                }
                listObjects.setModel(listModel);
            }
        });

        submit4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //vkladanie nehnutelnosti
                manager.insert(widthLetter5.getText().toCharArray()[0], Double.parseDouble(widthNumber5.getText().toString()), heightLetter5.getText().toCharArray()[0], Double.parseDouble(heightNumber5.getText().toString()), widthLetter6.getText().toCharArray()[0], Double.parseDouble(widthNumber6.getText().toString()), heightLetter6.getText().toCharArray()[0], Double.parseDouble(heightNumber6.getText().toString()), 'N', Integer.parseInt(cislo5.getText()), popis5.getText());
                cl.show(hlavny, "menu");
            }
        });
        submit5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //vkladanie parcely
                manager.insert(widthLetter7.getText().toCharArray()[0], Double.parseDouble(widthNumber7.getText().toString()), heightLetter7.getText().toCharArray()[0], Double.parseDouble(heightNumber7.getText().toString()), widthLetter8.getText().toCharArray()[0], Double.parseDouble(widthNumber8.getText().toString()), heightLetter8.getText().toCharArray()[0], Double.parseDouble(heightNumber8.getText().toString()), 'P', Integer.parseInt(cislo7.getText()), popis7.getText());
                cl.show(hlavny, "menu");
            }
        });
        submit6.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Land> objects = manager.find(widthLetter9.getText().toCharArray()[0],
                            Double.parseDouble(widthNumber9.getText().toString()),
                            heightLetter9.getText().toCharArray()[0],
                        Double.parseDouble(heightNumber9.getText().toString()), 'N');
                DefaultListModel<Land> listModel = new DefaultListModel<>();
                for (Land object : objects) {
                    listModel.addElement(object);
                }
                listEditNehnutelnost.setModel(listModel);
            }
        });

        listEditNehnutelnost.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!listEditNehnutelnost.isSelectionEmpty()) {
                    Land data = (Land)listEditNehnutelnost.getSelectedValue();

                    double x1 = data.getKey().getBottomLeft().getX();
                    double y1 = data.getKey().getBottomLeft().getY();
                    double x2 = data.getKey().getTopRight().getX();
                    double y2 = data.getKey().getTopRight().getY();
                    String x1GPS = "E";
                    String y1GPS = "N";
                    String x2GPS = "E";
                    String y2GPS = "N";
                    if (x1 < 0) {
                        x1GPS = "W";
                    }
                    if (x2 < 0) {
                        x2GPS = "W";
                    }
                    if (y1 < 0) {
                        y1GPS = "S";
                    }
                    if (y2 < 0) {
                        y2GPS = "S";
                    }
                    x1 = Math.abs(data.getKey().getBottomLeft().getX());
                    y1 = Math.abs(data.getKey().getBottomLeft().getY());
                    x2 = Math.abs(data.getKey().getTopRight().getX());
                    y2 = Math.abs(data.getKey().getTopRight().getY());
                    editWidthLetter1.setText(x1GPS);
                    editWidthNumber1.setText(String.valueOf(x1));
                    editHeightLetter1.setText(y1GPS);
                    editHeightNumber1.setText(String.valueOf(y1));
                    editWidthLetter2.setText(x2GPS);
                    editWidthNumber2.setText(String.valueOf(x2));
                    editHeightLetter2.setText(y2GPS);
                    editHeightNumber2.setText(String.valueOf(y2));
                    editPopis1.setText(data.getPopis());
                    editCislo1.setText(String.valueOf(data.getCisloSupis()));
                    cl.show(hlavny, "editNehnutelnostPlus");

                }

            }
        });
        submit6plus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                manager.edit(editWidthLetter1.getText().toCharArray()[0],
                        Double.parseDouble(editWidthNumber1.getText().toString()),
                        editHeightLetter1.getText().toCharArray()[0],
                        Double.parseDouble(editHeightNumber1.getText().toString()),
                        editWidthLetter2.getText().toCharArray()[0],
                        Double.parseDouble(editWidthNumber2.getText().toString()),
                        editHeightLetter2.getText().toCharArray()[0],
                        Double.parseDouble(editHeightNumber2.getText().toString()),
                        Integer.parseInt(editCislo1.getText()), editPopis1.getText(), (Land)listEditNehnutelnost.getSelectedValue(), 'N');
                cl.show(hlavny, "menu");
            }
        });

        submit7.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Land> objects = manager.find(widthLetter10.getText().toCharArray()[0], Double.parseDouble(widthNumber10.getText().toString()), heightLetter10.getText().toCharArray()[0], Double.parseDouble(heightNumber10.getText().toString()),'P');
                DefaultListModel<Land> listModel = new DefaultListModel<>();
                for (Land object : objects) {
                    listModel.addElement(object);
                }
                listEditParcela.setModel(listModel);
            }
        });

        listEditParcela.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!listEditParcela.isSelectionEmpty()) {
                    Land data = (Land)listEditParcela.getSelectedValue();

                    double x1 = data.getKey().getBottomLeft().getX();
                    double y1 = data.getKey().getBottomLeft().getY();
                    double x2 = data.getKey().getTopRight().getX();
                    double y2 = data.getKey().getTopRight().getY();
                    String x1GPS = "E";
                    String y1GPS = "N";
                    String x2GPS = "E";
                    String y2GPS = "N";
                    if (x1 < 0) {
                        x1GPS = "W";
                    }
                    if (x2 < 0) {
                        x2GPS = "W";
                    }
                    if (y1 < 0) {
                        y1GPS = "S";
                    }
                    if (y2 < 0) {
                        y2GPS = "S";
                    }
                    x1 = Math.abs(data.getKey().getBottomLeft().getX());
                    y1 = Math.abs(data.getKey().getBottomLeft().getY());
                    x2 = Math.abs(data.getKey().getTopRight().getX());
                    y2 = Math.abs(data.getKey().getTopRight().getY());
                    editWidthLetter3.setText(x1GPS);
                    editWidthNumber3.setText(String.valueOf(x1));
                    editHeightLetter3.setText(y1GPS);
                    editHeightNumber3.setText(String.valueOf(y1));
                    editWidthLetter4.setText(x2GPS);
                    editWidthNumber4.setText(String.valueOf(x2));
                    editHeightLetter4.setText(y2GPS);
                    edithHeightNumber4.setText(String.valueOf(y2));
                    editPopis3.setText(data.getPopis());
                    editCislo3.setText(String.valueOf(data.getCisloSupis()));
                    cl.show(hlavny, "editParcelaPlus");

                }
            }
        });
        submit7plus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                manager.edit(editWidthLetter3.getText().toCharArray()[0],
                            Double.parseDouble(editWidthNumber3.getText().toString()),
                        editHeightLetter3.getText().toCharArray()[0],
                        Double.parseDouble(editHeightNumber3.getText().toString()),
                        editWidthLetter4.getText().toCharArray()[0],
                        Double.parseDouble(editWidthNumber4.getText().toString()),
                        editHeightLetter4.getText().toCharArray()[0],
                        Double.parseDouble(edithHeightNumber4.getText().toString()),
                        Integer.parseInt(editCislo3.getText()), editPopis3.getText(), (Land)listEditParcela.getSelectedValue(), 'P');
                cl.show(hlavny, "menu");
            }
        });



        submit8.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Land> objects = manager.find(deleteWidthLetter1.getText().toCharArray()[0], Double.parseDouble(deleteWidthNumber1.getText().toString()), deleteHeightLetter1.getText().toCharArray()[0], Double.parseDouble(deleteHeightNumber1.getText().toString()), 'N');
                DefaultListModel<Land> listModel = new DefaultListModel<>();
                for (Land object : objects) {
                    listModel.addElement(object);
                }
                listDeleteNehnutelnost.setModel(listModel);
            }
        });
        listDeleteNehnutelnost.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(!listDeleteNehnutelnost.isSelectionEmpty()) {
                    Land data = (Land)listDeleteNehnutelnost.getSelectedValue();
                    manager.delete(data, 'N');
                    cl.show(hlavny, "menu");
                }

            }
        });


        submit9.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Land> objects = manager.find(deleteWidthLetter2.getText().toCharArray()[0], Double.parseDouble(deleteWidthNumber2.getText().toString()), deleteHeightLetter2.getText().toCharArray()[0], Double.parseDouble(deleteHeightNumber2.getText().toString()), 'P');
                DefaultListModel<Land> listModel = new DefaultListModel<>();
                for (Land object : objects) {
                    listModel.addElement(object);
                }
                listDeleteParcela.setModel(listModel);
            }
        });
        listDeleteParcela.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!listDeleteParcela.isSelectionEmpty()) {
                    Land data = (Land)listDeleteParcela.getSelectedValue();
                    manager.delete(data, 'P');
                    cl.show(hlavny, "menu");
                }

            }
        });

        submit10.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                manager.insertData(Integer.parseInt(numberOfParcela.getText()), Integer.parseInt(numberOfNehnutelnost.getText()), sliderPercenta.getValue());
                cl.show(hlavny, "menu");
            }
        });

        submit11.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //manager.setBounds(Double.parseDouble(systemX.getText()), Double.parseDouble(systemY.getText()), Double.parseDouble(systemWidth.getText()), Double.parseDouble(systemHeight.getText()));
                cl.show(hlavny, "menu");
            }
        });


        buttonVsetko.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Land> objects = manager.findAll();
                DefaultListModel<Land> listModel = new DefaultListModel<>();
                for (Land object : objects) {
                    listModel.addElement(object);
                }
                listObjects.setModel(listModel);
                cl.show(hlavny, "searchObjects");
            }
        });
    }
    public JPanel getPanel(){
        return hlavny;
    }


}
