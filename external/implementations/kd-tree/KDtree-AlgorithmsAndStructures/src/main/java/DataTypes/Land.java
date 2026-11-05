package DataTypes;

import java.util.ArrayList;

public class Land implements ITreeData<Land, GPSsuradnice> {
    private int id;
    private int cisloSupis;
    private String popis;
    private GPSsuradnice suradnice;
    private ArrayList<Land> zoznam;

    public Land(int id, Point bottomLeft, Point topRight, int cisloSupis, String popis) {
        this.id = id;
        this.cisloSupis = cisloSupis;
        this.popis = popis;
        this.suradnice = new GPSsuradnice(bottomLeft, topRight);
        zoznam = new ArrayList<>();
    }
    public Land(Land landCopy) {
        this.id = landCopy.getId();
        this.cisloSupis = landCopy.getCisloSupis();
        this.popis = landCopy.getPopis();
        this.suradnice = new GPSsuradnice(landCopy.getSuradnice().getBottomLeft(), landCopy.getSuradnice().getTopRight());
        zoznam = landCopy.getZoznam();
    }


    public int getCisloSupis() {
        return cisloSupis;
    }

    public String getPopis() {
        return popis;
    }



    @Override
    public int compareTo(GPSsuradnice key, int dimension) {
        return suradnice.compareTo(key, dimension); // return 1 ak sa rovnaju
    }

    @Override
    public boolean equals(Land data) {
        if ((this instanceof Nehnutelnost && data instanceof Nehnutelnost) || (this instanceof Parcela && data instanceof Parcela)) {
            if (data.getCisloSupis() == this.cisloSupis && data.getId() == this.id && data.getPopis().equals(this.popis) && data.getSuradnice().equals(suradnice)) {
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyEqual(GPSsuradnice key) {
        return suradnice.equals(key);
    }

    @Override
    public boolean equals(Object o) {
        return this.equals((Land)o);
    }
    @Override
    public GPSsuradnice getKey() {
        return suradnice;
    }

    @Override
    public void deleteSelf() {
        for (Land i : zoznam) {
            i.getZoznam().remove(this);
        }
        zoznam.clear();
    }

    public GPSsuradnice getSuradnice() {
        return suradnice;
    }

    @Override
    public String toString() {
        return "Číslo:" + cisloSupis + " " + suradnice.toString() + " Popis:" + popis;
    }

    public int getId() {
        return id;
    }

    public ArrayList<Land> getZoznam() {
        return zoznam;
    }
    public void nastavRefernciu(Land data) {
        if ((this instanceof Nehnutelnost && data instanceof Parcela) || (this instanceof Parcela && data instanceof Nehnutelnost)) {
            if (data.getSuradnice().equalsOne(suradnice) && !zoznam.contains(data)) {
                zoznam.add(data);
                data.getZoznam().add(this);
            }
        }
    }

    public void setCisloSupis(int cisloSupis) {
        this.cisloSupis = cisloSupis;
    }

    public void setPopis(String popis) {
        this.popis = popis;
    }
}
