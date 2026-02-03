import java.awt.*;
import java.awt.color.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Clock {

    public static void main(String[] args) {
        BufferedImage image = new BufferedImage(1024, 768, BufferedImage.TYPE_INT_RGB);

        for(int x=0; x<1024; x++) {
            for(int y=0; y<768; y++) {                              //background color
                image.setRGB(x, y, Color.black.getRGB());
            }
        }

        int wCenter = 512;
        int hCenter = 384;      //center of the image (width/2, height/2)
        int r = 300;            //radius

        for(double angle=0; angle<2*Math.PI; angle+=.001){
                int x = (int)(wCenter + r * Math.cos(angle));
                int y = (int)(hCenter + r * Math.sin(angle));       //circle
                image.setRGB(x, y, Color.white.getRGB());
            }
        
        for(double angle = 0; angle<2*Math.PI; angle+= Math.PI/6){   //hour marks
            int x = (int)(wCenter + (r-20) * Math.cos(angle));
            int y = (int)(hCenter + (r-20) * Math.sin(angle));
            image.setRGB(x, y, Color.white.getRGB());
    
            for(int dx = -1; dx <= 1; dx++) {
                for(int dy = -1; dy <= 1; dy++) {                       //making the hour marks thicker
                    image.setRGB(x + dx, y + dy, Color.white.getRGB());
                }
            }
        }

        int hour = 1;
        int minutes = 30;

        double hourAngle = (hour * 2*Math.PI / 12) + (minutes * 2*Math.PI / (12*60));   //hour hand angle
        double minuteAngle = (minutes * 2*Math.PI / 60);                                //minute hand angle

        int hourLength = 100;
        int hourX = (int)(wCenter + hourLength * Math.cos(hourAngle - Math.PI/2));
        int hourY = (int)(hCenter + hourLength * Math.sin(hourAngle - Math.PI/2));

        for(double t = 0; t <= 1; t += 0.01) {
            int x = (int)(wCenter + (hourX - wCenter) * t);
            int y = (int)(hCenter + (hourY - hCenter) * t);
            image.setRGB(x, y, Color.white.getRGB());
        }

        int minuteLength = 200;
        int minuteX = (int)(wCenter + minuteLength * Math.cos(minuteAngle - Math.PI/2));
        int minuteY = (int)(hCenter + minuteLength * Math.sin(minuteAngle - Math.PI/2));

        for(double t = 0; t <= 1; t += 0.01) {
            int x = (int)(wCenter + (minuteX - wCenter) * t);
            int y = (int)(hCenter + (minuteY - hCenter) * t);
            image.setRGB(x, y, Color.white.getRGB());
        }
        
        File outputfile = new File("clock.png");
        try {
            ImageIO.write(image, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        }
    }



