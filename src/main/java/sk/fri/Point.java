package sk.fri;

public record Point<K>(K x, K y) {
    public Point {
        if (x == null) {
            throw new IllegalArgumentException("x cannot be null");
        }
        if (y == null) {
            throw new IllegalArgumentException("y cannot be null");
        }
    }
}