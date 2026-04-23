package image;

import org.json.*;

import java.net.URI;
import java.net.http.*;
import java.nio.file.*;

public class ImageGenerator {

    private static final String URL = "https://api.openai.com/v1/images/generations";

    private final String apiKey;
    private final HttpClient http = HttpClient.newHttpClient();

    public ImageGenerator(String apiKey) {
        this.apiKey = apiKey;
    }

    public boolean generate(String prompt, String outPath) {
        try {
            JSONObject body = new JSONObject()
                .put("model", "dall-e-3")
                .put("prompt", prompt)
                .put("n", 1)
                .put("size", "1024x1792")
                .put("response_format", "url");

            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            JSONObject json = new JSONObject(res.body());

            if (json.has("error")) {
                System.err.println("[ImageGenerator] " + json.getJSONObject("error").getString("message"));
                return false;
            }

            String imageUrl = json.getJSONArray("data").getJSONObject(0).getString("url");

            try (var in = new URI(imageUrl).toURL().openStream()) {
                Files.copy(in, Path.of(outPath), StandardCopyOption.REPLACE_EXISTING);
            }

            return true;

        } catch (Exception e) {
            System.err.println("[ImageGenerator] generate failed: " + e.getMessage());
            return false;
        }
    }
}