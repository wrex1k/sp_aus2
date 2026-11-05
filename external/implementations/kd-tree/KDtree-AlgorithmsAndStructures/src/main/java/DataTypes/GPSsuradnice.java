package DataTypes;

import java.util.Objects;

public class GPSsuradnice implements ITreeData<GPSsuradnice, GPSsuradnice> {
    private Point bottomLeft;
    private Point topRight;

    private boolean onlyOne;
    public GPSsuradnice(Point bottomLeft, Point topRight) {
        this.bottomLeft = bottomLeft;
        this.topRight = topRight;
        this.onlyOne = false;
    }
    public GPSsuradnice(Point onlyOne) {
        this.bottomLeft = onlyOne;
        this.topRight = onlyOne;
        this.onlyOne = true;
    }



    @Override
    public int compareTo(GPSsuradnice key, int dimension) {
       //System.out.println(dimension == 0 ? Double.compare(key.getBottomLeft().getX(), this.bottomLeft.getX()) : Double.compare(key.getBottomLeft().getY(),this.bottomLeft.getY()));
        return dimension == 0 ? Double.compare(this.bottomLeft.getX(), key.getBottomLeft().getX()) : Double.compare(this.bottomLeft.getY(), key.getBottomLeft().getY());
/*
        if (dimension == 0) {
            if (Objects.equals(this.bottomLeft.getxGPS(), key.bottomLeft.getxGPS())) {
                return 0;
            } else if (Objects.equals(this.bottomLeft.getxGPS(), "E") && Objects.equals(key.bottomLeft.getxGPS(), "W")) {
                return -1;
            } else {
                return 1;
            }
        } else if (dimension == 1) {
            return Double.compare(this.bottomLeft.getX(), key.getBottomLeft().getX());
        } else if (dimension == 2) {
            if (Objects.equals(this.bottomLeft.getyGPS(), this.bottomLeft.getyGPS())) {
                return 0;
            } else if (Objects.equals(this.bottomLeft.getyGPS(), "N") && Objects.equals(this.bottomLeft.getyGPS(), "S")) {
                return -1;
            } else {
                return 1;
            }
        } else {
            return Double.compare(this.bottomLeft.getY(), key.getBottomLeft().getY());
        }
*/

    }

    @Override
    public boolean equals(GPSsuradnice data) {
        if (data.isOnlyOne()||this.isOnlyOne()) {
            if (this.bottomLeft.equals(data.getBottomLeft()) || this.topRight.equals(data.getBottomLeft())) {
                return true;
            }
        } else {
            if (this.bottomLeft.equals(data.getBottomLeft()) && this.topRight.equals(data.getTopRight())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean keyEqual(GPSsuradnice key) {
        return key.equals(this);
    }

    @Override
    public GPSsuradnice getKey() {
        return this;
    }

    @Override
    public void deleteSelf() {
        bottomLeft = null;
        topRight = null;
    }

    public Point getBottomLeft() {
        return bottomLeft;
    }

    public Point getTopRight() {
        return topRight;
    }
    public boolean equalsOne(GPSsuradnice suradnice) {
        if (suradnice.getTopRight().equals(topRight) || suradnice.getTopRight().equals(bottomLeft) || suradnice.getBottomLeft().equals(topRight) || suradnice.getBottomLeft().equals(bottomLeft)) {
            return true;
        }
        return false;
    }
    @Override
    public String toString() {
        return "Bod č.1: " + bottomLeft.getX() + "," + bottomLeft.getY() + " Bod č.2: " + topRight.getX() + "," + topRight.getY();
    }
    @Override
    public int hashCode() {
        return Objects.hash(bottomLeft, topRight, onlyOne);
    }
    public boolean isOnlyOne() {
        return onlyOne;
    }

}
