// =============================================================================
// Project 01
// Guanlun Zhao
// Sept. 12, 2016
// Run instructions:
//   * Use 'make' to compile the source.
//   * run ./ppmview <INPUT_FILE> <OUTPUT_FILE>.
//   * Note that <OUTPUT_FILE> is optional, but <INPUT_FILE> is required.
// Bonus point implemented:
//   * This programs reads P3/P6 and writes P6.
//
// =============================================================================

#include <cstdlib>
#include <cstdio>
#include <cstring>
#include <iostream>
#include <GL/glut.h>

using namespace std;

// =============================================================================
// These variables will store the input ppm image's width, height, and color
// depth values read in from the ppm header.
// =============================================================================
int width, height, maxcolor;

// =============================================================================
// You need 1 byte to store the information in an 8-bit color channel, and an
// unsigned char provides 1 byte. Use an unsigned char array to store the rgb
// pixmap data. glDrawPixels() requires an array of bytes in the order red,
// green, blue, red, green, blue, etc... , but you can also use the order red,
// green, blue, alpha, red, green, blue, alpha, etc... , if you want to give
// each pixel an alpha channel. Just make sure to change GL_RGB to GL_RGBA in
// glDrawPixels(); and GLUT_RGB to GLUT_RGBA in glutInitDisplayMode();
// =============================================================================
unsigned char *pixmap;

// =============================================================================
// You'll need to modify these functions to read and write pixmaps from ppm
// files. Here writePPM() does nothing and readPPM() just creates an empty
// white pixmap. Remember when you modify these function that OpenGL needs a
// pixmap with row positions reversed (going from bottom to top instead of
// from top to bottom). You may want to modify these functions' interfaces to
// to take arguments like char or FILE pointers so you can pass in filenames
// or open file streams.
// =============================================================================
void readPPM(FILE* input)
{
    char format[255];
    fscanf(input, "%s", format);
    fgetc(input);

    char c;

    while (true) {
        c = fgetc(input);
        if (c != '#') {
            // go back by 1 since it's no longer a comment line
            fseek(input, -1, SEEK_CUR);
            break;
        }
        
        while (c != '\n') {
            c = fgetc(input);
        }
    }

    fscanf(input, "%d %d %d", &width, &height, &maxcolor);
    fgetc(input);

    // [width * height] is the number of pixels; If you index the pixels (unsigned int), you'll 
    // need to do bit shifting to access color channels (refering Lecture 2 Notes).
    // [width * height * 3] is the number of color channels for rgb pixels;
    // [width * height * 4] is the number of color channels for rgba pixels.
    pixmap = new unsigned char[width * height * 3];

    int y, x, pixel;
    unsigned char red, green, blue;
    int i_red, i_green, i_blue;
    for(y = 0; y < height; y++) {
        for(x = 0; x < width; x++) {
            pixel = ((height - y - 1) * width + x) * 3; 

            if (strcmp(format, "P6") == 0) {
                red = fgetc(input);
                green = fgetc(input);
                blue = fgetc(input);

                pixmap[pixel] = red;
                pixmap[pixel + 1] = green;
                pixmap[pixel + 2] = blue;
            } else if (strcmp(format, "P3") == 0) {
                fscanf(input, "%d %d %d ", &i_red, &i_green, &i_blue);

                pixmap[pixel] = i_red;
                pixmap[pixel + 1] = i_green;
                pixmap[pixel + 2] = i_blue;
            } else {
                cout << "Only P3 and P6 are supported" << endl;
            }
        }
    }
}

void writePPM(FILE* output)
{
    fprintf(output, "P6\n");

    fprintf(output, "%d %d\n%d\n", width, height, maxcolor);

    int x, y, pixel = 0;
    for (y = 0; y < height; y++) {
        for (x = 0; x < width; x++) {
            pixel = ((height - y - 1) * width + x) * 3; 

            fputc(pixmap[pixel], output);
            fputc(pixmap[pixel + 1], output);
            fputc(pixmap[pixel + 2], output);
        }
    }

    fclose(output);
}

// =============================================================================
// OpenGL Display and Mouse Processing Functions.
//
// You can read up on OpenGL and modify these functions, as well as the commands
// in main(), to perform more sophisticated display or GUI behavior. This code
// will service the bare minimum display needs for most assignments.
// =============================================================================
static void resize(int w, int h)
{   
    glViewport(0, 0, w, h);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    glOrtho(0,(w/2),0,(h/2),0,1); 
    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity() ;
}
static void display(void)
{
    glClear(GL_COLOR_BUFFER_BIT);
    glRasterPos2i(0,0);
    glPixelStorei(GL_UNPACK_ALIGNMENT,1);
    glDrawPixels(width, height, GL_RGB, GL_UNSIGNED_BYTE, pixmap);
    glFlush();
}
static void processMouse(int button, int state, int x, int y)
{
    if(state == GLUT_UP)
    exit(0);               // Exit on mouse click.
}
static void init(void)
{
    glClearColor(1,1,1,1); // Set background color.
}

