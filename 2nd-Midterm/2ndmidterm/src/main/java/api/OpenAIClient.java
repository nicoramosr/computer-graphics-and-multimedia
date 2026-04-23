package api;

import model.MediaFile;
import org.json.*;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;

public class OpenAIClient {

    private static final String URL   = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o";

    private final String apiKey;
    private final HttpClient http = HttpClient.newHttpClient();

    public OpenAIClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public String describePhoto(String imagePath) {
        try {
            BufferedImage img = readImageSafe(imagePath);

            if (img == null) {
                return "A stunning travel photo captured along the journey.";
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", baos);
            String b64 = Base64.getEncoder().encodeToString(baos.toByteArray());

            JSONArray content = new JSONArray()
                .put(new JSONObject()
                    .put("type", "text")
                    .put("text", "Describe this travel photo in one or two engaging sentences."))
                .put(new JSONObject()
                    .put("type", "image_url")
                    .put("image_url", new JSONObject()
                        .put("url", "data:image/jpeg;base64," + b64)));

            return call(content);

        } catch (Exception e) {
            return "A beautiful moment captured along the way.";
        }
    }

    private BufferedImage readImageSafe(String imagePath) {
        try {
            BufferedImage img = ImageIO.read(new File(imagePath));
            if (img != null) return img;
        } catch (Exception ignored) {}

        try {
            File tmp = File.createTempFile("mvc_conv_", ".jpg");
            tmp.deleteOnExit();

            Process p = new ProcessBuilder(
                    "ffmpeg", "-y", "-i", imagePath,
                    "-vframes", "1",
                    "-q:v", "2",
                    tmp.getAbsolutePath())
                .redirectErrorStream(true)
                .start();
            p.getInputStream().transferTo(OutputStream.nullOutputStream());
            p.waitFor();

            if (tmp.exists() && tmp.length() > 0) {
                BufferedImage img = ImageIO.read(tmp);
                tmp.delete();
                return img;
            }
        } catch (Exception e) {
            System.err.println("[OpenAIClient] FFmpeg fallback failed: " + e.getMessage());
        }

        return null;
    }

    public String describeVideo(double lat, double lon) {
        return callText(
            "A traveler filmed a short video clip near coordinates (" + lat + ", " + lon + "). " +
            "Write one engaging sentence describing what this moment might look like.");
    }

    public String buildIntroPrompt(List<MediaFile> files) {
        MediaFile first = files.get(0);
        MediaFile last  = files.get(files.size() - 1);
        return callText(
            "I have " + files.size() + " travel photos/videos " +
            "from coordinates (" + first.getLatitude() + ", " + first.getLongitude() + ") " +
            "to (" + last.getLatitude() + ", " + last.getLongitude() + "). " +
            "Write a vivid cinematic image-generation prompt in max 25 words. " +
            "Output only the prompt, nothing else.");
    }

    public String inspirationalPhrase(double firstLat, double firstLon,
                                      double lastLat, double lastLon) {
        return callText(
            "Write a single short inspirational travel phrase of max 12 words " +
            "inspired by a journey from (" + firstLat + ", " + firstLon + ") " +
            "to (" + lastLat + ", " + lastLon + "). " +
            "Output only the phrase, nothing else.");
    }

    private String callText(String prompt) {
        JSONArray content = new JSONArray()
            .put(new JSONObject().put("type", "text").put("text", prompt));
        return call(content);
    }

    private String call(JSONArray content) {
        try {
            JSONObject body = new JSONObject()
                .put("model", MODEL)
                .put("messages", new JSONArray()
                    .put(new JSONObject()
                        .put("role", "user")
                        .put("content", content)))
                .put("max_tokens", 200);

            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            JSONObject json = new JSONObject(res.body());

            if (json.has("error")) {
                System.err.println("[OpenAIClient] " + json.getJSONObject("error").getString("message"));
                return "";
            }

            return json.getJSONArray("choices")
                       .getJSONObject(0)
                       .getJSONObject("message")
                       .getString("content")
                       .trim();

        } catch (Exception e) {
            System.err.println("[OpenAIClient] call failed: " + e.getMessage());
            return "";
        }
    }
}