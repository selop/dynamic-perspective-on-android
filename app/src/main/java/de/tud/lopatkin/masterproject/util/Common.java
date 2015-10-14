package de.tud.lopatkin.masterproject.util;

import android.hardware.Camera;

/**
 * Util class to reduce code base in logic classes.
 * Commonly used Lib calls such as
 * "Integer.valueOf(resolution.width).toString()" should be capsuled here.
 */
public class Common {

    public static String asString(Camera.Size resolution)
    {
        return String.valueOf(resolution.width)+"x"+String.valueOf(resolution.height);
    }
}
