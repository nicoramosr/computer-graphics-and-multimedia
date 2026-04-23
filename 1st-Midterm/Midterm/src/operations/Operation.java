package operations;

import model.Image;


// interface for all image editing operations.
 
public interface Operation {
    Image apply(Image image);
    String describe();
}