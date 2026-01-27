
import java.awt.*;
import java.awt.color.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Main {
    public static void main(String[] args) {
        BufferedImage img = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);

        for(int x=0; x<100; x++) {
            for(int y=0; y<100; y++) {
                img.setRGB(x, y, Color.RED.getRGB());
            }
        }

        File outputfile = new File("image.png");
        try {
            ImageIO.write(img, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}