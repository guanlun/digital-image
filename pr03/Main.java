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
 * java Main <SHAPE>
 */
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
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

class Curve {

}

class ImageDisplay {
    private BufferedImage img;
    private BufferedImage hueReplacementImg;

    public ImageDisplay() {
        JFileChooser fc = new JFileChooser();
    }

    public void loadImageFromFile(File f) throws IOException {

        Image image = ImageIO.read(f);

        this.img = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = this.img.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
    }

    public void loadHueReplacementImage(File f) throws IOException {
        Image image = ImageIO.read(f);

        int baseImageWidth = this.img.getWidth();
        int baseImageHeight = this.img.getHeight();

        Image hueReplacementImage = image.getScaledInstance(baseImageWidth, baseImageHeight, Image.SCALE_DEFAULT);

        this.hueReplacementImg = new BufferedImage(baseImageWidth, baseImageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = this.hueReplacementImg.createGraphics();
        g.drawImage(hueReplacementImage, 0, 0, null);
        g.dispose();

        for (int y = 0; y < this.img.getHeight(); y++) {
            for (int x = 0; x < this.img.getWidth(); x++) {
                int baseColor = this.img.getRGB(x, y);

                RGB baseRgbColor = RGB.fromInt(baseColor);
                HSV baseHsvColor = baseRgbColor.toHSV();

                int replacementColor = this.hueReplacementImg.getRGB(x, y);

                RGB replacementRgbColor = RGB.fromInt(replacementColor);
                HSV replacementHsvColor = replacementRgbColor.toHSV();

                baseHsvColor.hue = replacementHsvColor.hue;

                this.img.setRGB(x, y, baseHsvColor.toRGB().toInt());
            }
        }
    }

    public BufferedImage getImage() {
        return this.img;
    }
}

public class Main {
    private static ImageDisplay imgDisplay;

    private static void showWindow() {
        final JFrame frame = new JFrame("PR01");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        BoxLayout boxLayout = new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS);
        frame.setLayout(boxLayout);

        final JLabel label = new JLabel();
        frame.add(label);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");

        JMenuItem fileOpenItem = new JMenuItem(new AbstractAction("Open File") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();

                fc.showOpenDialog(frame);

                File f = fc.getSelectedFile();

                try {
                    imgDisplay.loadImageFromFile(f);

                    BufferedImage loadedImage = imgDisplay.getImage();

                    label.setIcon(new ImageIcon(loadedImage));

                    frame.pack();
                } catch (IOException ioExp) {
                    System.out.println("Invalid file");
                }
            }
        });

        menu.add(fileOpenItem);

        JMenuItem hueImageOpenItem = new JMenuItem(new AbstractAction("Open Hue Replacement Image") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();

                fc.showOpenDialog(frame);

                File f = fc.getSelectedFile();

                try {
                    imgDisplay.loadHueReplacementImage(f);

                    label.setIcon(new ImageIcon(imgDisplay.getImage()));
                } catch (IOException ioExp) {
                    System.out.println("Invalid file");
                }
            }
        });

        menu.add(hueImageOpenItem);

        menuBar.add(menu);

        frame.setJMenuBar(menuBar);

        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        int argc = args.length;

        imgDisplay = new ImageDisplay();

        showWindow();
    }
}
