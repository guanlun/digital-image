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
 * - Rotation
 * - Scaling
 * - Shearing
 * - Mirroring
 * - Translation
 * - Perspective projection
 *
 * Use "File -> Open" to load a base image
 *
 * Use the buttons to select transformation.
 */
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class Vec3 {
    double x;
    double y;
    double z;

    Vec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    Vec3 multiplyMatrix(Mat3 mat) {
        return new Vec3(
                this.x * mat.get(0, 0) + this.y * mat.get(0, 1) + this.z * mat.get(0, 2),
                this.x * mat.get(1, 0) + this.y * mat.get(1, 1) + this.z * mat.get(1, 2),
                this.x * mat.get(2, 0) + this.y * mat.get(2, 1) + this.z * mat.get(2, 2)
        );
    }
}

class Mat3 {
    double[][] data;

    Mat3(double[][] data) {
        this.data = data;
    }

    double get(int y, int x) {
        return this.data[y][x];
    }

    void print() {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                System.out.print(this.get(x, y) + "   ");
            }

            System.out.println();
        }
    }

    Mat3 multiplyMatrix(Mat3 mat) {
        double[][] newData = new double[3][3];

        newData[0][0] = this.get(0, 0) * mat.get(0, 0) + this.get(0, 1) * mat.get(1, 0) + this.get(0, 2) * mat.get(2, 0);
        newData[0][1] = this.get(0, 0) * mat.get(0, 1) + this.get(0, 1) * mat.get(1, 1) + this.get(0, 2) * mat.get(2, 1);
        newData[0][2] = this.get(0, 0) * mat.get(0, 2) + this.get(0, 1) * mat.get(1, 2) + this.get(0, 2) * mat.get(2, 2);

        newData[1][0] = this.get(1, 0) * mat.get(0, 0) + this.get(1, 1) * mat.get(1, 0) + this.get(1, 2) * mat.get(2, 0);
        newData[1][1] = this.get(1, 0) * mat.get(0, 1) + this.get(1, 1) * mat.get(1, 1) + this.get(1, 2) * mat.get(2, 1);
        newData[1][2] = this.get(1, 0) * mat.get(0, 2) + this.get(1, 1) * mat.get(1, 2) + this.get(1, 2) * mat.get(2, 2);

        newData[2][0] = this.get(2, 0) * mat.get(0, 0) + this.get(2, 1) * mat.get(1, 0) + this.get(2, 2) * mat.get(2, 0);
        newData[2][1] = this.get(2, 0) * mat.get(0, 1) + this.get(2, 1) * mat.get(1, 1) + this.get(2, 2) * mat.get(2, 1);
        newData[2][2] = this.get(2, 0) * mat.get(0, 2) + this.get(2, 1) * mat.get(1, 2) + this.get(2, 2) * mat.get(2, 2);

        return new Mat3(newData);
    }
}

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

class ImageDisplay {
    private BufferedImage origImg;
    private BufferedImage img;

    private JLabel imageLabel;

    public ImageDisplay(JLabel label) {
        JFileChooser fc = new JFileChooser();

        this.imageLabel = label;
    }

    public boolean imageLoaded() {
        return (this.img != null);
    }

    public int getImageWidth() {
        return img.getWidth();
    }

    public int getImageHeight() {
        return img.getHeight();
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

    public void translate() {
        int width = this.img.getWidth();
        int height = this.img.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                RGB color;

                int sampleX = x - 100;
                int sampleY = y - 100;

                if (sampleX < 0 || sampleX >= width || sampleY < 0 || sampleY >= height) {
                    color = new RGB(0, 0, 0);
                } else {
                    color = RGB.fromInt(this.origImg.getRGB(sampleX, sampleY));
                }

                this.img.setRGB(x, y, color.toInt());
            }
        }

