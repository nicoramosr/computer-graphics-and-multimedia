package operations;

import model.Image;

public class Invert implements Operation {
    private final int x1, y1, x2, y2;

    public Invert(int x1, int y1, int x2, int y2) {
        this.x1 = Math.min(x1, x2); this.y1 = Math.min(y1, y2);
        this.x2 = Math.max(x1, x2); this.y2 = Math.max(y1, y2);
    }

    @Override
    public Image apply(Image img) {
        Crop.checkBounds(img, x1, y1, x2, y2);
        Image result = new Image(img);
        for (int y = y1; y <= y2; y++)
            for (int x = x1; x <= x2; x++)
                result.set(x, y, img.get(x, y).inverted());
        return result;
    }

    @Override
    public String describe() {
        return "Invert (" + x1 + "," + y1 + ")-(" + x2 + "," + y2 + ")";
    }
}
