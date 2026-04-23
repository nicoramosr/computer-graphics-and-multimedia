package io;

import model.Image;
import model.Pixel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class ImageReader {

    public static Image read(String path) throws IOException {
        ImageFile imageFile = new ImageFile(path); // validates format

        BufferedImage bi;
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(imageFile.getFile()))) {
            bi = ImageIO.read(bis);
        }

        if (bi == null)
            throw new IOException("Could not read image: " + path);

        int w = bi.getWidth(), h = bi.getHeight();
        Image img = new Image(w, h);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = bi.getRGB(x, y);
                img.set(x, y, new Pixel(
                    (rgb >> 16) & 0xFF,
                    (rgb >>  8) & 0xFF,
                     rgb        & 0xFF));
            }
        }
        return img;
    }

    public static void write(Image img, String path) throws IOException {
        ImageFile imageFile = new ImageFile(path); // validates format

        int w = img.getWidth(), h = img.getHeight();
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Pixel p = img.get(x, y);
                int rgb = (p.r << 16) | (p.g << 8) | p.b;
                bi.setRGB(x, y, rgb);
            }
        }

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(imageFile.getFile()))) {
            boolean ok = ImageIO.write(bi, imageFile.getFormatName(), bos);
            if (!ok) throw new IOException("Could not write image: " + path);
        }
    }
}
