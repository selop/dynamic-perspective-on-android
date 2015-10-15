package de.tud.lopatkin.app.model;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import de.tud.lopatkin.app.util.Color;

/**
 * @author Sergej Lopatkin
 */
public class EyesShape extends AbstractShape implements OpenCVShape {
    private Point leftEye;
    private Point rightEye;
    private Point eyeCenter;

    public EyesShape(Rect face, Mat mRgba, Point leftEye, Point rightEye, Point eyeCenter) {
        super(face, mRgba);
        this.leftEye = leftEye;
        this.rightEye = rightEye;
        this.eyeCenter = eyeCenter;
    }

    @Override
    public void draw() {
        Core.circle(mRgba, leftEye, (int) (0.06 * face.width), Color.BLACK, 1, 8, 0);
        Core.circle(mRgba, rightEye, (int) (0.06 * face.width), Color.BLACK, 1, 8, 0);
        Core.line(mRgba, leftEye, rightEye, Color.BLUE, 1, 1, 0);
        Core.circle(mRgba, eyeCenter, 2, Color.BLUE, 3, 1, 0);
    }
}
