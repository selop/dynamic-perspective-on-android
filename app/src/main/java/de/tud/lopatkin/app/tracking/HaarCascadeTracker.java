package de.tud.lopatkin.app.tracking;


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

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.renderer.RajawaliRenderer;

import de.tud.lopatkin.app.model.EyesShape;
import de.tud.lopatkin.app.model.FaceShape;
import de.tud.lopatkin.app.model.OpenCVShape;
import de.tud.lopatkin.app.util.Color;

/**
 * Simple tracking algorithm: Detect a users face via Haar-Cascade classifier and synchronize
 * the renderer Camera classes position.
 *
 * @author Sergej Lopatkin
 */
public class HaarCascadeTracker implements Tracker {
	
	private static final String TAG = "HaarCascadeTracker";

	/**
	 * The image in color and grey format.
	 */
	private Mat mRgba,mGray;

	/**
	 * Setting an absolute value for the approx. face size relative to the screen.
	 *
	 */
	private float mRelativeFaceSize = 0.25f;
	private int mAbsoluteFaceSize = 0;

    /**
     * Reference to the trained cascade file.
     */
    private CascadeClassifier classifier;

    /**
     *
     * Reference to the renderer class to sync. user position with camera position.
     */
	private RajawaliRenderer renderer;

    /**
     * Is tracking enabled?*
     */
	private boolean cameraTrackingEnabled = true;

    /**
     *  Image width, heigt. Depends on the input CVFrame.
     */
	int camWidth, camHeight;

	/**
	 *  The number of detected faces per frame.
	 */
	private MatOfRect matFaces;

	public HaarCascadeTracker(CascadeClassifier classifier) {
		this.classifier = classifier;
	}

	@Override
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
		Imgproc.equalizeHist(mGray, mGray);

		if (mAbsoluteFaceSize == 0) {
			int height = mGray.rows();
			if (Math.round(height * mRelativeFaceSize) > 0)
				mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
		}

		matFaces = new MatOfRect();
        Size faceSize = new Size(mAbsoluteFaceSize, mAbsoluteFaceSize);

        long startTime = System.nanoTime();

		if(cameraTrackingEnabled)
			classifier.detectMultiScale(mGray, matFaces, 1.1, 2, 2, faceSize, new Size());

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
		Log.d(TAG,"tracking Enabled: " + cameraTrackingEnabled);
        Log.d(TAG, "detection faces time : " + duration / 1000000 + " ms");

		Rect[] faces = matFaces.toArray();

        //TODO : cam interal parameter find out which
        float f=801;
		for (Rect face : faces) {
			Log.d(TAG, "found faces : " + faces.length);

            //-- points in pixel
			Point leftEye   = new Point(face.x+face.width*0.3, face.y+face.height*0.37 );
			Point rightEye  = new Point(face.x+face.width*0.7, face.y+face.height*0.37 );
			Point eyeCenter = new Point(face.x+face.width*0.5, leftEye.y );

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

			cam.setX(cam.getX() * 0.2f + tempX * 5f);
			cam.setY(cam.getY() * 0.2f + tempY * 4f);
			//cam.setZ(cam.getZ()*0.999f  + tempZ*0.001f);

			Log.d(TAG, "Coord  Cam : " + cam.getX() + " " + cam.getY() + " " + cam.getZ());
			Log.d(TAG, "Coord  Tmp : " + tempX + " " + tempY + " " + tempZ);

			// drawing a green box surrounding the face
			OpenCVShape faceBox = new FaceShape(face, mRgba);
			faceBox.draw();

			// drawing circles at the position of the eyes
			OpenCVShape eyes = new EyesShape(face,mRgba,leftEye,rightEye,eyeCenter);
			eyes.draw();

			Point offsetToEyeCenter = new Point(eyeCenter.x + 20, eyeCenter.y + 20);
			Core.putText(mRgba, "[" + eyeCenter.x + "," + eyeCenter.y + "]",
					offsetToEyeCenter,
					Core.FONT_HERSHEY_SIMPLEX, 0.7, Color.BLACK);
		}

		return mRgba;
	}

	public MatOfRect getMatFaces() {
		return matFaces;
	}

	public void setMatFaces(MatOfRect matFaces) {
		this.matFaces = matFaces;
	}

    public void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

	public void init(int width, int height) {
        Log.i(TAG, "Init tracker with width: " + width + " and " + height);
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

    public void setRenderer(RajawaliRenderer renderer) {
        this.renderer = renderer;
    }

	public void setCameraTrackingEnabled(boolean cameraTrackingEnabled) {
		this.cameraTrackingEnabled = cameraTrackingEnabled;
	}

}
