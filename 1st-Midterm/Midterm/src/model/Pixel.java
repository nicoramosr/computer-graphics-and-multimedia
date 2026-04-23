package model;

public class Pixel {
    public int r;
    public int g;
    public int b;

    //ensures that color values are between 0 and 255
    public Pixel(int r, int g, int b) {
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
    }

    public Pixel(Pixel other) {
        this.r = other.r;
        this.g = other.g;
        this.b = other.b;
    }

    //returns a new Pixel with inverted colors
    public Pixel inverted() {
        return new Pixel(255 - r, 255 - g, 255 - b);
    }

    //values are clamped to be between 0 and 255
    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }


}
