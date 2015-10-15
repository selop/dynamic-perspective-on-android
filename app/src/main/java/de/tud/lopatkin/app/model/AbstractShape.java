package de.tud.lopatkin.app.model;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

/**
 * @author Sergej Lopatkin
 */
public abstract class AbstractShape {
    protected Rect face;
    protected Mat mRgba;

    public AbstractShape(Rect face, Mat mRgba) {
        this.face = face;
        this.mRgba = mRgba;
    }
}
