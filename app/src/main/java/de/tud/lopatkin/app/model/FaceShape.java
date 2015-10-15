package de.tud.lopatkin.app.model;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import de.tud.lopatkin.app.util.Color;

/**
 * @author Sergej Lopatkin
 */
public class FaceShape extends AbstractShape implements OpenCVShape {

    public FaceShape(Rect face, Mat mRgba) {
        super(face, mRgba);
    }

    @Override
    public void draw() {
        Point leftPt   = new Point(face.x,face.y+face.height*0.37 );
        Point rightPt  = new Point(face.x+face.width, face.y+face.height*0.37 );
        Point topPt    = new Point(face.x+face.width*0.5, face.y);
        Point bottomPt = new Point(face.x+face.width*0.5, face.y+face.height);

        Core.line(mRgba, leftPt, rightPt, Color.WHITE, 1, 1, 0);
        Core.line(mRgba, topPt, bottomPt, Color.WHITE, 1, 1, 0);
        Core.rectangle(mRgba, face.tl(), face.br(), Color.FACE_RECT_COLOR, 3);


    }
}
