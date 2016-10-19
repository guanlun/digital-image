/**
 * Name: Guanlun Zhao
 * UIN: 525001939
 * Email: guanlun@tamu.edu
 *
 * Compilation:
 * Developed using Java 8. With previous versions of JDK you should also be able to compile it.
 *
 * Commands:
 * javac Main.java
 * java Main
 *
 * Features:
 * - Blur
 * - Edge detection
 * - Dilation
 * - Erosion
 * - Smart blur based on edge detection
 */
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class RGB {
    int red;
    int green;
    int blue;

    RGB(int r, int g, int b) {
        this.red = r;
        this.green = g;
        this.blue = b;
    }

    int toGrayScale() {
        return (this.red + this.green + this.blue) / 3;
    }

    static RGB fromInt(int rgb) {
        int r = (rgb & 0x00ff0000) >> 16;
        int g = (rgb & 0x0000ff00) >> 8;
        int b = (rgb & 0x000000ff);

        return new RGB(r, g, b);
    }

    int toInt() {
        return (this.red << 16) | (this.green << 8) | (this.blue);
    }
}

class Vec2 {
    double x;
    double y;

    Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    Vec2 sub(Vec2 v) {
        return new Vec2(this.x - v.x, this.y - v.y);
    }

    double dim() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }
}

class Filter {
    static final int BLUR = 0;
    static final int EDGE_DETECTION = 1;
    static final int DILATION = 2;
    static final int EROSION = 3;
    static final int SMART_BLUR = 4;

    static final double[][] BLUR_KERNEL = {
            { 0.000036, 0.000363, 0.001446, 0.002291, 0.001446, 0.000363, 0.000036 },
            { 0.000363, 0.003676, 0.014662, 0.023226, 0.014662, 0.003676, 0.000363 },
            { 0.001446, 0.014662, 0.058488, 0.092651, 0.058488, 0.014662, 0.001446 },
            { 0.002291, 0.023226, 0.092651, 0.146768, 0.092651, 0.023226, 0.002291 },
            { 0.001446, 0.014662, 0.058488, 0.092651, 0.058488, 0.014662, 0.001446 },
            { 0.000363, 0.003676, 0.014662, 0.023226, 0.014662, 0.003676, 0.000363 },
            { 0.000036, 0.000363, 0.001446, 0.002291, 0.001446, 0.000363, 0.000036 }
    };

    static final double[][] SOBEL_Y_KERNEL = {
            {1, 2, 1},
            {0, 0, 0},
            {-1, -2, -1}
    };

    static final double[][] SOBEL_X_KERNEL = {
            {-1, 0, 1},
            {-2, 0, 2},
            {-1, 0, 1}
    };

    static final boolean[][] MORPH_KERNEL = {
            {false, false, false, true, true, true, true, true, false, false, false},
            {false, false, true, true, true, true, true, true, true, false, false},
            {false, true, true, true, true, true, true, true, true, true, false},
            {true, true, true, true, true, true, true, true, true, true, true},
            {true, true, true, true, true, true, true, true, true, true, true},
            {true, true, true, true, true, true, true, true, true, true, true},
            {true, true, true, true, true, true, true, true, true, true, true},
            {true, true, true, true, true, true, true, true, true, true, true},
            {false, true, true, true, true, true, true, true, true, true, false},
            {false, false, true, true, true, true, true, true, true, false, false},
            {false, false, false, true, true, true, true, true, false, false, false},
    };

