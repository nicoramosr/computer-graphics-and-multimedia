import java.awt.*;
import java.awt.color.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Landscape {
    public static void main(String[] args) {
        BufferedImage image = new BufferedImage(1024, 768, BufferedImage.TYPE_INT_RGB);
        for(int x=0; x<1024; x++) {
            for(int y=0; y<768; y++) {                              //background color
                image.setRGB(x, y, Color.white.getRGB());
            }
        }
        
        int r=100;
        int wCenter=200;
        int hCenter=150;

        for(int x=0; x<1024; x++) {
            for(int y=0; y<768; y++) {
                int distance = (x-wCenter)*(x-wCenter) + (y-hCenter)*(y-hCenter);   
                if(distance <= r*r) {                                                       //sun
                    image.setRGB(x, y, Color.yellow.getRGB());
                }
            }
        }

        for(double angle=0; angle<2*Math.PI; angle+=Math.PI/4){
            int startX = (int)(wCenter + r * Math.cos(angle));
            int startY = (int)(hCenter + r * Math.sin(angle));
            int endX = (int)(wCenter + (r+50) * Math.cos(angle));
            int endY = (int)(hCenter + (r+50) * Math.sin(angle));
            
            // draw line from start to end
            for(int i=0; i<=50; i++){
                int x = startX + (endX - startX) * i / 50;
                int y = startY + (endY - startY) * i / 50;
                image.setRGB(x, y, Color.red.getRGB());
            }
        }

        //grass
        for(int x=0; x<1024; x++) {
            int grassHeight = (int)(500 + 50 * Math.cos(x * 0.05));
            for(int y=grassHeight; y<768; y++) {
                image.setRGB(x, y, Color.green.getRGB());
            }
        }

        File outputFile = new File("landscape.png");
        try {
            ImageIO.write(image, "png", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}