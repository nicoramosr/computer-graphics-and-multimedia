import java.io.InputStream;

public class App {
    String[] command = new String[]{"curl", "-X", "POST", "http://localhost:8080/api/v1/hello", "-H", "Content-Type: application/json", "-d", "{\"name\":\"John\"}"};

    final ProcessBuilder processBuilder = new ProcessBuilder();

    public void run() throws IOException, InterruptedException {
        try {
            final Process process = processBuilder.command(command).start();
            InputStream inputStream = process.getInputStream();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