    static BufferedImage detectEdge(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int halfSize = SOBEL_X_KERNEL.length / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gxr = 0;
                int gxg = 0;
                int gxb = 0;

                int gyr = 0;
                int gyg = 0;
                int gyb = 0;

                for (int ky = -halfSize; ky <= halfSize; ky++) {
                    for (int kx = -halfSize; kx <= halfSize; kx++) {
                        int sy = y + ky;
                        int sx = x + kx;

                        if ((sx >= 0) && (sx <= width - 1) && (sy >= 0) && (sy <= height - 1)) {
                            RGB color = RGB.fromInt(img.getRGB(sx, sy));

                            gxr += color.red * SOBEL_X_KERNEL[ky + halfSize][kx + halfSize];
                            gxg += color.green * SOBEL_X_KERNEL[ky + halfSize][kx + halfSize];
                            gxb += color.blue * SOBEL_X_KERNEL[ky + halfSize][kx + halfSize];

                            gyr += color.red * SOBEL_Y_KERNEL[ky + halfSize][kx + halfSize];
                            gyg += color.green * SOBEL_Y_KERNEL[ky + halfSize][kx + halfSize];
                            gyb += color.blue * SOBEL_Y_KERNEL[ky + halfSize][kx + halfSize];
                        }
                    }
                }

                int g = (int) Math.sqrt(gxr * gxr + gxg * gxg + gxb * gxb + gyr * gyr + gyg * gyg + gyb * gyb) / 4;

                RGB edgeColor = new RGB(g, g, g);

                newImage.setRGB(x, y, edgeColor.toInt());
            }
        }

        return newImage;
    }

    static BufferedImage blur(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int halfSize = BLUR_KERNEL.length / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double totalWeight = 0;
                double r = 0;
                double g = 0;
                double b = 0;

                for (int ky = -halfSize; ky <= halfSize; ky++) {
                    for (int kx = -halfSize; kx <= halfSize; kx++) {
                        int sy = y + ky;
                        int sx = x + kx;
                        if ((sx >= 0) && (sx <= width - 1) && (sy >= 0) && (sy <= height - 1)) {
                            double weight = BLUR_KERNEL[ky + halfSize][kx + halfSize];

                            totalWeight += weight;

                            RGB sColor = RGB.fromInt(img.getRGB(sx, sy));

                            r += sColor.red * weight;
                            g += sColor.green * weight;
                            b += sColor.blue * weight;
                        }
                    }
                }

                RGB avgColor = new RGB((int) (r / totalWeight), (int) (g / totalWeight), (int) (b / totalWeight));

                newImage.setRGB(x, y, avgColor.toInt());
            }
        }

        return newImage;
    }

    static BufferedImage morph(BufferedImage img, boolean isDilation) {
        int width = img.getWidth();
        int height = img.getHeight();

        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int halfSize = MORPH_KERNEL.length / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                RGB maxColor = new RGB(0, 0, 0);
                int maxGrayScale = 0;

                RGB minColor = new RGB(255, 255, 255);
                int minGrayScale = 255;

                for (int ky = -halfSize; ky <= halfSize; ky++) {
                    for (int kx = -halfSize; kx <= halfSize; kx++) {
                        int sy = y + ky;
                        int sx = x + kx;

                        if ((sx >= 0) && (sx <= width - 1) && (sy >= 0) && (sy <= height - 1)) {
                            if (!MORPH_KERNEL[ky + halfSize][kx + halfSize]) {
                                continue;
                            }

                            RGB color = RGB.fromInt(img.getRGB(sx, sy));

                            int grayscale = color.toGrayScale();

                            if (grayscale > maxGrayScale) {
                                maxGrayScale = grayscale;
                                maxColor = color;
                            }

                            if (grayscale < minGrayScale) {
                                minGrayScale = grayscale;
                                minColor = color;
                            }
                        }
                    }
                }

                RGB newColor = isDilation ? maxColor : minColor;
                newImage.setRGB(x, y, newColor.toInt());
            }
        }

        return newImage;
    }

    static BufferedImage smartBlur(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        BufferedImage edgeImage = detectEdge(img);

        int halfSize = BLUR_KERNEL.length / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                RGB edgeColor = RGB.fromInt(edgeImage.getRGB(x, y));

                if (edgeColor.toGrayScale() > 40) {
                    newImage.setRGB(x, y, img.getRGB(x, y));
                    continue;
                }

                double totalWeight = 0;
                double r = 0;
                double g = 0;
                double b = 0;

                for (int ky = -halfSize; ky <= halfSize; ky++) {
                    for (int kx = -halfSize; kx <= halfSize; kx++) {
                        int sy = y + ky;
                        int sx = x + kx;
                        if ((sx >= 0) && (sx <= width - 1) && (sy >= 0) && (sy <= height - 1)) {
                            double weight = BLUR_KERNEL[ky + halfSize][kx + halfSize];

                            totalWeight += weight;

                            RGB sColor = RGB.fromInt(img.getRGB(sx, sy));

                            r += sColor.red * weight;
                            g += sColor.green * weight;
                            b += sColor.blue * weight;
                        }
                    }
                }

                RGB avgColor = new RGB((int) (r / totalWeight), (int) (g / totalWeight), (int) (b / totalWeight));

                newImage.setRGB(x, y, avgColor.toInt());
            }
        }

        return newImage;
    }
}

