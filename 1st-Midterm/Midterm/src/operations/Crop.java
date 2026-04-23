package operations;

import model.Image;
import model.Pixel;

public class Crop implements Operation {
    private final int x1, y1, x2, y2;

    public Crop(int x1, int y1, int x2, int y2) {
        this.x1 = Math.min(x1, x2); this.y1 = Math.min(y1, y2);
        this.x2 = Math.max(x1, x2); this.y2 = Math.max(y1, y2);
    }

    @Override   //new image with dimensions of the crop region, and pixels copied from the original image
    public Image apply(Image img) {
        checkBounds(img, x1, y1, x2, y2);
        Image result = new Image(x2 - x1 + 1, y2 - y1 + 1);
        for (int y = y1; y <= y2; y++)
            for (int x = x1; x <= x2; x++)
                result.set(x - x1, y - y1, new Pixel(img.get(x, y)));
        return result;
    }

    @Override //describes the operation and its parameters for display in the UI
    public String describe() {
        return "Crop (" + x1 + "," + y1 + ")-(" + x2 + "," + y2 + ")";
    }
        //checks if it is inside image bounds
    static void checkBounds(Image img, int lx, int ly, int rx, int ry) {
        if (lx < 0 || ly < 0 || rx >= img.getWidth() || ry >= img.getHeight())
            throw new IllegalArgumentException(
                "Region (" + lx + "," + ly + ")-(" + rx + "," + ry + ") out of bounds for " + img);
    }
}
