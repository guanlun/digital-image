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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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

class Curve {
    static class Segment {
        Vec2 p0;
        Vec2 p1;
        Vec2 v0;
        Vec2 v1;

        ArrayList<Vec2> intermediatePoints;

        Segment(Vec2 p0, Vec2 p1, Vec2 v0, Vec2 v1) {
            this.p0 = p0;
            this.p1 = p1;
            this.v0 = v0;
            this.v1 = v1;
        }

        Vec2 getValueAt(double t) {
            double x =
                    (2 * Math.pow(t, 3) - 3 * t * t + 1) * p0.x
                    + (Math.pow(t, 3) - 2 * t * t + t) * v0.x
                    + (-2 * Math.pow(t, 3) + 3 * t * t) * p1.x
                    + (Math.pow(t, 3) - t * t) * v1.x;

            double y =
                    (2 * Math.pow(t, 3) - 3 * t * t + 1) * p0.y
                    + (Math.pow(t, 3) - 2 * t * t + t) * v0.y
                    + (-2 * Math.pow(t, 3) + 3 * t * t) * p1.y
                    + (Math.pow(t, 3) - t * t) * v1.y;

            return new Vec2(x, y);
        }
    }

    ArrayList<Segment> segments;

    ArrayList<Vec2> points;

    Curve() {
        points = new ArrayList<Vec2>();
        points.add(new Vec2(0, 0));
        points.add(new Vec2(1, 1));
    }
}

class CurvePanel extends JPanel implements MouseListener, MouseMotionListener {
    private Curve curve;

    private static int DIM = 500;
    private static int OFFSET = 20;

    private static final int SELECTION_P0 = 1;
    private static final int SELECTION_P1 = 2;
    private static final int SELECTION_V0 = 3;
    private static final int SELECTION_V1 = 4;


    private Vec2 selectedPoint;

    CurvePanel() {
        super();

        this.curve = new Curve();

        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        this.selectedPoint = null;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.clearRect(0, 0, 600, 600);

        g2.setColor(Color.BLACK);
        g2.drawRect(OFFSET, OFFSET, DIM, DIM);

        g2.setColor(new Color(192, 192, 192));
        for (int i = 1; i < 4; i++) {
            g2.drawLine(OFFSET, OFFSET + 125 * i, OFFSET + DIM, OFFSET + 125 * i);
        }

        for (int i = 1; i < 4; i++) {
            g2.drawLine(OFFSET + 125 * i, OFFSET, OFFSET + 125 * i, OFFSET + DIM);
        }

        g2.setColor(Color.BLACK);

        int pointCount = this.curve.points.size();

        ArrayList<Vec2> tangentVecs = new ArrayList<Vec2>();

        for (int i = 0; i < pointCount; i++) {
            Vec2 currPt = this.curve.points.get(i);

            if (i == 0) {
                Vec2 nextPt = this.curve.points.get(i + 1);

                tangentVecs.add(nextPt.sub(currPt));

            } else if (i == pointCount - 1){
                Vec2 prevPt = this.curve.points.get(i - 1);

                tangentVecs.add(currPt.sub(prevPt));

            } else {
                Vec2 nextPt = this.curve.points.get(i + 1);
                Vec2 prevPt = this.curve.points.get(i - 1);

                Vec2 diff = nextPt.sub(prevPt);

                tangentVecs.add(new Vec2(diff.x / 2.0, diff.y / 2.0));
            }
        }

        for (int i = 0; i < pointCount - 1; i++) {
            Vec2 p0 = this.curve.points.get(i);
            Vec2 p1 = this.curve.points.get(i + 1);

            Vec2 v0 = tangentVecs.get(i);
            Vec2 v1 = tangentVecs.get(i + 1);

            double lastX = p0.x;
            double lastY = p0.y;

            for (double t = 0.01; t <= 1.01; t += 0.01) {
                double x =
                        (2 * Math.pow(t, 3) - 3 * t * t + 1) * p0.x
                                + (Math.pow(t, 3) - 2 * t * t + t) * v0.x
                                + (-2 * Math.pow(t, 3) + 3 * t * t) * p1.x
                                + (Math.pow(t, 3) - t * t) * v1.x;

                double y =
                        (2 * Math.pow(t, 3) - 3 * t * t + 1) * p0.y
                                + (Math.pow(t, 3) - 2 * t * t + t) * v0.y
                                + (-2 * Math.pow(t, 3) + 3 * t * t) * p1.y
                                + (Math.pow(t, 3) - t * t) * v1.y;

                g2.drawLine(
                        (int) Math.round(OFFSET + lastX * DIM),
                        (int) Math.round(OFFSET + (1 - lastY) * DIM),
                        (int) Math.round(OFFSET + x * DIM),
                        (int) Math.round(OFFSET + (1 - y) * DIM)
                );

                lastX = x;
                lastY = y;
            }
        }

        for (Vec2 pt : this.curve.points) {
            g2.drawRect((int) Math.round(pt.x * DIM + OFFSET - 5), (int) Math.round((1 - pt.y) * DIM + OFFSET - 5), 10, 10);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX() - OFFSET;
        int y = e.getY() - OFFSET;

        int insertIndex = -1;

        for (int i = 0; i < this.curve.points.size(); i++) {
            Vec2 point = this.curve.points.get(i);

            int dx = (int)(point.x * DIM - x);
            int dy = (int)((1 - point.y) * DIM - y);

            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist < 10) {
                this.selectedPoint = point;
                return;
            }

            if ((point.x * DIM > x) && (insertIndex == -1)) {
                insertIndex = i;
            }
        }

        // No near points found, add a new point
        Vec2 point = new Vec2((double)(x) / DIM, 1 - (double)(y) / DIM);

        this.curve.points.add(insertIndex, point);

        this.selectedPoint = point;

        this.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.selectedPoint = null;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (this.selectedPoint == null) {
            return;
        }

        double x = Math.min(1, Math.max(0, ((double)(e.getX()) - OFFSET) / DIM));
        double y = Math.min(1, Math.max(0, 1 - ((double)(e.getY()) - OFFSET) / DIM));

        this.selectedPoint.x = x;
        this.selectedPoint.y = y;

        this.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }
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

    private static void showImageWindow() {
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

    private static void showCurveWindow() {
        JFrame curveWindow = new JFrame("Curve");
        curveWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        curveWindow.setSize(600, 600);

        CurvePanel curvePanel = new CurvePanel();

        curveWindow.add(curvePanel);
        curveWindow.setVisible(true);
    }

    public static void main(String[] args) {
        int argc = args.length;

        imgDisplay = new ImageDisplay();

//        showImageWindow();
        showCurveWindow();
    }
}
