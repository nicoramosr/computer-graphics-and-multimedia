package model;

import java.time.LocalDateTime;

public class MediaFile implements Comparable<MediaFile> {

    public enum Type { PHOTO, VIDEO }

    private final String        path;
    private final Type          type;
    private final double        latitude;
    private final double        longitude;
    private final LocalDateTime dateTaken;
    private final boolean       hasGps;

    public MediaFile(String path, Type type, double latitude,
                     double longitude, LocalDateTime dateTaken, boolean hasGps) {
        this.path      = path;
        this.type      = type;
        this.latitude  = latitude;
        this.longitude = longitude;
        this.dateTaken = dateTaken;
        this.hasGps    = hasGps;
    }

    public MediaFile(String path, Type type, double latitude,
                     double longitude, LocalDateTime dateTaken) {
        this(path, type, latitude, longitude, dateTaken, true);
    }

    @Override
    public int compareTo(MediaFile other) {
        return this.dateTaken.compareTo(other.dateTaken);
    }

    public String getPath(){ 
        return path;
    }

    public Type getType(){ 
        return type;  
    }

    public double getLatitude(){
        return latitude;  
    }

    public double getLongitude(){
        return longitude;
    }

    public LocalDateTime getDateTaken(){
        return dateTaken; 
    }

    public boolean hasGps(){
        return hasGps;
    }
}