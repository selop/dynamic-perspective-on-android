package de.tud.lopatkin.masterproject;

import android.app.ActionBar;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;
import org.rajawali3d.surface.IRajawaliSurface;
import org.rajawali3d.surface.RajawaliSurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ListIterator;

import de.tud.lopatkin.masterproject.tracking.AdjustableCameraView;
import de.tud.lopatkin.masterproject.tracking.JavaTracker;
import de.tud.lopatkin.masterproject.views.AbstractTrackingRenderer;
import de.tud.lopatkin.masterproject.views.PlanesRenderer;

public class MainActivity extends ActionBarActivity implements
CvCameraViewListener2, OnTouchListener, SensorEventListener {

	private static final String TAG = "MasterProject::Activity";

	private List<android.hardware.Camera.Size> mResolutionList;

	/// Tracking components
	private File mCascadeFile;
	private CascadeClassifier mJavaDetector;	
	private AdjustableCameraView mOpenCvCameraView;
	private JavaTracker jTracker; 
	
	// OpenGL Renderer class
	private AbstractTrackingRenderer mRenderer;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				
				try {
					// load cascade file from application resources
					InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
					File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
					mCascadeFile = new File(cascadeDir,"lbpcascade_frontalface.xml");
					FileOutputStream os = new FileOutputStream(mCascadeFile);

					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) {
						os.write(buffer, 0, bytesRead);
					}
					is.close();
					os.close();

					mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
					if (mJavaDetector.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mJavaDetector = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from "+ mCascadeFile.getAbsolutePath());
					cascadeDir.delete();

				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
				}
				
				// has all the OpenCV calls to detect faces / eyes
				jTracker = new JavaTracker(mJavaDetector);

				mOpenCvCameraView.enableView();
				mOpenCvCameraView.enableFpsMeter();
                // TODO: check how to fix actionbar resolution problem
                //mOpenCvCameraView.setMaxFrameSize(480,320);
			}
				break;

			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};
	
	public MainActivity() {
		Log.i(TAG, "Instantiated new " + ((Object) this).getClass());
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.i(TAG, "called onCreate");
    	super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Open a custom CameraView with options to adjust resolution
        mOpenCvCameraView = (AdjustableCameraView) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setOnTouchListener(this);

    	// Make sure the screen won't dim
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Create OpenGL Surface
        final RajawaliSurfaceView surface = new RajawaliSurfaceView(this);
        surface.setFrameRate(60.0);
        surface.setRenderMode(IRajawaliSurface.RENDERMODE_WHEN_DIRTY);

        // enable transparent background
        surface.setTransparent(true);

        // Add mSurface to your root view
        addContentView(surface, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT));

		// assign our renderer class for 3D related processing
        mRenderer = new PlanesRenderer(this);
        //mRenderer = new CubeRoomRenderer(this);
        surface.setSurfaceRenderer(mRenderer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem[] mResolutionMenuItems;
        SubMenu mResolutionMenu;

        int idx = 0;
        mResolutionMenu = menu.addSubMenu("Resolution");
        mResolutionList = mOpenCvCameraView.getResolutionList();
        mResolutionMenuItems = new MenuItem[mResolutionList.size()];

        ListIterator<android.hardware.Camera.Size> resolutionItr = mResolutionList.listIterator();

        while (resolutionItr.hasNext()) {
            android.hardware.Camera.Size element = resolutionItr.next();
            mResolutionMenuItems[idx] = mResolutionMenu.add(
                    2,
                    idx,
                    Menu.NONE,
                    Integer.valueOf(element.width).toString() + "x" +
                    Integer.valueOf(element.height).toString());
            idx++;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        if (item.getGroupId() == 2)
        {
            int id = item.getItemId();
            android.hardware.Camera.Size resolution = mResolutionList.get(id);
            mOpenCvCameraView.setResolution(resolution);
            resolution = mOpenCvCameraView.getResolution();
            String caption = Integer.valueOf(resolution.width).toString() +
                    "x" + Integer.valueOf(resolution.height).toString();
            Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
            return super.onOptionsItemSelected(item);
        }

        switch (item.getItemId()) {
            case R.id.menu_item_face20:
                jTracker.setMinFaceSize(0.2f);
                return true;
            case R.id.menu_item_face30:
                jTracker.setMinFaceSize(0.3f);
                return true;
            case R.id.menu_item_face40:
                jTracker.setMinFaceSize(0.4f);
                return true;
            case R.id.menu_item_face50:
                jTracker.setMinFaceSize(0.5f);
                return true;
            case R.id.menu_show_tracking:
                if(mRenderer.isShowTracking())
                    mRenderer.setShowTracking(false);
                else
                    mRenderer.setShowTracking(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {}

	@Override
	public void onSensorChanged(SensorEvent arg0) {}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
        int _xDelta = 0;
        int _yDelta = 0;

        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                _xDelta = X - lParams.leftMargin;
                _yDelta = Y - lParams.topMargin;
                break;
            case MotionEvent.ACTION_MOVE:
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                layoutParams.leftMargin = X - _xDelta;
                layoutParams.topMargin = Y - _yDelta;
                view.setLayoutParams(layoutParams);
                break;
        }
        mOpenCvCameraView.invalidate();
        return true;
	}

	public void onCameraViewStarted(int width, int height) {
		jTracker.init(width,height);
		jTracker.setRenderer(mRenderer);
	}

	public void onCameraViewStopped() {
		jTracker.release();
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		if(inputFrame.rgba().empty()){
			Log.e(TAG, "input frame empty..");
			return null;
		}
		return jTracker.detectFace(inputFrame);
	}	
	
	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
	}
	


}