        this.imageLabel.setIcon(new ImageIcon(this.img));
    }

    public void transform(Mat3 matrix) {
        int width = this.img.getWidth();
        int height = this.img.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double red = 0, green = 0, blue = 0;

                double xBase = Math.random() * 0.25;
                double yBase = Math.random() * 0.25;

                for (double ys = yBase; ys < 1; ys += 0.25) {
                    for (double xs = xBase; xs < 1; xs += 0.25) {
                        double xPos = x + xs;
                        double yPos = y + ys;

                        Vec3 posVec = new Vec3(xPos, yPos, 1);

                        Vec3 origVec = posVec.multiplyMatrix(matrix);

                        int sampleX = (int) (origVec.x / origVec.z);
                        int sampleY = (int) (origVec.y / origVec.z);

                        if (sampleX < 0 || sampleX >= width || sampleY < 0 || sampleY >= height) {
                            continue;
                        } else {
                            RGB color = RGB.fromInt(this.origImg.getRGB(sampleX, sampleY));

                            red += color.red / 16.0;
                            green += color.green / 16.0;
                            blue += color.blue / 16.0;
                        }
                    }
                }

                this.img.setRGB(x, y, new RGB((int) red, (int) green, (int) blue).toInt());
            }
        }

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

        JButton rotateBtn = new JButton("Rotate");
        rotateBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!imgDisplay.imageLoaded()) {
                    return;
                }
                double[][] matrix = {
                        {Math.cos(0.2), -Math.sin(0.2), 0},
                        {Math.sin(0.2), Math.cos(0.2), 0},
                        {0, 0, 1}
                };
                imgDisplay.transform(new Mat3(matrix));
            }
        });
        imgFrame.add(rotateBtn);

        JButton scaleBtn = new JButton("Scale");
        scaleBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!imgDisplay.imageLoaded()) {
                    return;
                }
                double[][] matrix = {
                        {1.5, 0, 0},
                        {0, 1.5, 0},
                        {0, 0, 1}
                };
                imgDisplay.transform(new Mat3(matrix));
            }
        });
        imgFrame.add(scaleBtn);

        JButton shearBtn = new JButton("Shear");
        shearBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!imgDisplay.imageLoaded()) {
                    return;
                }
                double[][] matrix = {
                        {1, 0.2, 0},
                        {0, 1, 0},
                        {0, 0, 1}
                };
                imgDisplay.transform(new Mat3(matrix));
            }
        });
        imgFrame.add(shearBtn);

        JButton mirrorBtn = new JButton("Mirror");
        mirrorBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!imgDisplay.imageLoaded()) {
                    return;
                }
                double[][] matrix = {
                        {-1, 0, 0},
                        {0, 1, 0},
                        {0, 0, 1}
                };
                Mat3 mirrorMtx = new Mat3(matrix);

                double [][] translateBackMatrix = {
                        {1, 0, -imgDisplay.getImageWidth()},
                        {0, 1, 0},
                        {0, 0, 1}
                };
                Mat3 translateBackMtx = new Mat3(translateBackMatrix);

                Mat3 combinedMtx = mirrorMtx.multiplyMatrix(translateBackMtx);

                imgDisplay.transform(combinedMtx);
            }
        });
        imgFrame.add(mirrorBtn);

        JButton translateBtn = new JButton("Translate");
        translateBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!imgDisplay.imageLoaded()) {
                    return;
                }
                double[][] matrix = {
                        {1, 0, 100},
                        {0, 1, 200},
                        {0, 0, 1}
                };
                imgDisplay.transform(new Mat3(matrix));
            }
        });
        imgFrame.add(translateBtn);

        JButton projectionBtn = new JButton("Perspective Projection");
        projectionBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!imgDisplay.imageLoaded()) {
                    return;
                }
                double[][] matrix = {
                        {1, 0, 0},
                        {0, 1, 0},
                        {-0.0006, -0.001, 1}
                };
                imgDisplay.transform(new Mat3(matrix));
            }
        });
        imgFrame.add(projectionBtn);

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
        int argc = args.length;

        showImageWindow();

        imgDisplay = new ImageDisplay(imageLabel);
    }
}
