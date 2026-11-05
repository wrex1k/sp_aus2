package Test;

import DataTypes.BasicData;
import DataTypes.ITreeData;
import DataTypes.Point;

import java.util.Objects;
import java.util.Random;

public class TestovacieData implements ITreeData<TestovacieData, Key> {

    private String id;
    private Key key;

    public TestovacieData(Key key) {
        this.id = this.generateString();
        this.key = key;
    }

    @Override
    public int compareTo(Key key, int dimension) {
        return this.key.compareTo(key, dimension);
    }

    @Override
    public boolean equals(TestovacieData data) {
        return (Objects.equals(data.getID(), this.id));
    }

    @Override
    public boolean keyEqual(Key key) {
        return this.key.equals(key);
    }

    @Override
    public Key getKey() {
        return this.key;
    }

    @Override
    public void deleteSelf() {

    }
    public String getID() {
        return this.id;
    }

    private String generateString() {

        int leftLimit = 97; // 'a'
        int rightLimit = 122; // 'z'
        int targetStringLength = 10;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        String generatedString = buffer.toString();

        return generatedString;
    }
}
