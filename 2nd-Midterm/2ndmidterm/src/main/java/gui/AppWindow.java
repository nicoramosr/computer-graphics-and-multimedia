package gui;

import api.OpenAIClient;
import api.MapboxClient;
import image.ImageGenerator;
import loader.MediaLoader;
import model.MediaFile;
import narrator.NarratorTTS;
import video.VideoAssembler;
import video.VideoAssembler.Slide;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class AppWindow extends JFrame {

    private final JTextField   inputField  = new JTextField(25);
    private final JTextField   outputField = new JTextField(25);
    private final JTextField   openaiField = new JTextField(25);
    private final JTextField   mapboxField = new JTextField(25);
    private final JProgressBar progress    = new JProgressBar(0, 100);
    private final JButton      runBtn      = new JButton("Create Video");

    private static final Color BG     = new Color(28, 28, 28);
    private static final Color FIELD  = new Color(42, 42, 42);
    private static final Color ACCENT = new Color(0, 200, 120);
    private static final Color FG     = new Color(220, 220, 220);

    public AppWindow() {
        super("GPS Video Creator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("GPS Video Creator");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(ACCENT);
        title.setAlignmentX(CENTER_ALIGNMENT);
        root.add(title);
        root.add(Box.createVerticalStrut(20));

        root.add(labeledField("Input folder",   inputField,  true,  false));
        root.add(Box.createVerticalStrut(8));
        root.add(labeledField("Output file",    outputField, false, true));
        root.add(Box.createVerticalStrut(8));
        root.add(labeledField("OpenAI API key", openaiField, false, false));
        root.add(Box.createVerticalStrut(8));
        root.add(labeledField("Mapbox token",   mapboxField, false, false));
        root.add(Box.createVerticalStrut(16));

        styleButton(runBtn);
        runBtn.setAlignmentX(CENTER_ALIGNMENT);
        runBtn.addActionListener(e -> onRun());
        root.add(runBtn);
        root.add(Box.createVerticalStrut(12));

        progress.setStringPainted(true);
        progress.setForeground(ACCENT);
        progress.setBackground(FIELD);
        progress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        root.add(progress);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel labeledField(String label, JTextField field,
                                boolean browse, boolean saveAs) {
        JPanel row = new JPanel(new BorderLayout(6, 4));
        row.setBackground(BG);
        JLabel lbl = new JLabel(label);
        lbl.setForeground(FG);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        row.add(lbl, BorderLayout.NORTH);
        styleField(field);
        if (browse || saveAs) {
            JPanel fieldRow = new JPanel(new BorderLayout(4, 0));
            fieldRow.setBackground(BG);
            fieldRow.add(field, BorderLayout.CENTER);
            JButton btn = new JButton(saveAs ? "Save as" : "Browse");
            btn.setBackground(FIELD);
            btn.setForeground(FG);
            btn.setFocusPainted(false);
            btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
            btn.addActionListener(e -> {
                JFileChooser fc = new JFileChooser();
                if (saveAs) {
                    fc.setSelectedFile(new File("output.mp4"));
                    if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                        String p = fc.getSelectedFile().getAbsolutePath();
                        if (!p.endsWith(".mp4")) p += ".mp4";
                        field.setText(p);
                    }
                } else {
                    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
                        field.setText(fc.getSelectedFile().getAbsolutePath());
                }
            });
            fieldRow.add(btn, BorderLayout.EAST);
            row.add(fieldRow, BorderLayout.CENTER);
        } else {
            row.add(field, BorderLayout.CENTER);
        }
        return row;
    }

    private void styleField(JTextField f) {
        f.setBackground(FIELD);
        f.setForeground(FG);
        f.setCaretColor(FG);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 70)),
            new EmptyBorder(4, 6, 4, 6)));
        f.setFont(new Font("SansSerif", Font.PLAIN, 12));
    }

    private void styleButton(JButton btn) {
        btn.setBackground(ACCENT);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 30, 10, 30));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void onRun() {
        String inputFolder = inputField.getText().trim();
        String outputFile  = outputField.getText().trim();
        String openaiKey   = openaiField.getText().trim();
        String mapboxToken = mapboxField.getText().trim();

        if (inputFolder.isEmpty() || outputFile.isEmpty()
                || openaiKey.isEmpty() || mapboxToken.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.",
                    "Missing fields", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!outputFile.endsWith(".mp4")) outputFile += ".mp4";
        final String finalOutput = outputFile;

        progress.setValue(0);
        runBtn.setEnabled(false);

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                String tempDir = Files.createTempDirectory("mvc_").toString();
                try {
                    runWorkflow(inputFolder, finalOutput, openaiKey, mapboxToken,
                                tempDir, pct -> publish(pct));
                } finally {
                    Files.walk(Path.of(tempDir))
                         .sorted(Comparator.reverseOrder())
                         .forEach(p -> p.toFile().delete());
                }
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                progress.setValue(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                runBtn.setEnabled(true);
                try {
                    get();
                    progress.setValue(100);
                    File out = new File(finalOutput);
                    if (out.exists()) Desktop.getDesktop().open(out.getParentFile());
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(AppWindow.this,
                        "Error: " + cause.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void runWorkflow(String inputFolder, String outputFile,
                             String openaiKey, String mapboxToken,
                             String tempDir,
                             java.util.function.Consumer<Integer> prog) throws Exception {

        OpenAIClient   openai    = new OpenAIClient(openaiKey);
        MapboxClient   mapbox    = new MapboxClient(mapboxToken);
        ImageGenerator imageGen  = new ImageGenerator(openaiKey);
        NarratorTTS    tts       = new NarratorTTS(tempDir);
        VideoAssembler assembler = new VideoAssembler(tempDir, outputFile);

        prog.accept(5);
        List<MediaFile> files = new MediaLoader(inputFolder).load();
        if (files.isEmpty()) throw new Exception("No media files found in folder.");
        System.out.println("[Workflow] Found " + files.size() + " media files.");

        MediaFile mapFirst = files.stream().filter(MediaFile::hasGps).findFirst()
                                  .orElse(files.get(0));
        MediaFile mapLast = null;
        for (int i = files.size() - 1; i >= 0; i--) {
            if (files.get(i).hasGps()) { mapLast = files.get(i); break; }
        }
        if (mapLast == null) mapLast = files.get(files.size() - 1);

        List<Slide> slides = new ArrayList<>();

        prog.accept(10);
        String introPrompt = openai.buildIntroPrompt(files);
        if (introPrompt == null || introPrompt.isBlank())
            introPrompt = "A breathtaking travel adventure across stunning landscapes";

        String introImg = tempDir + File.separator + "intro.png";
        boolean introGenerated = imageGen.generate(introPrompt, introImg);

        if (!introGenerated || !new File(introImg).exists()) {
            String fallback = tempDir + File.separator + "intro_fallback.jpg";
            Process conv = new ProcessBuilder(
                    "ffmpeg", "-y", "-i", files.get(0).getPath(),
                    "-vframes", "1", "-q:v", "2", fallback)
                .redirectErrorStream(true).start();
            conv.getInputStream().transferTo(OutputStream.nullOutputStream());
            conv.waitFor();
            introImg = new File(fallback).exists() ? fallback : files.get(0).getPath();
        }

        String introAudio = tts.synthesise("Welcome. " + introPrompt, "intro");
        slides.add(new Slide(introImg, introAudio, false));

        for (int i = 0; i < files.size(); i++) {
            prog.accept(15 + (int)(60.0 * i / files.size()));
            MediaFile mf = files.get(i);

            String visualPath = mf.getPath();
            if (mf.getType() == MediaFile.Type.PHOTO) {
                String converted = tempDir + File.separator + "photo_" + i + ".jpg";
                Process conv = new ProcessBuilder(
                        "ffmpeg", "-y", "-i", mf.getPath(),
                        "-vframes", "1", "-q:v", "2", converted)
                    .redirectErrorStream(true).start();
                conv.getInputStream().transferTo(OutputStream.nullOutputStream());
                conv.waitFor();
                if (new File(converted).exists()) visualPath = converted;
            }

            String desc;
            if (mf.getType() == MediaFile.Type.PHOTO) {
                desc = openai.describePhoto(mf.getPath());
            } else {
                desc = openai.describeVideo(mf.getLatitude(), mf.getLongitude());
            }
            if (desc == null || desc.isBlank())
                desc = "A beautiful moment on the journey.";

            String audio = tts.synthesise(desc, "slide_" + i);
            slides.add(new Slide(visualPath, audio, mf.getType() == MediaFile.Type.VIDEO));
        }

        prog.accept(78);
        String mapRaw   = tempDir + File.separator + "map_raw.png";
        String mapFinal = tempDir + File.separator + "map_final.png";

        boolean hasAnyGps = mapFirst.hasGps();
        if (hasAnyGps) {
            mapbox.downloadMap(mapFirst.getLatitude(), mapFirst.getLongitude(),
                               mapLast.getLatitude(),  mapLast.getLongitude(), mapRaw);
        }

        String phrase = "Every journey begins with a single step.";
        if (hasAnyGps) {
            String aiPhrase = openai.inspirationalPhrase(
                    mapFirst.getLatitude(), mapFirst.getLongitude(),
                    mapLast.getLatitude(),  mapLast.getLongitude());
            if (aiPhrase != null && !aiPhrase.isBlank()) phrase = aiPhrase;
        }

        if (new File(mapRaw).exists()) {
            burnText(mapRaw, phrase, mapFinal);
            if (!new File(mapFinal).exists())
                Files.copy(Path.of(mapRaw), Path.of(mapFinal), StandardCopyOption.REPLACE_EXISTING);
            String mapAudio = tts.synthesise(phrase, "map");
            slides.add(new Slide(mapFinal, mapAudio, false));
        }

        prog.accept(90);
        assembler.assemble(slides);
    }

    private void burnText(String input, String text, String output) {
        String safe = text
            .replace("\\", " ")
            .replace("'",  " ")
            .replace("\"", " ")
            .replace(":",  " ")
            .replace("%",  "pct")
            .trim();

        String[] fontPaths = {
            "C\\:/Windows/Fonts/arial.ttf",
            "C\\:/Windows/Fonts/calibri.ttf",
            "C\\:/Windows/Fonts/segoeui.ttf",
            ""
        };

        for (String fontPath : fontPaths) {
            String fontPart = fontPath.isEmpty() ? "" : "fontfile='" + fontPath + "':";
            String filter =
                "drawtext=" + fontPart +
                "text='" + safe + "':" +
                "fontsize=32:fontcolor=white:" +
                "shadowcolor=black:shadowx=2:shadowy=2:" +
                "box=1:boxcolor=black@0.55:boxborderw=14:" +
                "x=(w-text_w)/2:y=h-130";

            try {
                Process p = new ProcessBuilder("ffmpeg", "-y", "-i", input,
                        "-vf", filter, output)
                    .redirectErrorStream(true).start();
                p.getInputStream().transferTo(OutputStream.nullOutputStream());
                int exit = p.waitFor();
                if (exit == 0 && new File(output).exists()) return;
            } catch (Exception ignored) {}
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AppWindow::new);
    }
}