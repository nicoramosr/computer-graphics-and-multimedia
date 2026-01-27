import java.awt.*;
import java.awt.color.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class classwork {
    public static void main(String[] args) {
        BufferedImage img = new BufferedImage(1024, 768, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < 1024; x++) {
            for (int y = 0; y < 768; y++) {

                if (y > x*.75) {
                    img.setRGB(x, y, Color.BLUE.getRGB());
                } else if (y < x*.75) {
                    img.setRGB(x, y, Color.RED.getRGB());
                } else {
                    img.setRGB(x, y, Color.BLACK.getRGB());
                }

            }
        }
   
        File outputfile = new File("classwork_image.png");
        try {
            ImageIO.write(img, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
   
