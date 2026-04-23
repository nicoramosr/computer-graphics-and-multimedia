package io;
import java.io.File;
//validates that the file is a .jpg, .jpeg or .png and provides the format name for ImageIO
    
public class ImageFile {

    private final File file;    

    public ImageFile(String path) {
        if (!path.endsWith(".jpg") && !path.endsWith(".jpeg") && !path.endsWith(".png"))
            throw new IllegalArgumentException("Only .jpg, .jpeg and .png files are allowed: " + path);
        this.file = new File(path);
    }

    public File getFile() { return file; }

    public String getFormatName() {
        return file.getName().endsWith(".png") ? "png" : "jpg";
    }
}