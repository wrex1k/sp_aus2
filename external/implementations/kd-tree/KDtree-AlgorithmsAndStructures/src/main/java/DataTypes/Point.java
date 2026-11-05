package DataTypes;

import java.util.Objects;

public class Point implements ITreeData<Point, Point> {

    // porovnat dva body a
    private double x;
    private double y;
    private String xGPS;
    private String yGPS;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
        if (this.x >= 0) {
            this.xGPS = "E";
        } else {
            this.xGPS = "W";
        }
        if (this.y >= 0) {
            this.yGPS = "N";
        } else {
            this.yGPS = "S";
        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public int compareTo(Point o, int dimension) {
        return dimension == 0 ? Double.compare(x, o.getX()) : Double.compare(y, o.getY());
/*
        if (dimension == 0) {
            if (Objects.equals(this.xGPS, o.getxGPS())) {
                return 0;
            } else if (Objects.equals(this.xGPS, "E") && Objects.equals(o.getxGPS(), "W")) {
                return -1;
            } else {
                return 1;
            }
        } else if (dimension == 1) {
           return Double.compare(x, o.getX());
        } else if (dimension == 2) {
            if (Objects.equals(this.yGPS, o.getyGPS())) {
                return 0;
            } else if (Objects.equals(this.yGPS, "N") && Objects.equals(o.getyGPS(), "S")) {
                return -1;
            } else {
                return 1;
            }
        } else {
          return Double.compare(y, o.getY());
        }
*/


    }

    @Override
    public boolean equals(Point data) {
        return (data.getY() == this.y && data.x == this.x);
    }

    @Override
    public boolean keyEqual(Point key) {
        return key.equals(this);
    }

    @Override
    public Point getKey() {
        return this;
    }

    @Override
    public void deleteSelf() {

    }

    public String getxGPS() {
        return xGPS;
    }

    public String getyGPS() {
        return yGPS;
    }
}