// =============================================================================
// main() Program Entry
// =============================================================================
int main(int argc, char *argv[])
{
    // Check command line arguments and read in the filename, then call readPPM().
    if (argc >= 2) {
        char* inFilename = argv[1];
        FILE* input = fopen(inFilename, "r");

        if (input == NULL) {
            cout << "File " << inFilename << " does not exist" << endl;
            return 0;
        }

        readPPM(input);
    } else {
        cout << "Invalid Arguments!" << endl;
    }

    if (argc == 3) {
        const char* outFilename = argv[2];

        FILE* output = fopen(outFilename, "w");

        if (output == NULL) {
            cout << "Cout not write to file " << outFilename << endl;
            return 0;
        }

        writePPM(output);
    }

    // OpenGL commands
    glutInit(&argc, argv);
    glutInitWindowPosition(100, 100); // Where the window will display on-screen.
    glutInitWindowSize(width, height);
    glutInitDisplayMode(GLUT_RGB | GLUT_SINGLE);
    glutCreateWindow("ppmview");
    init();
    glutReshapeFunc(resize);
    glutDisplayFunc(display);
    glutMouseFunc(processMouse);
    glutMainLoop();

    return 0;
}

// =============================================================================
// GENERAL RECOMMENDATIONS AND SOLUTIONS TO COMMON PROBLEMS
//
// Use FILE, fopen, fclose, fscanf, fprintf, fgetc, and fputc to construct
// the readPPM and writePPM functions. The isspace() function from ctype.h
// is also very useful. You can learn about basic C file I/O issues in the
// excellent Wikipedia entry here:
//
// http://en.wikipedia.org/wiki/C_file_input/output
//
// Pay special attention to the section on the "The EOF Pitfall". Values
// returned from fgetc should be stored in an int, not a char.
//
// You can make more sophisticated versions of these functions using the C++
// <fstream> library and cin/cout. This is probably overkill for the ppm
// utilities you're creating in this class, unless you are already comfortable
// with the C++ way of doing things.
//
// If you stick with a more C-style approach, I strongly recommend you use
// fgetc and fputc to test for the P6 'magic number' bytes and to skip over
// any comment lines. When you've finished skipping over comments, scan-in
// the image width, height and color depth using this line:
//
//      fscanf(imagefile, "%d %d %d", &width, &height, &maxcolor);
//
// Then use this line to grab the last newline character after the maxcolor byte:
//
//      fgetc(imagefile);
//
// after which I recommend using fgetc to read in color channel byte values.
//
// This method is proven and works well. You should adhere to it in your own
// program unless you thoroughly test your own alternate methods.
//
//
// *** IMPORTANT ***
//
// DON'T DO THIS:
//
//      fscanf(imagefile, "%d %d\n %d\n", &width, &height, &maxcolor);
//
// OR THIS:
//
//      fscanf(imagefile, "%d %d %d\n", &width, &height, &maxcolor);
//
// OR THIS:
//
//      fscanf(imagefile, "\n", &ch);
//
// to scan in the last newline after the maxcolor byte.
//
// fscanf is not required to only read in one char byte for each explicit
// newline scan. This is because C binary I/O allows a newline character to
// sometimes be output as two characters, depending on the target output file
// type and other system conventions, and likewise be identified during scan in
// from two char bytes of an input file. fscanf may scan the newline and also
// the following first red color channel byte, if the hex value for that red
// color channel byte corresponds to a character that fscanf interprets as part
// of the newline. This definitely happens if the first red color channel has
// the hex value 0B (which is the hex value for a vertical tab character), but
// fscanf may be vulnerable to other newline-trailing hex values as well.
// If you must use fscanf to read in the last newline, you need to instead
// explicitely instruct fscanf to read in a single char:
//
// fscanf(imagefile, "%c", &ch);
//
// But fgetc(imagefile) will work fine too.
//
// Don't attempt to search for and skip over white space or comments lines after
// the newline that follows the maxcolor byte - there should never be any such
// chunks of data in a valid ppm file. At best you will add useless code to
// your project; at worst you will generate the same problem as the fscanf issue
// above - you may accidentally scan the first red color channel byte, and
// possibly following bytes as well, if the hex values for these bytes
// correspond to whitespace or comment line characters that you're skipping.
//
//
// For image output I recommend you use fprintf to write out the ppm header
// data:
//
//     fprintf(outfile, "P6\n");
//     fprintf(outfile, "%d %d\n", width, height);
//     fprintf(outfile, "255\n");
//
// and then fputc to output color channel byte values.
// =============================================================================
