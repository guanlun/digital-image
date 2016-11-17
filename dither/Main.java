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
 */
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

class ImageDisplay {
    public static final int THRESHOLD = 0;
    public static final int FLOYD_STEINBERG = 1;
    public static final int ORDERED = 2;

    private BufferedImage origImg;
    private BufferedImage img;

    private JLabel imageLabel;

    private int[][] orderedKernel = {
            {1, 9, 3, 11},
            {13, 5, 15, 7},
            {4, 12, 2, 10},
            {16, 8, 14, 6},
    };

    public ImageDisplay(JLabel label) {
        this.imageLabel = label;
    }

    public boolean imageLoaded() {
        return (this.img != null);
    }

    public void showOriginal() {
        this.imageLabel.setIcon(new ImageIcon(this.origImg));
    }

    private double findClosestColor(double val) {
        int interval = 256 / 3;

        return Math.min(255, Math.round(val / interval) * interval);
    }

    public void modify(int mode) {
        int width = this.origImg.getWidth();
        int height = this.origImg.getHeight();

        double[][][] result = new double[height][width][3];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                RGB color = RGB.fromInt(this.origImg.getRGB(x, y));

                result[y][x][0] = color.red;
                result[y][x][1] = color.green;
                result[y][x][2] = color.blue;
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int colorIdx = 0; colorIdx < 3; colorIdx++) {
                    double oldVal, newVal;

                    switch (mode) {
                        case THRESHOLD:
                            result[y][x][colorIdx] = findClosestColor(result[y][x][colorIdx]);
                            break;

                        case FLOYD_STEINBERG:
                            oldVal = result[y][x][colorIdx];

                            newVal = findClosestColor(oldVal);

                            result[y][x][colorIdx] = newVal;

                            double error = oldVal - newVal;

                            if (x < width - 1) {
                                result[y][x + 1][colorIdx] += error * 7 / 16;
                            }

                            if (x > 0 && y < height - 1) {
                                result[y + 1][x - 1][colorIdx] += error * 3 / 16;
                            }

                            if (y < height - 1) {
                                result[y + 1][x][colorIdx] += error * 5 / 16;
                            }

                            if (x < width - 1 && y < height - 1) {
                                result[y + 1][x + 1][colorIdx] += error * 1 / 16;
                            }
                            break;

                        case ORDERED:
                            oldVal = result[y][x][colorIdx] + (result[y][x][colorIdx] * (orderedKernel[y % 4][x % 4] - 8) / 17);
                            newVal = findClosestColor(oldVal);

                            result[y][x][colorIdx] = newVal;
                            break;
                    }
                }
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double[] val = result[y][x];

                RGB resultColor = new RGB((int)(val[0]), (int)(val[1]), (int)(val[2]));
                this.img.setRGB(x, y, resultColor.toInt());
            }
        }

        this.imageLabel.setIcon(new ImageIcon(this.img));
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
}

public class Main {
    private static ImageDisplay imgDisplay;
    private static JLabel imageLabel;

    private static JFrame imgFrame;

    private static void showImageWindow() {
        imgFrame = new JFrame("PR01");
        imgFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        BoxLayout boxLayout = new BoxLayout(imgFrame.getContentPane(), BoxLayout.Y_AXIS);
        imgFrame.setLayout(boxLayout);

        JButton originalBtn = new JButton("Original");
        originalBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!imgDisplay.imageLoaded()) {
                    return;
                }
                imgDisplay.showOriginal();
            }
        });
        imgFrame.add(originalBtn);

        JButton thresholdBtn = new JButton("Threshold");
        thresholdBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!imgDisplay.imageLoaded()) {
                    return;
                }
                imgDisplay.modify(ImageDisplay.THRESHOLD);
            }
        });
        imgFrame.add(thresholdBtn);

        JButton fsBtn = new JButton("Floyd-Steinberg Dithering");
        fsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!imgDisplay.imageLoaded()) {
                    return;
                }
                imgDisplay.modify(ImageDisplay.FLOYD_STEINBERG);
            }
        });
        imgFrame.add(fsBtn);

        JButton orderedBtn = new JButton("Ordered Dithering");
        orderedBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!imgDisplay.imageLoaded()) {
                    return;
                }
                imgDisplay.modify(ImageDisplay.ORDERED);
            }
        });
        imgFrame.add(orderedBtn);

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
        showImageWindow();

        imgDisplay = new ImageDisplay(imageLabel);
    }
}