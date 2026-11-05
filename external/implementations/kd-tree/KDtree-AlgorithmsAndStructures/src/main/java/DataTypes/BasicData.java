package DataTypes;

public class BasicData implements ITreeData<BasicData, Point>{
    private Point bottomLeft;

    public BasicData(Point bottomLeft) {
        this.bottomLeft = bottomLeft;
    }

    public Point getBottomLeft() {
        return bottomLeft;
    }


    @Override
    public int compareTo(Point key, int dimension) {
        return bottomLeft.compareTo(key, dimension);
    }

    @Override
    public boolean equals(BasicData data) {
        return data.getKey().equals(bottomLeft);
    }

    @Override
    public boolean keyEqual(Point key) {
        return this.bottomLeft.equals(key);
    }

    @Override
    public Point getKey() {
        return bottomLeft;
    }

    @Override
    public void deleteSelf() {

    }
}
