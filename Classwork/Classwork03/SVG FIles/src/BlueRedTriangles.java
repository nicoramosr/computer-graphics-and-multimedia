import java.io.*;

public class BlueRedTriangles {
    public static void main(String[] args) {
        try{
            FileWriter f = new FileWriter("blueRedTriangles.svg");
            f.write("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"800\" height=\"600\">");
            f.write("<polygon points=\"0,0 800,0 800,600\" fill=\"red\"/>");
            f.write("<polygon points=\"0,0 0,600 800,600\" fill=\"blue\"/>");
            f.write("</svg>");
            f.close();
        } catch(IOException e){
            e.printStackTrace();
        }

    }
}
