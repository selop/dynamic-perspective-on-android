package de.tud.lopatkin.masterproject.views;

import android.content.Context;
import android.util.Log;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.RajawaliRenderer;
import org.rajawali3d.util.GLU;
import org.rajawali3d.util.ObjectColorPicker;
import org.rajawali3d.util.OnObjectPickedListener;

import javax.microedition.khronos.opengles.GL10;


/**
 * Created by selop on 12/04/15.
 */
public abstract class AbstractTrackingRenderer extends RajawaliRenderer implements OnObjectPickedListener {

    public static final int TRACKING_MODE_CAM = 0;
    public static final int TRACKING_MODE_SENSOR = 1;

    protected Vector3 mAccValues;

    public Float baseAzimuth;

    public Float basePitch;

    public Float baseRoll;

    protected int trackingMode;

    public AbstractTrackingRenderer(Context context){
        super(context);
        mViewport = new int[] { 0, 0, getViewportWidth(), getViewportHeight() };
    }

    abstract public Vector3 getAccelerometerValues();
    abstract public void setAccelerometerValues(float x, float y, float z);
    abstract public void setCamTracking();
    abstract public void setSensorTracking();

    protected boolean showTracking = true;

    private Object3D mSelectedObject;
    private int[] mViewport;
    private double[] mNearPos4;
    private double[] mFarPos4;
    private Vector3 mNearPos;
    private Vector3 mFarPos;
    private Vector3 mNewObjPos;
    private Matrix4 mViewMatrix;
    private Matrix4 mProjectionMatrix;
    protected ObjectColorPicker mPicker;

    public void toggleShowTracking(){
        if(showTracking)
            showTracking = false;
        else
            showTracking = true;
    }

    public void stopMovingSelectedObject() {
        mSelectedObject = null;
    }

    public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
        super.onRenderSurfaceSizeChanged(gl, width, height);
        mViewport[2] = getViewportWidth();
        mViewport[3] = getViewportHeight();
        mViewMatrix = getCurrentCamera().getViewMatrix();
        mProjectionMatrix = getCurrentCamera().getProjectionMatrix();
    }

    @Override
    protected void initScene(){
        mPicker = new ObjectColorPicker(this);
        mPicker.setOnObjectPickedListener(this);

        mNearPos4 = new double[4];
        mFarPos4 = new double[4];
        mNearPos = new Vector3();
        mFarPos = new Vector3();
        mNewObjPos = new Vector3();
        mViewMatrix = getCurrentCamera().getViewMatrix();
        mProjectionMatrix = getCurrentCamera().getProjectionMatrix();
    }

    public void getObjectAt(float x, float y) {
        mPicker.getObjectAt(x, y);
    }

    public void onObjectPicked(Object3D object) {
        Log.i("abstract", "entering onObjectPicked "+object.getName());
        mSelectedObject = object;
    }

    public void moveSelectedObject(float x, float y) {
        Log.i("abstract", "entering moveSelectedObject");
        if (mSelectedObject == null){
            Log.i("abstract", "mSelectedObject null");
            return;
        }


    }
}
