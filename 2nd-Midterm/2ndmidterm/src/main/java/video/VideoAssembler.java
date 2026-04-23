package video;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class VideoAssembler {

    public static class Slide {
        private final String  visualPath;
        private final String  audioPath;
        private final boolean isVideo;

        public Slide(String visualPath, String audioPath, boolean isVideo) {
            this.visualPath = visualPath;
            this.audioPath  = audioPath;
            this.isVideo    = isVideo;
        }

        public String  visualPath() { return visualPath; }
        public String  audioPath()  { return audioPath;  }
        public boolean isVideo()    { return isVideo;    }
    }

    private static final int W          = 1080;
    private static final int H          = 1920;
    private static final int PHOTO_SECS = 5;
    private static final int FPS        = 30;

    private final String tempDir;
    private final String outputPath;

    public VideoAssembler(String tempDir, String outputPath) {
        this.tempDir    = tempDir;
        this.outputPath = outputPath;
    }

    public void assemble(List<Slide> slides) throws Exception {
        List<String> segments = new ArrayList<>();

        for (int i = 0; i < slides.size(); i++) {
            Slide  slide = slides.get(i);
            String seg   = tempDir + File.separator + "seg_" + i + ".mp4";

            if (!new File(slide.visualPath()).exists()) {
                System.err.println("Visual missing, skipping slide " + i);
                continue;
            }

            renderSlide(slide, seg, i);

            if (new File(seg).exists() && new File(seg).length() > 0) {
                segments.add(seg);
            } else {
                System.err.println("Segment not produced for slide " + i);
            }
        }

        if (segments.isEmpty()) throw new Exception("No slides rendered.");
        concatenate(segments);
    }

    private void renderSlide(Slide slide, String segPath, int idx) throws Exception {

        boolean hasAudio = slide.audioPath() != null
                        && new File(slide.audioPath()).exists();

        List<String> cmd = new ArrayList<>();
        cmd.add("ffmpeg");
        cmd.add("-y");

        if (slide.isVideo()) {
            cmd.addAll(List.of("-i", slide.visualPath()));

            if (hasAudio) {
                cmd.addAll(List.of("-i", slide.audioPath()));
            } else {
                cmd.addAll(List.of("-f", "lavfi", "-i",
                        "anullsrc=channel_layout=stereo:sample_rate=44100"));
            }

            String scale =
                "scale=" + W + ":" + H + ":force_original_aspect_ratio=decrease," +
                "pad="   + W + ":" + H + ":(ow-iw)/2:(oh-ih)/2:black,setsar=1";

            cmd.addAll(List.of("-vf", scale));
            cmd.addAll(List.of(
                "-c:v", "libx264", "-crf", "23", "-preset", "fast",
                "-pix_fmt", "yuv420p", "-r", String.valueOf(FPS),
                "-c:a", "aac",
                "-shortest"
            ));

        } else {
            double slideDuration = PHOTO_SECS;
            if (hasAudio) {
                double audioDur = getAudioDuration(slide.audioPath());
                slideDuration = Math.max(audioDur, PHOTO_SECS);
            }

            cmd.addAll(List.of("-i", slide.visualPath()));

            if (hasAudio) {
                cmd.addAll(List.of("-i", slide.audioPath()));
            } else {
                cmd.addAll(List.of("-f", "lavfi", "-i",
                        "anullsrc=channel_layout=stereo:sample_rate=44100"));
            }

            String vf =
                "loop=loop=-1:size=1:start=0," +
                "scale=" + W + ":" + H + ":force_original_aspect_ratio=decrease," +
                "pad="   + W + ":" + H + ":(ow-iw)/2:(oh-ih)/2:black,setsar=1," +
                "fps=" + FPS;

            cmd.addAll(List.of("-vf", vf));
            cmd.addAll(List.of(
                "-c:v", "libx264", "-crf", "23", "-preset", "fast",
                "-pix_fmt", "yuv420p",
                "-c:a", "aac",
                "-t", String.format("%.3f", slideDuration)
            ));
        }

        cmd.add(segPath);

        System.out.println("Rendering slide " + idx + (slide.isVideo() ? " [video]" : " [image]"));
        run(cmd);
    }

    private double getAudioDuration(String wavPath) {
        try {
            Process p = new ProcessBuilder(
                    "ffprobe", "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    wavPath)
                .redirectErrorStream(true)
                .start();

            String out;
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()))) {
                out = br.lines().findFirst().orElse("").trim();
            }
            p.waitFor();
            return out.isEmpty() ? PHOTO_SECS : Double.parseDouble(out);
        } catch (Exception e) {
            return PHOTO_SECS;
        }
    }

    private void concatenate(List<String> segments) throws Exception {
        String listFile = tempDir + File.separator + "concat.txt";
        try (PrintWriter pw = new PrintWriter(listFile)) {
            for (String s : segments)
                pw.println("file '" + s.replace("\\", "/") + "'");
        }

        run(List.of(
            "ffmpeg", "-y",
            "-f", "concat", "-safe", "0",
            "-i", listFile,
            "-c:v", "libx264", "-crf", "23",
            "-preset", "fast", "-pix_fmt", "yuv420p",
            "-c:a", "aac",
            outputPath
        ));
    }

    private void run(List<String> cmd) throws Exception {
        Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null)
                System.out.println("[ffmpeg] " + line);
        }
        int exitCode = p.waitFor();
        if (exitCode != 0)
            throw new Exception("FFmpeg exited with code " + exitCode);
    }
}