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

class PixelData implements Comparable<PixelData> {
    int x;
    int y;
    RGB color;
    double energy;
    double aggrEnergy;
    PixelData lastRowPixel;
    boolean removed;

    @Override
    public int compareTo(PixelData other) {
        if (this.aggrEnergy > other.aggrEnergy) {
            return 1;
        } else if (this.aggrEnergy < other.aggrEnergy) {
            return -1;
        } else {
            return 0;
        }
    }
}

class ImageDisplay {
    private BufferedImage origImg;
    private BufferedImage img;
    private BufferedImage secondImg;

    private JLabel imageLabel;

    public ImageDisplay(JLabel label) {
        JFileChooser fc = new JFileChooser();

        this.imageLabel = label;
    }

    public boolean imageLoaded() {
        return (this.img != null);
    }

    public void showOriginal() {
        this.imageLabel.setIcon(new ImageIcon(this.origImg));
    }

    public void showSecond() {
        this.imageLabel.setIcon(new ImageIcon(this.secondImg));
    }

    private int diff(RGB c1, RGB c2) {
        return Math.abs(c1.red - c2.red + c1.green - c2.green + c1.blue - c2.blue);
    }

    public void stitch() {
        int width = this.img.getWidth();
        int height = this.img.getHeight();

        PixelData[][] pixels = new PixelData[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y][x] = new PixelData();
                pixels[y][x].y = y;
                pixels[y][x].x = x;
                double e = 0;

                if (x < width - 1) {
                    RGB c1 = RGB.fromInt(this.origImg.getRGB(x, y));
                    RGB c2 = RGB.fromInt(this.secondImg.getRGB(x + 1, y));

                    int rDiff = c2.red - c1.red;
                    int gDiff = c2.green - c1.green;
                    int bDiff = c2.blue - c1.blue;

                    e = Math.sqrt(rDiff * rDiff + gDiff * gDiff + bDiff * bDiff);
                } else {
                    e = Double.MAX_VALUE;
                }

                pixels[y][x].energy = e;
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                PixelData pd = pixels[y][x];

                if (y == 0) {
                    pd.aggrEnergy = pd.energy;
                } else {
                    PixelData top = pixels[y - 1][x];

                    if (x == 0) {
                        PixelData topRight = pixels[y - 1][x + 1];

                        if (top.aggrEnergy > topRight.aggrEnergy) {
                            pd.aggrEnergy = topRight.aggrEnergy + pd.energy;
                            pd.lastRowPixel = topRight;
                        } else {
                            pd.aggrEnergy = top.aggrEnergy + pd.energy;
                            pd.lastRowPixel = top;
                        }

                    } else if (x == width - 1) {
                        PixelData topLeft = pixels[y - 1][x - 1];

                        if (top.aggrEnergy > topLeft.aggrEnergy) {
                            pd.aggrEnergy = topLeft.aggrEnergy + pd.energy;
                            pd.lastRowPixel = topLeft;
                        } else {
                            pd.aggrEnergy = top.aggrEnergy + pd.energy;
                            pd.lastRowPixel = top;
                        }

                    } else {
                        PixelData topLeft = pixels[y - 1][x - 1];
                        PixelData topRight = pixels[y - 1][x + 1];

                        if (top.aggrEnergy < topLeft.aggrEnergy) {
                            if (top.aggrEnergy < topRight.aggrEnergy) {
                                // TOP is min
                                pd.aggrEnergy = top.aggrEnergy + pd.energy;
                                pd.lastRowPixel = top;
                            } else {
                                // TOPRIGHT is min
                                pd.aggrEnergy = topRight.aggrEnergy + pd.energy;
                                pd.lastRowPixel = topRight;
                            }
                        } else {
                            if (topLeft.aggrEnergy < topRight.aggrEnergy) {
                                // TOPLEFT is min
                                pd.aggrEnergy = topLeft.aggrEnergy + pd.energy;
                                pd.lastRowPixel = topLeft;
                            } else {
                                // TOPRIGHT is min
                                pd.aggrEnergy = topRight.aggrEnergy + pd.energy;
                                pd.lastRowPixel = topRight;
                            }
                        }
                    }
                }
            }
        }

        double minAggrEnergy = Double.MAX_VALUE;
        PixelData argMin = null;

        PixelData[] lastRow = pixels[height - 1];

        for (int x = 0; x < width; x++) {
            PixelData pd = lastRow[x];

            if (pd.aggrEnergy < minAggrEnergy) {
                minAggrEnergy = pd.aggrEnergy;
                argMin = pd;
            }
        }

        PixelData currMin = argMin;
        for (int y = height - 1; y >= 0; y--) {
            System.out.println(currMin.x);
            for (int x = 0; x < width; x++) {
                if (x <= currMin.x) {
                    this.img.setRGB(x, y, this.origImg.getRGB(x, y));
                } else {
                    this.img.setRGB(x, y, this.secondImg.getRGB(x, y));
                }
            }
            currMin = currMin.lastRowPixel;
        }

        this.imageLabel.setIcon(new ImageIcon(this.img));
    }

    public void resize() {
        int width = this.img.getWidth();
        int height = this.img.getHeight();

        PixelData[][] pixels = new PixelData[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                RGB color = RGB.fromInt(this.origImg.getRGB(x, y));

                pixels[y][x] = new PixelData();
                pixels[y][x].y = y;
                pixels[y][x].x = x;
                pixels[y][x].color = color;
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double energy;
                PixelData pd = pixels[y][x];

                if (x == 0) {
                    energy = diff(pixels[y][x + 1].color, pd.color);
                } else if (x == width - 1) {
                    energy = diff(pd.color, pixels[y][x - 1].color);
                } else {
                    energy = (diff(pixels[y][x + 1].color, pd.color) + diff(pd.color, pixels[y][x - 1].color)) / 2;
                }

                pd.energy = energy;
            }
        }

        int currWidth = width;

        for (int i = 0; i < 50; i++) {
            currWidth--;

            PixelData[][] newPixels = new PixelData[height][currWidth - 1];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < currWidth; x++) {
                    PixelData pd = pixels[y][x];

                    if (y == 0) {
                        pd.aggrEnergy = pd.energy;
                    } else {
                        PixelData top = pixels[y - 1][x];

                        if (x == 0) {
                            PixelData topRight = pixels[y - 1][x + 1];

                            if (top.aggrEnergy > topRight.aggrEnergy) {
                                pd.aggrEnergy = topRight.aggrEnergy + pd.energy;
                                pd.lastRowPixel = topRight;
                            } else {
                                pd.aggrEnergy = top.aggrEnergy + pd.energy;
                                pd.lastRowPixel = top;
                            }

                        } else if (x == currWidth - 1) {
                            PixelData topLeft = pixels[y - 1][x - 1];

                            if (top.aggrEnergy > topLeft.aggrEnergy) {
                                pd.aggrEnergy = topLeft.aggrEnergy + pd.energy;
                                pd.lastRowPixel = topLeft;
                            } else {
                                pd.aggrEnergy = top.aggrEnergy + pd.energy;
                                pd.lastRowPixel = top;
                            }

                        } else {
                            PixelData topLeft = pixels[y - 1][x - 1];
                            PixelData topRight = pixels[y - 1][x + 1];

                            if (top.aggrEnergy < topLeft.aggrEnergy) {
                                if (top.aggrEnergy < topRight.aggrEnergy) {
                                    // TOP is min
                                    pd.aggrEnergy = top.aggrEnergy + pd.energy;
                                    pd.lastRowPixel = top;
                                } else {
                                    // TOPRIGHT is min
                                    pd.aggrEnergy = topRight.aggrEnergy + pd.energy;
                                    pd.lastRowPixel = topRight;
                                }
                            } else {
                                if (topLeft.aggrEnergy < topRight.aggrEnergy) {
                                    // TOPLEFT is min
                                    pd.aggrEnergy = topLeft.aggrEnergy + pd.energy;
                                    pd.lastRowPixel = topLeft;
                                } else {
                                    // TOPRIGHT is min
                                    pd.aggrEnergy = topRight.aggrEnergy + pd.energy;
                                    pd.lastRowPixel = topRight;
                                }
                            }
                        }
                    }
                }
            }

            double minAggrEnergy = Double.MAX_VALUE;
            PixelData argMin = null;
            int argMinX = 0;

            PixelData[] lastRow = pixels[height - 1];

            for (int x = 0; x < currWidth; x++) {
                PixelData pd = lastRow[x];

                if (pd.aggrEnergy < minAggrEnergy) {
                    minAggrEnergy = pd.aggrEnergy;
                    argMin = pd;
                    argMinX = x;
                }
            }

            PixelData currMin = argMin;

            for (int y = height - 1; y >= 0; y--) {
                for (int x = 0; x < currWidth - 1; x++) {
                    if (x < currMin.x) {
                        newPixels[y][x] = pixels[y][x];
                    } else {
                        newPixels[y][x] = pixels[y][x + 1];
                    }

                    newPixels[y][x].x = x;
                    newPixels[y][x].y = y;
                }

                currMin = currMin.lastRowPixel;
            }

            pixels = newPixels;
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x < currWidth - 1) {
                    this.img.setRGB(x, y, pixels[y][x].color.toInt());
                } else {
                    this.img.setRGB(x, y, 0);
                }
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

    public void loadSecondImage(File f) throws IOException {
        Image image = ImageIO.read(f);

        int baseImageWidth = this.img.getWidth();
        int baseImageHeight = this.img.getHeight();

        Image foregroundImage = image.getScaledInstance(baseImageWidth, baseImageHeight, Image.SCALE_DEFAULT);

        this.secondImg = new BufferedImage(baseImageWidth, baseImageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = this.secondImg.createGraphics();
        g.drawImage(foregroundImage, 0, 0, null);
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

        JButton secondBtn = new JButton("Second");
        secondBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!imgDisplay.imageLoaded()) {
                    return;
                }
                imgDisplay.showSecond();
            }
        });
        imgFrame.add(secondBtn);

        JButton stitchBtn = new JButton("Stitch");
        stitchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!imgDisplay.imageLoaded()) {
                    return;
                }
                imgDisplay.stitch();
            }
        });
        imgFrame.add(stitchBtn);

        JButton resizeBtn = new JButton("Resize");
        resizeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!imgDisplay.imageLoaded()) {
                    return;
                }
                imgDisplay.resize();
            }
        });
        imgFrame.add(resizeBtn);

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

        JMenuItem foregroundImageOpenItem = new JMenuItem(new AbstractAction("Open Second Image") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (imgDisplay.imageLoaded()) {
                    JFileChooser fc = new JFileChooser();

                    fc.showOpenDialog(imgFrame);

                    File f = fc.getSelectedFile();

                    try {
                        imgDisplay.loadSecondImage(f);
                    } catch (IOException ioExp) {
                        System.out.println("Invalid file");
                    }
                }
            }
        });

        menu.add(foregroundImageOpenItem);

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