import java.awt.*;
import java.awt.color.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class BarycentricCoordinates {
    public static void main(String[] args) {
        BufferedImage image = new BufferedImage(800, 800, BufferedImage.TYPE_INT_RGB);
        
        Point p1 = new Point(100, 100);
        Point p2 = new Point(700, 150);
        Point p3 = new Point(400, 700);
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Point P = new Point(x, y);
                double denominator = ((p2.y - p3.y) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.y - p3.y));
                double a = ((p2.y - p3.y) * (P.x - p3.x) + (p3.x - p2.x) * (P.y - p3.y)) / denominator;
                double b = ((p3.y - p1.y) * (P.x - p3.x) + (p1.x - p3.x) * (P.y - p3.y)) / denominator;
                double c = 1 - a - b;
                if (a >= 0 && b >= 0 && c >= 0) {
                    int red = (int)(a * 255);
                    int green = (int)(b * 255);
                    int blue = (int)(c * 255);
                    Color color = new Color(red, green, blue);
                    image.setRGB(x, y, color.getRGB());
                } else {
                    image.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }   
        try {
            ImageIO.write(image, "png", new File("barycentric_coordinates.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


