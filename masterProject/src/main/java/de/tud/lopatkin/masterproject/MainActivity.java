package de.tud.lopatkin.masterproject;

import android.app.ActionBar;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
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

import butterknife.ButterKnife;
import butterknife.OnTouch;
import de.tud.lopatkin.masterproject.tracking.AdjustableCameraView;
import de.tud.lopatkin.masterproject.tracking.JavaTracker;
import de.tud.lopatkin.masterproject.util.Common;
import de.tud.lopatkin.masterproject.views.AbstractTrackingRenderer;
import de.tud.lopatkin.masterproject.views.CubeRoomRenderer;
import de.tud.lopatkin.masterproject.views.PlanesRenderer;

public class MainActivity extends ActionBarActivity implements
CvCameraViewListener2, SensorEventListener {

    /**
     * The tag for logging.
     */
	private static final String TAG = "MasterProject::Activity";

    /**
     * This field holds all possible resolutions available for the given camera.
     * Note that the deprecated class is due to the openCV implementation.
     */
    @SuppressWarnings( "deprecation" )
	private List<Camera.Size> mResolutionList;

    /**
     * The Haar cascade classifier which is used to detect faces while tracking.
     */
	private CascadeClassifier mCascadeClassifier;

    /**
     * Modified cvCameraView class which is based on the JavaCameraView class.
     *
     */
	private AdjustableCameraView mOpenCvCameraView;

    /**
     * Java based face tracker based on Haar cascade detection.
     */
	private JavaTracker jTracker;

    /**
     * The Rajawali renderer class.
     */
	private AbstractTrackingRenderer mRenderer;

    private Handler mHandler = new Handler();

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + ((Object) this).getClass());
    }

    /**
     * The Callback for loading the OpenCV library.
     * If the lib has been loaded successfully the cvCameraView is being started.
     * The fps meter will be enabled.
     * The tracker will be initialised with the loaded Haar cascade classifier.
     */
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
                    File mCascadeFile = new File(cascadeDir,"lbpcascade_frontalface.xml");
					FileOutputStream os = new FileOutputStream(mCascadeFile);

					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) {
						os.write(buffer, 0, bytesRead);
					}
					is.close();
					os.close();

					mCascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
					if (mCascadeClassifier.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mCascadeClassifier = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from "+ mCascadeFile.getAbsolutePath());
					cascadeDir.delete();

				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
				}
				
				// has all the OpenCV calls to detect faces / eyes
				jTracker = new JavaTracker(mCascadeClassifier);

				mOpenCvCameraView.enableView();
				mOpenCvCameraView.enableFpsMeter();
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

    // ------------------------------ OpenCV Callbacks --------------------------------------- //

    /**
     * Called as the cvCameraView is being started.
     *
     * @param width  - the width of the frames that will be delivered
     * @param height - the height of the frames that will be delivered
     */
    public void onCameraViewStarted(int width, int height) {
        jTracker.init(width,height);
        jTracker.setRenderer(mRenderer);
    }

    /**
     * Called as the cvCameraView is being stopped.
     *
     */
    public void onCameraViewStopped() {
        jTracker.release();
    }

    /**
     * Called on every new frame grabbed by the CvCameraView.
     *
     * @param inputFrame the current Preview frame
     * @return the processed input frame as Mat
     */
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        return jTracker.detectFace(inputFrame);
    }

    private void stallActivity() {
        Toast.makeText(this, "Delayed Toast!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.i(TAG, "called onCreate");
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler.postDelayed(new Runnable() {
            public void run() {
                stallActivity();
            }
        }, 5000);

        ButterKnife.inject(this);

        // Open a custom CameraView with options to adjust resolution
        mOpenCvCameraView = (AdjustableCameraView) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        mOpenCvCameraView.setCvCameraViewListener(this);
        //mOpenCvCameraView.setOnTouchListener(this);

        // Create OpenGL Surface
        final RajawaliSurfaceView surface = new RajawaliSurfaceView(this);
        surface.setFrameRate(60.0);
        surface.setRenderMode(IRajawaliSurface.RENDERMODE_WHEN_DIRTY);

        // enable transparent background
        surface.setTransparent(true);

        // Add mSurface to your root view
        addContentView(surface, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT));

        // assign our renderer class for 3D related processing
        // TODO: make this interchangable via fragmets or activity switches
        //mRenderer = new PlanesRenderer(this);
        mRenderer = new CubeRoomRenderer(this);
        surface.setSurfaceRenderer(mRenderer);

    	// Make sure the screen won't dim
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        SubMenu mResolutionMenu = menu.addSubMenu("Resolution");
        try {
            Thread.sleep(1000);
        }catch (Exception e){
            e.printStackTrace();
        }
        mResolutionList = mOpenCvCameraView.getResolutionList();
        int idx = 0;
        for(Camera.Size size : mResolutionList){
            mResolutionMenu.add(2,idx,Menu.NONE,Common.asString(size));
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

            Camera.Size resolution = mResolutionList.get(id);
            mOpenCvCameraView.setResolution(resolution);

            Toast.makeText(this, Common.asString(resolution), Toast.LENGTH_SHORT).show();
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
                mRenderer.toggleShowTracking();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {}

    @Override
    public void onSensorChanged(SensorEvent arg0) {}

    int prevX = 0,prevY = 0; // this has to be outside of the callback
    @OnTouch(R.id.fd_activity_surface_view)
    public boolean dragPreview(final View v,final MotionEvent event){
        final FrameLayout.LayoutParams par=(FrameLayout.LayoutParams)v.getLayoutParams();
        switch(event.getAction())
        {
            case MotionEvent.ACTION_MOVE:
            {
                par.topMargin+=(int)event.getRawY()-prevY;
                prevY=(int)event.getRawY();
                par.leftMargin+=(int)event.getRawX()-prevX;
                prevX=(int)event.getRawX();
                v.setLayoutParams(par);
                return true;
            }
            case MotionEvent.ACTION_UP:
            {
                par.topMargin+=(int)event.getRawY()-prevY;
                par.leftMargin+=(int)event.getRawX()-prevX;
                v.setLayoutParams(par);
                return true;
            }
            case MotionEvent.ACTION_DOWN:
            {
                prevX=(int)event.getRawX();
                prevY=(int)event.getRawY();
                par.bottomMargin=-2*v.getHeight();
                par.rightMargin=-2*v.getWidth();
                v.setLayoutParams(par);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mRenderer.getObjectAt(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                mRenderer.moveSelectedObject(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                mRenderer.stopMovingSelectedObject();
                break;
        }
        return true;
    }
}
