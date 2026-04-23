package ui;

import io.ImageReader;
import model.Image;
import model.Pixel;
import operations.Crop;
import operations.Invert;
import operations.Rotate;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageEditorUI extends JFrame {

    // Current working image
    private Image currentImage;

    // Preview panel
    private final ImagePreviewPanel previewPanel = new ImagePreviewPanel();

    // Status bar
    private final JLabel statusLabel = new JLabel("No image loaded.");

    // Crop fields
    private final JTextField cropX1 = new JTextField("0", 4);
    private final JTextField cropY1 = new JTextField("0", 4);
    private final JTextField cropX2 = new JTextField("100", 4);
    private final JTextField cropY2 = new JTextField("100", 4);

    // Invert fields
    private final JTextField invX1 = new JTextField("0", 4);
    private final JTextField invY1 = new JTextField("0", 4);
    private final JTextField invX2 = new JTextField("100", 4);
    private final JTextField invY2 = new JTextField("100", 4);

    // Rotate fields
    private final JTextField rotX1 = new JTextField("0", 4);
    private final JTextField rotY1 = new JTextField("0", 4);
    private final JTextField rotX2 = new JTextField("100", 4);
    private final JTextField rotY2 = new JTextField("100", 4);
    private final JComboBox<String> rotDegrees = new JComboBox<>(new String[]{"90", "180", "270"});

    public ImageEditorUI() {
        super("Java Image Editor");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        add(buildToolPanel(), BorderLayout.WEST);
        add(previewPanel,     BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        setVisible(true);
    }

    //Panels

    private JPanel buildToolPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.setPreferredSize(new Dimension(220, 0));

        panel.add(buildUploadPanel());
        panel.add(Box.createVerticalStrut(8));
        panel.add(buildCropPanel());
        panel.add(Box.createVerticalStrut(8));
        panel.add(buildInvertPanel());
        panel.add(Box.createVerticalStrut(8));
        panel.add(buildRotatePanel());
        panel.add(Box.createVerticalStrut(8));
        panel.add(buildSavePanel());

        return panel;
    }

    private JPanel buildUploadPanel() {
        JPanel panel = titledPanel("Image");
        JButton uploadBtn = new JButton("Upload JPG / PNG");
        uploadBtn.setAlignmentX(CENTER_ALIGNMENT);
        uploadBtn.addActionListener(e -> uploadImage());
        panel.add(uploadBtn);
        return panel;
    }

    private JPanel buildCropPanel() {
        JPanel panel = titledPanel("Crop");
        panel.add(coordRow("x1", cropX1, "y1", cropY1));
        panel.add(coordRow("x2", cropX2, "y2", cropY2));
        JButton btn = new JButton("Apply Crop");
        btn.addActionListener(e -> applyCrop());
        panel.add(btn);
        return panel;
    }

    private JPanel buildInvertPanel() {
        JPanel panel = titledPanel("Invert Colors");
        panel.add(coordRow("x1", invX1, "y1", invY1));
        panel.add(coordRow("x2", invX2, "y2", invY2));
        JButton btn = new JButton("Apply Invert");
        btn.addActionListener(e -> applyInvert());
        panel.add(btn);
        return panel;
    }

    private JPanel buildRotatePanel() {
        JPanel panel = titledPanel("Rotate");
        panel.add(coordRow("x1", rotX1, "y1", rotY1));
        panel.add(coordRow("x2", rotX2, "y2", rotY2));
        JPanel degRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        degRow.add(new JLabel("Degrees:"));
        degRow.add(rotDegrees);
        panel.add(degRow);
        JButton btn = new JButton("Apply Rotate");
        btn.addActionListener(e -> applyRotate());
        panel.add(btn);
        return panel;
    }

    private JPanel buildSavePanel() {
        JPanel panel = titledPanel("Save");
        JButton btn = new JButton("Save Image");
        btn.addActionListener(e -> saveImage());
        panel.add(btn);
        return panel;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setBorder(BorderFactory.createEtchedBorder());
        bar.add(statusLabel);
        return bar;
    }

    //Actions

    private void uploadImage() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Open JPG or PNG");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images (jpg, png)", "jpg", "jpeg", "png"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            currentImage = ImageReader.read(fc.getSelectedFile().getAbsolutePath());
            previewPanel.setImage(toBufferedImage(currentImage));
            status("Loaded: " + fc.getSelectedFile().getName() + "  (" + currentImage + ")");
        } catch (Exception e) {
            error("Could not load image: " + e.getMessage());
        }
    }

    private void applyCrop() {
        if (!checkImageLoaded()) return;
        try {
            int[] r = regionFrom(cropX1, cropY1, cropX2, cropY2);
            currentImage = new Crop(r[0], r[1], r[2], r[3]).apply(currentImage);
            previewPanel.setImage(toBufferedImage(currentImage));
            status("Cropped. New size: " + currentImage);
        } catch (Exception e) {
            error(e.getMessage());
        }
    }

    private void applyInvert() {
        if (!checkImageLoaded()) return;
        try {
            int[] r = regionFrom(invX1, invY1, invX2, invY2);
            currentImage = new Invert(r[0], r[1], r[2], r[3]).apply(currentImage);
            previewPanel.setImage(toBufferedImage(currentImage));
            status("Colors inverted in region.");
        } catch (Exception e) {
            error(e.getMessage());
        }
    }

    private void applyRotate() {
        if (!checkImageLoaded()) return;
        try {
            int[] r = regionFrom(rotX1, rotY1, rotX2, rotY2);
            int deg = Integer.parseInt((String) rotDegrees.getSelectedItem());
            currentImage = new Rotate(r[0], r[1], r[2], r[3], deg).apply(currentImage);
            previewPanel.setImage(toBufferedImage(currentImage));
            status("Rotated " + deg + "°. New size: " + currentImage);
        } catch (Exception e) {
            error(e.getMessage());
        }
    }

    private void saveImage() {
        if (!checkImageLoaded()) return;
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save as JPG or PNG");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images (jpg, png)", "jpg", "jpeg", "png"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            String path = fc.getSelectedFile().getAbsolutePath();
            // Add .jpg extension if user didn't type one
            if (!path.endsWith(".jpg") && !path.endsWith(".jpeg") && !path.endsWith(".png"))
                path += ".jpg";
            ImageReader.write(currentImage, path);
            status("Saved: " + new File(path).getName());
        } catch (Exception e) {
            error("Could not save: " + e.getMessage());
        }
    }

    //Converts our Image model to a BufferedImage for display
    private BufferedImage toBufferedImage(Image img) {
        BufferedImage bi = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++) {
                Pixel p = img.get(x, y);
                bi.setRGB(x, y, (p.r << 16) | (p.g << 8) | p.b);
            }
        return bi;
    }

    private int[] regionFrom(JTextField x1, JTextField y1, JTextField x2, JTextField y2) {
        return new int[]{
            Integer.parseInt(x1.getText().trim()),
            Integer.parseInt(y1.getText().trim()),
            Integer.parseInt(x2.getText().trim()),
            Integer.parseInt(y2.getText().trim())
        };
    }

    private boolean checkImageLoaded() {
        if (currentImage == null) { error("Please upload an image first."); return false; }
        return true;
    }

    private void status(String msg) { statusLabel.setText(msg); }
    private void error(String msg)  { statusLabel.setText("Error: " + msg); JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }

    private JPanel titledPanel(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), title, TitledBorder.LEFT, TitledBorder.TOP));
        return p;
    }

    private JPanel coordRow(String l1, JTextField f1, String l2, JTextField f2) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        row.add(new JLabel(l1 + ":")); row.add(f1);
        row.add(new JLabel(l2 + ":")); row.add(f2);
        return row;
    }

    static class ImagePreviewPanel extends JPanel {
        private BufferedImage image;

        ImagePreviewPanel() {
            setBackground(Color.DARK_GRAY);
            setBorder(BorderFactory.createLoweredBevelBorder());
        }

        void setImage(BufferedImage img) {
            this.image = img;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image == null) {
                g.setColor(Color.GRAY);
                g.drawString("No image loaded", getWidth() / 2 - 50, getHeight() / 2);
                return;
            }
            // Scale image to fit the panel while keeping aspect ratio
            double scale = Math.min((double) getWidth() / image.getWidth(), (double) getHeight() / image.getHeight());
            int w = (int) (image.getWidth()  * scale);
            int h = (int) (image.getHeight() * scale);
            int x = (getWidth()  - w) / 2;
            int y = (getHeight() - h) / 2;
            g.drawImage(image, x, y, w, h, null);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ImageEditorUI::new);
    }
}