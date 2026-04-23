package loader;

import model.MediaFile;
import model.MediaFile.Type;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MediaLoader {

    private static final Set<String> PHOTO = Set.of("jpg", "jpeg", "png", "heic", "tiff");
    private static final Set<String> VIDEO = Set.of("mp4", "mov", "avi", "mkv");

    private static final DateTimeFormatter EXIF_FMT =
            DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

    private final String folderPath;

    public MediaLoader(String folderPath) {
        this.folderPath = folderPath;
    }

    public List<MediaFile> load() throws Exception {
        String exiftool = findExiftool();
        File[] files = new File(folderPath).listFiles();
        if (files == null) return Collections.emptyList();

        List<MediaFile> result = new ArrayList<>();
        for (File f : files) {
            MediaFile mf = parse(f, exiftool);
            if (mf != null) result.add(mf);
        }

        Collections.sort(result);
        return result;
    }

    private MediaFile parse(File file, String exiftool) {
        String ext = ext(file.getName());
        Type type;
        if      (PHOTO.contains(ext)) type = Type.PHOTO;
        else if (VIDEO.contains(ext)) type = Type.VIDEO;
        else return null;

        try {
            Process p = new ProcessBuilder(exiftool,
                    "-GPSLatitude#", "-GPSLongitude#", "-DateTimeOriginal",
                    file.getAbsolutePath())
                .redirectErrorStream(true).start();

            Map<String, String> tags = new LinkedHashMap<>();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    int colon = line.indexOf(':');
                    if (colon < 0) continue;
                    String key = line.substring(0, colon).trim()
                                     .toLowerCase()
                                     .replaceAll("[^a-z0-9]", "");
                    String val = line.substring(colon + 1).trim();
                    if (!val.isEmpty()) tags.put(key, val);
                }
            }
            p.waitFor();

            Double lat = extractDouble(tags, "gpslatitude");
            Double lon = extractDouble(tags, "gpslongitude");
            boolean hasGps = (lat != null && lon != null);
            if (!hasGps) { lat = 0.0; lon = 0.0; }

            LocalDateTime date;
            String dtVal = extractString(tags, "datetimeoriginal");
            if (dtVal != null) {
                try {
                    date = LocalDateTime.parse(dtVal, EXIF_FMT);
                } catch (Exception ignored) {
                    date = lastModified(file);
                }
            } else {
                date = lastModified(file);
            }

            return new MediaFile(file.getAbsolutePath(), type, lat, lon, date, hasGps);

        } catch (Exception e) {
            System.err.println("[MediaLoader] parse failed for " + file.getName() + ": " + e.getMessage());
            return null;
        }
    }

    private LocalDateTime lastModified(File file) {
        try {
            return LocalDateTime.ofInstant(
                java.nio.file.Files.getLastModifiedTime(file.toPath()).toInstant(),
                java.time.ZoneId.systemDefault());
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private Double extractDouble(Map<String, String> tags, String keyFragment) {
        for (Map.Entry<String, String> e : tags.entrySet()) {
            if (e.getKey().contains(keyFragment)) {
                try { return Double.parseDouble(e.getValue()); }
                catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    private String extractString(Map<String, String> tags, String keyFragment) {
        for (Map.Entry<String, String> e : tags.entrySet()) {
            if (e.getKey().contains(keyFragment)) return e.getValue();
        }
        return null;
    }

    private String findExiftool() throws Exception {
        if (canRun("exiftool"))     return "exiftool";
        if (canRun("exiftool.exe")) return "exiftool.exe";

        String jarDir = new File(MediaLoader.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).getParent();
        File local = new File(jarDir, "exiftool.exe");
        if (local.exists()) return local.getAbsolutePath();

        for (String g : new String[]{
                "C:\\Windows\\exiftool.exe",
                System.getProperty("user.home") + "\\Downloads\\exiftool.exe"}) {
            if (new File(g).exists()) return g;
        }

        throw new Exception("exiftool not found. Download from https://exiftool.org");
    }

    private boolean canRun(String cmd) {
        try {
            Process p = new ProcessBuilder(cmd, "-ver")
                .redirectErrorStream(true).start();
            p.getInputStream().transferTo(OutputStream.nullOutputStream());
            p.waitFor();
            return p.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static String ext(String name) {
        int i = name.lastIndexOf('.');
        return i >= 0 ? name.substring(i + 1).toLowerCase() : "";
    }
}