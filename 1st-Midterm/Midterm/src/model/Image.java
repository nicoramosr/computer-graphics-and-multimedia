package model;

public class Image {
    private final int width, height;
    private final Pixel[][] pixels; // pixels in y,x order

    public Image(int width, int height) {
        this.width = width;
        this.height = height;
        pixels = new Pixel[height][width];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                pixels[y][x] = new Pixel(0, 0, 0);
    }


    public Image(Image src) {
        this(src.width, src.height);
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                pixels[y][x] = new Pixel(src.pixels[y][x]);
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    public Pixel get(int x, int y){
        return pixels[y][x];
    }

    public void  set(int x, int y, Pixel p){ 
        pixels[y][x] = p;
    }

    @Override
    public String toString() { return width + "x" + height; }
}