class ImageDisplay {
    private BufferedImage origImg;
    private BufferedImage img;
    private BufferedImage hueReplacementImg;

    private JLabel imageLabel;

    public ImageDisplay(JLabel label) {
        JFileChooser fc = new JFileChooser();

        this.imageLabel = label;
    }

    public boolean imageLoaded() {
        return (this.img != null);
    }

    public void loadImageFromFile(File f) throws IOException {
        Image image = ImageIO.read(f);

        this.img = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = this.img.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        this.origImg = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        g = this.origImg.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        this.imageLabel.setIcon(new ImageIcon(this.img));
    }

    public void showOriginal() {
        this.imageLabel.setIcon(new ImageIcon(this.origImg));
    }

    public void applyFilter(int filterType) {
        switch (filterType) {
            case Filter.BLUR:
                this.img = Filter.blur(this.origImg);
                break;
            case Filter.EDGE_DETECTION:
                this.img = Filter.detectEdge(this.origImg);
                break;
            case Filter.DILATION:
                this.img = Filter.morph(this.origImg, true);
                break;
            case Filter.EROSION:
                this.img = Filter.morph(this.origImg, false);
                break;
            case Filter.SMART_BLUR:
                this.img = Filter.smartBlur(this.origImg);
                break;
        }

        this.imageLabel.setIcon(new ImageIcon(this.img));
    }
}

public class Main {
    private static ImageDisplay imgDisplay;
    private static JLabel imageLabel;

    private static JFrame imgFrame;
    private static JFrame curveWindow;

    private static void showImageWindow() {
        imgFrame = new JFrame("PR01");
        imgFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        BoxLayout boxLayout = new BoxLayout(imgFrame.getContentPane(), BoxLayout.Y_AXIS);
        imgFrame.setLayout(boxLayout);

        JButton originalBtn = new JButton("Original");
        originalBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imgDisplay.showOriginal();
            }
        });
        imgFrame.add(originalBtn);

        JButton blurBtn = new JButton("Blur");
        blurBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imgDisplay.applyFilter(Filter.BLUR);
            }
        });
        imgFrame.add(blurBtn);

        JButton edgeDetectionBtn = new JButton("Detect Edge");
        edgeDetectionBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imgDisplay.applyFilter(Filter.EDGE_DETECTION);
            }
        });
        imgFrame.add(edgeDetectionBtn);

        JButton dilationBtn = new JButton("Dilate");
        dilationBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imgDisplay.applyFilter(Filter.DILATION);
            }
        });
        imgFrame.add(dilationBtn);

        JButton erosionBtn = new JButton("Erode");
        erosionBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imgDisplay.applyFilter(Filter.EROSION);
            }
        });
        imgFrame.add(erosionBtn);

        JButton smartBlurBtn = new JButton("Smart Blur");
        smartBlurBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imgDisplay.applyFilter(Filter.SMART_BLUR);
            }
        });
        imgFrame.add(smartBlurBtn);

        imageLabel = new JLabel();
        imgFrame.add(imageLabel);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");

        JMenuItem fileOpenItem = new JMenuItem(new AbstractAction("Open File") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();

                fc.showOpenDialog(imgFrame);

                File f = fc.getSelectedFile();

                try {
                    imgDisplay.loadImageFromFile(f);

                    imgFrame.pack();

                    if (curveWindow != null) {
                        curveWindow.setVisible(false);
                    }
                } catch (IOException ioExp) {
                    System.out.println("Invalid file");
                }
            }
        });

        menu.add(fileOpenItem);

        menuBar.add(menu);

        imgFrame.setJMenuBar(menuBar);

        imgFrame.setSize(800, 600);
        imgFrame.setVisible(true);
    }

    public static void main(String[] args) {
        int argc = args.length;

        showImageWindow();

        imgDisplay = new ImageDisplay(imageLabel);
    }
}
