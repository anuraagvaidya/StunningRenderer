package com.bysness.stunningrenderer;

import com.jogamp.opengl.GLProfile;
import org.eclipse.swt.widgets.Display;

import java.util.Scanner;

import static com.sun.glass.ui.gtk.GtkApplication.display;

/**
 * Created by Anuraag on 4/29/2016.
 */
public class StunningInstance {
    public static void main(String[] args) {
        GLProfile.initSingleton();
        // create a scanner so we can read the command-line input
        StunningScanner scanner=new StunningScanner();
        scanner.start();

    }
}
