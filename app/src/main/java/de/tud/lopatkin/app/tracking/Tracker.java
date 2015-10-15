package de.tud.lopatkin.app.tracking;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

/**
 * @author Sergej Lopatkin
 */
public interface Tracker {

    /**
     * Detecting a face in a camera image
     * To inform the user if he is tracked a box is drawn in the position of the face.
     *
     * @return The processed image.
     */
    Mat detectFace(CameraBridgeViewBase.CvCameraViewFrame inputFrame);
}
