package narrator;

import java.io.*;
import java.nio.file.*;

public class NarratorTTS {

    private final String tempDir;

    public NarratorTTS(String tempDir) {
        this.tempDir = tempDir;
    }

    public String synthesise(String text, String name) {
        if (text == null || text.isBlank()) return null;

        String wav = tempDir + File.separator + name + ".wav";
        String os  = System.getProperty("os.name").toLowerCase();

        String safe = text
            .replace("\\", " ")
            .replace("\"", " ")
            .replace("'",  " ")
            .replace("`",  " ")
            .replace("$",  " ")
            .replace("\n", " ")
            .replace("\r", " ")
            .trim();

        try {
            Process p;

            if (os.contains("win")) {
                String ps1Path = tempDir + File.separator + name + ".ps1";
                String script =
                    "Add-Type -AssemblyName System.Speech\r\n" +
                    "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer\r\n" +
                    "$s.SetOutputToWaveFile(\"" + wav.replace("\\", "\\\\") + "\")\r\n" +
                    "$s.Speak(\"" + safe + "\")\r\n" +
                    "$s.Dispose()\r\n";

                Files.writeString(Path.of(ps1Path), script);

                p = new ProcessBuilder(
                        "powershell.exe",
                        "-NonInteractive",
                        "-NoProfile",
                        "-ExecutionPolicy", "Bypass",
                        "-File", ps1Path)
                    .redirectErrorStream(true)
                    .start();

            } else if (os.contains("mac")) {
                p = new ProcessBuilder(
                        "say", "-o", wav,
                        "--data-format=LEF32@22050", safe)
                    .redirectErrorStream(true)
                    .start();
            } else {
                p = new ProcessBuilder(
                        "espeak-ng", "-w", wav, safe)
                    .redirectErrorStream(true)
                    .start();
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null)
                    System.out.println("[TTS] " + line);
            }
            p.waitFor();

            boolean exists = Files.exists(Path.of(wav));
            System.out.println("[TTS] " + name + " -> " + (exists ? "OK" : "FAILED"));
            return exists ? wav : null;

        } catch (Exception e) {
            System.err.println("[NarratorTTS] failed for '" + name + "': " + e.getMessage());
            return null;
        }
    }
}