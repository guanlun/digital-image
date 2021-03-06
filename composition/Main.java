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

    HSV toHSV(){
        double epsilon = 0.0001;

        double r = this.red / 255.0;
        double g = this.green / 255.0;
        double b = this.blue / 255.0;

        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));

        double value = max;
        double saturation = 0;
        double hue = 0;

        if (max > epsilon) {
            double delta = max - min;

            saturation = delta / max;

            if (delta < epsilon) {
                hue = 0;
            } else {
                if (Math.abs(r - max) < epsilon) {
                    hue = (g - b) / delta;
                } else if (Math.abs(g - max) < epsilon) {
                    hue = 2.0 + (b - r) / delta;
                } else {
                    hue = 4.0 + (r - g) / delta;
                }

                hue *= 60;

                if (hue < 0) {
                    hue += 360;
                }
            }
        }

        return new HSV(hue, saturation, value);
    }
}

class HSV {
    double hue;
    double saturation;
    double value;

    HSV(double h, double s, double v) {
        this.hue = h;
        this.saturation = s;
        this.value = v;
    }

    RGB toRGB() {
        double hh, p, q, t, ff;
        long i;
        double r = 0, g = 0, b = 0;

        if (this.saturation <= 0) {
            r = this.value;
            g = this.value;
            b = this.value;
        } else {
            hh = this.hue;

            if (hh >= 360) {
                hh = 0;
            }

            hh /= 60.0;

            i = (long)hh;
            ff = hh - i;
            p = this.value * (1.0 - this.saturation);
            q = this.value * (1.0 - (this.saturation * ff));
            t = this.value * (1.0 - (this.saturation * (1.0 - ff)));

            if (i == 0) {
                r = this.value;
                g = t;
                b = p;
            } else if (i == 1) {
                r = q;
                g = this.value;
                b = p;
            } else if (i == 2) {
                r = p;
                g = this.value;
                b = t;
            } else if (i == 3) {
                r = p;
                g = q;
                b = this.value;
            } else if (i == 4) {
                r = t;
                g = p;
                b = this.value;
            } else {
                r = this.value;
                g = p;
                b = q;
            }
        }

        return new RGB((int)(r * 255), (int)(g * 255), (int)(b * 255));
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

class ImageDisplay {
    private BufferedImage origImg;
    private BufferedImage img;
    private BufferedImage foregroundImg;

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

    public void over() {
        double alpha = 0.5;

        for (int y = 0; y < this.img.getHeight(); y++) {
            for (int x = 0; x < this.img.getWidth(); x++) {
                RGB bgColor = RGB.fromInt(this.origImg.getRGB(x, y));
                RGB fgColor = RGB.fromInt(this.foregroundImg.getRGB(x, y));

                int r = (int)(bgColor.red * (1 - alpha) + fgColor.red * alpha);
                int g = (int)(bgColor.green * (1 - alpha) + fgColor.green * alpha);
                int b = (int)(bgColor.blue * (1 - alpha) + fgColor.blue * alpha);

                RGB compositeColor = new RGB(r, g, b);

                this.img.setRGB(x, y, compositeColor.toInt());
            }
        }

        this.imageLabel.setIcon(new ImageIcon(this.img));
    }

    public void multiplication() {
        for (int y = 0; y < this.img.getHeight(); y++) {
            for (int x = 0; x < this.img.getWidth(); x++) {
                RGB bgColor = RGB.fromInt(this.origImg.getRGB(x, y));
                RGB fgColor = RGB.fromInt(this.foregroundImg.getRGB(x, y));

                int r = (int)((bgColor.red / 255.0) * (fgColor.red / 255.0) * 255.0);
                int g = (int)((bgColor.green / 255.0) * (fgColor.green / 255.0) * 255.0);
                int b = (int)((bgColor.blue / 255.0) * (fgColor.blue / 255.0) * 255.0);

                RGB compositeColor = new RGB(r, g, b);

                this.img.setRGB(x, y, compositeColor.toInt());
            }
        }

        this.imageLabel.setIcon(new ImageIcon(this.img));
    }

    public void subtraction() {
        for (int y = 0; y < this.img.getHeight(); y++) {
            for (int x = 0; x < this.img.getWidth(); x++) {
                RGB bgColor = RGB.fromInt(this.origImg.getRGB(x, y));
                RGB fgColor = RGB.fromInt(this.foregroundImg.getRGB(x, y));

                int r = Math.max(0, bgColor.red - fgColor.red);
                int g = Math.max(0, bgColor.green - fgColor.green);
                int b = Math.max(0, bgColor.blue - fgColor.blue);

                RGB compositeColor = new RGB(r, g, b);

                this.img.setRGB(x, y, compositeColor.toInt());
            }
        }

        this.imageLabel.setIcon(new ImageIcon(this.img));
    }

    public void max() {
        for (int y = 0; y < this.img.getHeight(); y++) {
            for (int x = 0; x < this.img.getWidth(); x++) {
                RGB bgColor = RGB.fromInt(this.origImg.getRGB(x, y));
                RGB fgColor = RGB.fromInt(this.foregroundImg.getRGB(x, y));

                int r = Math.max(bgColor.red, fgColor.red);
                int g = Math.max(bgColor.green, fgColor.green);
                int b = Math.max(bgColor.blue, fgColor.blue);

                RGB compositeColor = new RGB(r, g, b);

                this.img.setRGB(x, y, compositeColor.toInt());
            }
        }

        this.imageLabel.setIcon(new ImageIcon(this.img));
    }

    public void min() {
        for (int y = 0; y < this.img.getHeight(); y++) {
            for (int x = 0; x < this.img.getWidth(); x++) {
                RGB bgColor = RGB.fromInt(this.origImg.getRGB(x, y));
                RGB fgColor = RGB.fromInt(this.foregroundImg.getRGB(x, y));

                int r = Math.min(bgColor.red, fgColor.red);
                int g = Math.min(bgColor.green, fgColor.green);
                int b = Math.min(bgColor.blue, fgColor.blue);

                RGB compositeColor = new RGB(r, g, b);

                this.img.setRGB(x, y, compositeColor.toInt());
            }
        }

        this.imageLabel.setIcon(new ImageIcon(this.img));
    }

    public void greenScreen() {
        for (int y = 0; y < this.img.getHeight(); y++) {
            for (int x = 0; x < this.img.getWidth(); x++) {
                RGB bgColor = RGB.fromInt(this.origImg.getRGB(x, y));
                RGB fgColor = RGB.fromInt(this.foregroundImg.getRGB(x, y));

                HSV fgHSV = fgColor.toHSV();

                if (fgHSV.hue > 100 && fgHSV.hue < 140 && fgHSV.saturation > 0.5) {
                    this.img.setRGB(x, y, bgColor.toInt());

                } else if (fgHSV.hue > 60 && fgHSV.hue < 180 && fgHSV.saturation > 0.3) {
                    double alpha = Math.pow(Math.abs(fgHSV.hue - 120) / 60.0, 2);

                    int r = (int)(fgColor.red * alpha + bgColor.red * (1 - alpha));
                    int g = (int)(fgColor.green * alpha + bgColor.green * (1 - alpha));
                    int b = (int)(fgColor.blue * alpha + bgColor.blue * (1 - alpha));

                    this.img.setRGB(x, y, new RGB(r, g, b).toInt());

                } else {
                    this.img.setRGB(x, y, fgColor.toInt());
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

    public void loadForegroundImage(File f) throws IOException {
        Image image = ImageIO.read(f);

        int baseImageWidth = this.img.getWidth();
        int baseImageHeight = this.img.getHeight();

        Image foregroundImage = image.getScaledInstance(baseImageWidth, baseImageHeight, Image.SCALE_DEFAULT);

        this.foregroundImg = new BufferedImage(baseImageWidth, baseImageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = this.foregroundImg.createGraphics();
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

        JButton overBtn = new JButton("Over");
        overBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!imgDisplay.imageLoaded()) {
                    return;
                }
                imgDisplay.over();
            }
        });
        imgFrame.add(overBtn);

        JButton multiplicationBtn = new JButton("Multiplication");
        multiplicationBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!imgDisplay.imageLoaded()) {
                    return;
                }
                imgDisplay.multiplication();
            }
        });
        imgFrame.add(multiplicationBtn);

        JButton subtractionBtn = new JButton("Subtraction");
        subtractionBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!imgDisplay.imageLoaded()) {
                    return;
                }
                imgDisplay.subtraction();
            }
        });
        imgFrame.add(subtractionBtn);

        JButton maxBtn = new JButton("Max");
        maxBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!imgDisplay.imageLoaded()) {
                    return;
                }
                imgDisplay.max();
            }
        });
        imgFrame.add(maxBtn);

        JButton minBtn = new JButton("Min");
        minBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!imgDisplay.imageLoaded()) {
                    return;
                }
                imgDisplay.min();
            }
        });
        imgFrame.add(minBtn);

        JButton greenScreenBtn = new JButton("Green screen");
        greenScreenBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!imgDisplay.imageLoaded()) {
                    return;
                }
                imgDisplay.greenScreen();
            }
        });
        imgFrame.add(greenScreenBtn);

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

        JMenuItem foregroundImageOpenItem = new JMenuItem(new AbstractAction("Open Foreground Image") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (imgDisplay.imageLoaded()) {
                    JFileChooser fc = new JFileChooser();

                    fc.showOpenDialog(imgFrame);

                    File f = fc.getSelectedFile();

                    try {
                        imgDisplay.loadForegroundImage(f);
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