package DataTypes;

public class Nehnutelnost extends Land{
    public Nehnutelnost(int id, Point bottomLeft, Point topRight, int cisloSupis, String popis) {
        super(id, bottomLeft, topRight, cisloSupis, popis);
    }

    public Nehnutelnost(Land copy) {
        super(copy);
    }
    @Override
    public String toString() {
        String udaje = "Súpisné číslo:" + super.getCisloSupis() + " | " + super.getSuradnice().toString() + " | Popis:" + super.getPopis() + " | Zoznam prekrývajúcich parciel: ";
        if (!super.getZoznam().isEmpty()) {
            for (Land land : super.getZoznam()) {
                udaje = udaje + land.getCisloSupis() + " [ " + land.getSuradnice().toString() + " ] ,";
            }
        }
        return udaje;
    }
}
