package Test;

import DataTypes.ITreeData;
import DataTypes.Point;

public class Key implements ITreeData<Key, Key> {
    private int A;
    private int B;

    public Key(int a, int b) {
        A = a;
        B = b;
    }

    @Override
    public int compareTo(Key key, int dimension) {
        if (dimension == 0) {
            return Integer.compare(A, key.getA());
        } else {
            return Integer.compare(B, key.getB());
        }
    }

    @Override
    public boolean equals(Key data) {
        return A == data.getA() && B == data.getB() ;
    }

    @Override
    public boolean keyEqual(Key key) {
        return key.equals(this);
    }

    @Override
    public Key getKey() {
        return this;
    }

    @Override
    public void deleteSelf() {

    }

    public int getA() {
        return A;
    }

    public int getB() {
        return B;
    }

}
