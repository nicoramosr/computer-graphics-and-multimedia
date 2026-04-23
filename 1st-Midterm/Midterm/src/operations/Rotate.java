package operations;

import model.Image;
import model.Pixel;

public class Rotate implements Operation {
    private final int x1, y1, x2, y2, degrees;

    public Rotate(int x1, int y1, int x2, int y2, int degrees) {
        this.x1 = Math.min(x1, x2); this.y1 = Math.min(y1, y2);
        this.x2 = Math.max(x1, x2); this.y2 = Math.max(y1, y2);
        if (degrees != 90 && degrees != 180 && degrees != 270)
            throw new IllegalArgumentException("Degrees must be 90, 180, or 270.");
        this.degrees = degrees;
    }

    @Override
    public Image apply(Image img) {
        Crop.checkBounds(img, x1, y1, x2, y2);

        int rW = x2 - x1 + 1, rH = y2 - y1 + 1;

        // Extract region
        Pixel[][] block = new Pixel[rH][rW];
        for (int y = 0; y < rH; y++)
            for (int x = 0; x < rW; x++)
                block[y][x] = new Pixel(img.get(x1 + x, y1 + y));

        // Rotate block
        Pixel[][] rotated = rotateBlock(block, rH, rW);
        int newBH = rotated.length, newBW = rotated[0].length;

        // Resize canvas if region dimensions changed (90 or 270)
        int newW = img.getWidth()  + (newBW - rW);
        int newH = img.getHeight() + (newBH - rH);
        Image result = new Image(newW, newH);

        // Copy pixels outside the region
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++)
                if ((x < x1 || x > x2 || y < y1 || y > y2) && x < newW && y < newH)
                    result.set(x, y, new Pixel(img.get(x, y)));

        // Place rotated block
        for (int y = 0; y < newBH; y++)
            for (int x = 0; x < newBW; x++)
                if (x1 + x < newW && y1 + y < newH)
                    result.set(x1 + x, y1 + y, rotated[y][x]);

        return result;
    }

    private Pixel[][] rotateBlock(Pixel[][] src, int h, int w) {
        if (degrees == 90) {
            Pixel[][] dst = new Pixel[w][h];
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++)
                    dst[x][h - 1 - y] = src[y][x];
            return dst;
        } else if (degrees == 180) {
            Pixel[][] dst = new Pixel[h][w];
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++)
                    dst[h - 1 - y][w - 1 - x] = src[y][x];
            return dst;
        } else { // 270
            Pixel[][] dst = new Pixel[w][h];
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++)
                    dst[w - 1 - x][y] = src[y][x];
            return dst;
        }
    }

    @Override
    public String describe() {
        return "Rotate " + degrees + "° CW (" + x1 + "," + y1 + ")-(" + x2 + "," + y2 + ")";
    }
}