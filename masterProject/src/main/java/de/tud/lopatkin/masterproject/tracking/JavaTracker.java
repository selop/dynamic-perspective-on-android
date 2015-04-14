package de.tud.lopatkin.masterproject.tracking;


import android.util.Log;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.rajawali3d.Camera;
import org.rajawali3d.renderer.RajawaliRenderer;

import java.math.BigDecimal;

import de.tud.lopatkin.masterproject.util.Color;

public class JavaTracker {
	
	private static final String TAG = "JavaTracker";

	private Mat mRgba,mGray;
	private float mRelativeFaceSize = 0.25f, f=500;
	private int mAbsoluteFaceSize = 0;
	private CascadeClassifier mJavaDetector;	

	private RajawaliRenderer renderer;

	int camWidth, camHeight;

	public JavaTracker(CascadeClassifier mJavaDetector2) {
		this.mJavaDetector = mJavaDetector2;
	}

	public Mat detectFace(CvCameraViewFrame inputFrame){

        if ( inputFrame.rgba().empty() ){
            Log.e(TAG, "inputFrame was empty.. returning empty Mat()");
            return new Mat();
        }

        Camera cam = renderer.getCurrentCamera();

        mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();

        // flip the preview image to make sure the preview acts like a mirror
		Core.flip(mRgba, mRgba, 1);
		Core.flip(mGray, mGray, 1);
		
		//Imgproc.GaussianBlur(mGray, mGray, new Size(), 2, 2, Imgproc.BORDER_DEFAULT);
		Imgproc.equalizeHist(mGray, mGray);

		if (mAbsoluteFaceSize == 0) {
			int height = mGray.rows();
			if (Math.round(height * mRelativeFaceSize) > 0)
				mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
		}

		MatOfRect matFaces = new MatOfRect();
        Size faceSize = new Size(mAbsoluteFaceSize, mAbsoluteFaceSize);

        long startTime = System.nanoTime();

		mJavaDetector.detectMultiScale(mGray, matFaces, 1.1, 2, 2, faceSize, new Size());

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        Log.d(TAG, "detection faces time : " + duration/1000000 +" ms");

		Rect[] faces = matFaces.toArray();
		for (int i = 0; i < faces.length; i++) {
			Log.d(TAG, "found faces : " + faces.length);

            //-- points in pixel
			Point leftEye = new Point(faces[i].x+faces[i].width*0.3, faces[i].y+faces[i].height*0.37 );
			Point rightEye = new Point(faces[i].x+faces[i].width*0.7, faces[i].y+faces[i].height*0.37 );
			Point eyeCenter = new Point(faces[i].x+faces[i].width*0.5, leftEye.y );

            //-- normalize with front cam internal parameters
			double normRightEye = (rightEye.x  - camWidth/2)  / f;
			double normLeftEye  = (leftEye.x   - camWidth/2)  / f;
			double normCenterX  = (eyeCenter.x - camWidth/2)  / f;
			double normCenterY  = (eyeCenter.y - camHeight/2) / f;

            //-- get space coordinates
			float tempZ = (float) (6.5f / (normRightEye-normLeftEye));
			float tempX = (float) (normCenterX *cam.getZ());
			float tempY = (float) (-normCenterY*cam.getZ());

            // TODO: this is a hack
            tempZ=tempZ-100;

			cam.setX(cam.getX()*0.2f  + tempX*4f);
			cam.setY(cam.getY()*0.5f  + tempY*0.5f);
			cam.setZ(cam.getZ()*0.99f  + tempZ*0.01f);

			Log.d(TAG, "Coord  Cam : " + cam.getX() + " " + cam.getY() + " " + cam.getZ());
			Log.d(TAG, "Coord Temp : " + tempX + " " + tempY + " " + tempZ );

			Core.rectangle(mRgba, faces[i].tl(), faces[i].br(), Color.FACE_RECT_COLOR, 3);

			Point leftPt   = new Point(faces[i].x,faces[i].y+faces[i].height*0.37 );
			Point rightPt  = new Point(faces[i].x+faces[i].width, faces[i].y+faces[i].height*0.37 );
			Point topPt    = new Point(faces[i].x+faces[i].width*0.5, faces[i].y);
			Point bottomPt = new Point(faces[i].x+faces[i].width*0.5, faces[i].y+faces[i].height);
			Core.line(mRgba, leftPt, rightPt, Color.WHITE, 1, 1, 0);
			Core.line(mRgba, topPt, bottomPt, Color.WHITE, 1, 1, 0);

			Core.circle(mRgba, leftEye , (int) (0.06*faces[i].width), Color.BLACK , 1, 8, 0);
			Core.circle(mRgba, rightEye, (int) (0.06*faces[i].width), Color.BLACK, 1, 8, 0);

			Core.line(mRgba, leftEye, rightEye, Color.BLUE, 1, 1, 0);
			Core.circle(mRgba, eyeCenter, 2,  Color.BLUE, 3, 1, 0);

			Core.putText(mRgba, "[" + eyeCenter.x + "," + eyeCenter.y + "]",
					new Point(eyeCenter.x + 20, eyeCenter.y + 20),
					Core.FONT_HERSHEY_SIMPLEX, 0.7, Color.BLACK);
		}

		return mRgba;
	}
	
	public static double round(double d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }
	
	public void init(int width, int height) {
		this.camWidth = width;
		this.camHeight = height;
		mGray = new Mat();
		mRgba = new Mat();
	}
	
	public void release() {
		Log.i(TAG, "releasing Gray + RGBA");
		mGray.release();
		mRgba.release();
	}

	public float getCamWidth() {
		return camWidth;
	}
	public void setCamWidth(int camWidth) {
		this.camWidth = camWidth;
	}
	public float getCamHeight() {
		return camHeight;
	}
	public void setCamHeight(int camHeight) {
		this.camHeight = camHeight;
	}
    public RajawaliRenderer getRenderer() {
        return renderer;
    }
    public void setRenderer(RajawaliRenderer renderer) {
        this.renderer = renderer;
    }
}
