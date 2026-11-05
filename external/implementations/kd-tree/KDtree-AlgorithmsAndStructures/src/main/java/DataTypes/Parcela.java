package DataTypes;

public class Parcela extends Land{

    public Parcela(int id, Point bottomLeft, Point topRight, int cisloSupis, String popis) {
        super(id, bottomLeft, topRight, cisloSupis, popis);
    }
    public Parcela(Land copy) {
        super(copy);
    }

    @Override
    public String toString() {
        String udaje = "Číslo parcely:" + super.getCisloSupis() + " | " + super.getSuradnice().toString() + " | Popis:" + super.getPopis() + " | Zoznam prekrývajúcich nehnuteĺností: ";
        if (!super.getZoznam().isEmpty()) {
            for (Land land : super.getZoznam()) {
                udaje = udaje + land.getCisloSupis() + " [ " + land.getSuradnice().toString() + " ] ,";
            }
        }

        return udaje;
    }
}
