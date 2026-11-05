package DataTypes;

public class Data implements ITreeData<Data, GPSsuradnice> {
    private Land land;
    private GPSsuradnice point;

    public Data(Land land, GPSsuradnice point) {
        this.land = land;
        this.point = point;
    }

    @Override
    public int compareTo(GPSsuradnice key, int dimension) {
        return this.point.compareTo(key, dimension);
    }

    @Override
    public boolean equals(Data data) {
        return this.land.equals(data.getLand());
    }

    @Override
    public boolean keyEqual(GPSsuradnice key) {
        return point.equals(key);
    }


    @Override
    public GPSsuradnice getKey() {
        return point;
    }


    @Override
    public void deleteSelf() {
        this.land.deleteSelf();
    }
    public Land getLand() {
        return this.land;
    }
}
