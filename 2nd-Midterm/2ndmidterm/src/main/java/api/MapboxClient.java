package api;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;

public class MapboxClient {

    private static final int W = 720;
    private static final int H = 1280;

    private final String token;

    public MapboxClient(String token) {
        this.token = token;
    }

    public void downloadMap(double firstLat, double firstLon,
                            double lastLat, double lastLon,
                            String outPath) throws Exception {

        boolean sameLocation = Math.abs(firstLat - lastLat) < 0.0001
                            && Math.abs(firstLon - lastLon) < 0.0001;

        String url;

        if (sameLocation) {
            url = "https://api.mapbox.com/styles/v1/mapbox/streets-v12/static/"
                + "pin-l-s+00cc44(" + fmt(firstLon) + "," + fmt(firstLat) + ")/"
                + fmt(firstLon) + "," + fmt(firstLat) + ",13/"
                + W + "x" + H
                + "?access_token=" + token;
        } else {
            url = "https://api.mapbox.com/styles/v1/mapbox/streets-v12/static/"
                + "pin-l-s+00cc44(" + fmt(firstLon) + "," + fmt(firstLat) + "),"
                + "pin-l-f+cc0000(" + fmt(lastLon) + "," + fmt(lastLat) + ")/"
                + "auto/"
                + W + "x" + H
                + "?padding=50&access_token=" + token;
        }

        try (InputStream in = new URI(url).toURL().openStream()) {
            Files.copy(in, Path.of(outPath), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String fmt(double v) {
        return String.format("%.6f", v);
    }
}