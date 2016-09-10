// =============================================================================
// VIZA654/CSCE646 at Texas A&M University
// Homework 0
// Created by Anton Agana based from Ariel Chisholm's template
// 05.23.2011
//
// This file is supplied with an associated makefile. Put both files in the same
// directory, navigate to that directory from the Linux shell, and type 'make'.
// This will create a program called 'pr01' that you can run by entering
// 'homework0' as a command in the shell.
//
// If you are new to programming in Linux, there is an
// excellent introduction to makefile structure and the gcc compiler here:
//
// http://www.cs.txstate.edu/labs/tutorials/tut_docs/Linux_Prog_Environment.pdf
//
// =============================================================================

#include <cstdlib>
#include <iostream>
#include <GL/glut.h>

#include <fstream>
#include <cassert>
#include <sstream>
#include <cstring>
#include <cmath>

using namespace std;

// =============================================================================
// These variables will store the input ppm image's width, height, and color
// =============================================================================
int width, height;
unsigned char *pixmap;


// =============================================================================
// setPixels()
//
// This function stores the RGB values of each pixel to "pixmap."
// Then, "glutDisplayFunc" below will use pixmap to display the pixel colors.
// =============================================================================
void setPixels()
{
     for(int y = 0; y < height ; y++) {
       for(int x = 0; x < width; x++) {
         int i = (y * width + x) * 3; 
         pixmap[i++] = 255;
         pixmap[i++] = 0xFF; //Do you know what "0xFF" represents? Google it!
         pixmap[i] = 0x00; //Learn to use the "0x" notation to your advantage.
       }
     }
}

void drawSolidColor(int r, int g, int b) {
    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            int i = (y * width + x) * 3;

            pixmap[i] = r;
            pixmap[i + 1] = g;
            pixmap[i + 2] = b;
        }
    }
}

void drawAllColors() {
    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            int i = (y * width + x) * 3;

            int r, g, b;

            if (x < width / 2 && y < height / 2) {
                r = 0xFF;
                g = 0x00;
                b = 0x00;
            } else if (x >= width / 2 && y < height / 2) {
                r = 0x00;
                g = 0xFF;
                b = 0x00;
            } else if (x < width / 2 && y >= height / 2) {
                r = 0x00;
                g = 0x00;
                b = 0xFF;
            } else if (x >= width / 2 && y >= height / 2) {
                r = 0xFF;
                g = 0xFF;
                b = 0x00;
            }

            pixmap[i] = r;
            pixmap[i + 1] = g;
            pixmap[i + 2] = b;
        }
    }
}

void drawCircle() {
    int centerX = width / 2;
    int centerY = height / 2;

    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            int i = (y * width + x) * 3;

            int xDist = x - centerX;
            int yDist = y - centerY;

            int r, g, b;

            if (sqrt(xDist * xDist + yDist * yDist) < 100) {
                r = 0xFF;
                g = 0xFF;
                b = 0x00;
            } else {
                r = 0x00;
                g = 0x00;
                b = 0xFF;
            }

            pixmap[i] = r;
            pixmap[i + 1] = g;
            pixmap[i + 2] = b;
        }
    }
}

bool drawPixels(const char* type) {
    if (strcmp(type, "red") == 0) {
        drawSolidColor(0xFF, 0x00, 0x00);
    } else if (strcmp(type, "green") == 0) {
        drawSolidColor(0x00, 0xFF, 0x00);
    } else if (strcmp(type, "blue") == 0) {
        drawSolidColor(0x00, 0x00, 0xFF);
    } else if (strcmp(type, "all") == 0) {
        drawAllColors();
    } else if (strcmp(type, "circle") == 0) {
        drawCircle();
    } else {
        return false;
    }
}

// =============================================================================
// OpenGL Display and Mouse Processing Functions.
//
// You can read up on OpenGL and modify these functions, as well as the commands
// in main(), to perform more sophisticated display or GUI behavior. This code
// will service the bare minimum display needs for most assignments.
// =============================================================================
static void windowResize(int w, int h)
{   
    glViewport(0, 0, w, h);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    glOrtho(0,(w/2),0,(h/2),0,1); 
    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity() ;
}
static void windowDisplay(void)
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

    //initialize the global variables
    width = 640;
    height = 480;
    pixmap = new unsigned char[width * height * 3];  //Do you know why "3" is used?

    if (argc != 2) {
        cout << "Invalid Input" << endl;
    } else {
        char* type = argv[1];

        bool ret = drawPixels(type);
    }


    // OpenGL Commands:
    // Once "glutMainLoop" is executed, the program loops indefinitely to all
    // glut functions.  
    glutInit(&argc, argv);
    glutInitWindowPosition(100, 100); // Where the window will display on-screen.
    glutInitWindowSize(width, height);
    glutInitDisplayMode(GLUT_RGB | GLUT_SINGLE);
    glutCreateWindow("Homework Zero");
    init();
    glutReshapeFunc(windowResize);
    glutDisplayFunc(windowDisplay);
    glutMouseFunc(processMouse);
    glutMainLoop();

    return 0; //This line never gets reached. We use it because "main" is type int.
}

