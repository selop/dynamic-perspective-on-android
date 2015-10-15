package de.tud.lopatkin.app.views;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.SplineTranslateAnimation3D;
import org.rajawali3d.curves.CatmullRomCurve3D;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.util.GLU;
import org.rajawali3d.util.ObjectColorPicker;
import org.rajawali3d.util.OnObjectPickedListener;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import de.tud.lopatkin.app.R;
import de.tud.lopatkin.app.model.PlanesGalore;
import de.tud.lopatkin.app.model.PlanesGaloreMaterialPlugin;

public class PlanesRenderer extends AbstractTrackingRenderer implements OnObjectPickedListener {

    /**
     * The tag for logging.
     */
    private static final String TAG = "PlanesRenderer";

    public Context context;

    private Material mMaterial;
    private PlanesGaloreMaterialPlugin mMaterialPlugin;

    private ObjectColorPicker mPicker;
    private Object3D mSelectedObject;
    private int[] mViewport;
    private double[] mNearPos4;
    private double[] mFarPos4;
    private Vector3 mNearPos;
    private Vector3 mFarPos;
    private Vector3 mNewObjPos;
    private Matrix4 mViewMatrix;
    private Matrix4 mProjectionMatrix;

	//private OffAxisPerspective mOffAxisPerspective;

	private long mStartTime;

	public PlanesRenderer(Context context) {
		super(context);
        this.context = context;
		setFrameRate(60);
	}

	protected void initScene() {
        mViewport = new int[] { 0, 0, getViewportWidth(), getViewportHeight() };
        mNearPos4 = new double[4];
        mFarPos4 = new double[4];
        mNearPos = new Vector3();
        mFarPos = new Vector3();
        mNewObjPos = new Vector3();
        mViewMatrix = getCurrentCamera().getViewMatrix();
        mProjectionMatrix = getCurrentCamera().getProjectionMatrix();

        mPicker = new ObjectColorPicker(this);
        mPicker.setOnObjectPickedListener(this);

        PointLight light = new PointLight();
        light.setPosition(0,0,0);
        light.setPower(10f);
        getCurrentScene().addLight(light);
        getCurrentCamera().setPosition(0, 0, 0);

        final PlanesGalore planes = new PlanesGalore();
        mMaterial = planes.getMaterial();
        mMaterial.setColorInfluence(0);
        try {
            mMaterial.addTexture(new Texture("flickrPics", R.drawable.flickrpics));
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }

        mMaterialPlugin = planes.getMaterialPlugin();

        planes.setDoubleSided(true);
        planes.setZ(4);
        mPicker.registerObject(planes);

        planes.setScale(10f);

        getCurrentScene().addChild(planes);

        Object3D empty = new Object3D();
        getCurrentScene().addChild(empty);

        CatmullRomCurve3D path = new CatmullRomCurve3D();
        path.addPoint(new Vector3(-4, 0, -20));
        path.addPoint(new Vector3(2, 1, -10));
        path.addPoint(new Vector3(-2, 0, 10));
        path.addPoint(new Vector3(0, -4, 20));
        path.addPoint(new Vector3(5, 10, 30));
        path.addPoint(new Vector3(-2, 5, 40));
        path.addPoint(new Vector3(3, -1, 60));
        path.addPoint(new Vector3(5, -1, 70));

        final SplineTranslateAnimation3D anim = new SplineTranslateAnimation3D(path);
        anim.setDurationMilliseconds(20000);
        anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
        anim.setTransformable3D(getCurrentCamera());
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        getCurrentScene().registerAnimation(anim);
        //anim.play();

        getCurrentCamera().setLookAt(new Vector3(0, 0, 30));
	}

    public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
        super.onRenderSurfaceSizeChanged(gl, width, height);
        mViewport[2] = getViewportWidth();
        mViewport[3] = getViewportHeight();
        mViewMatrix = getCurrentCamera().getViewMatrix();
        mProjectionMatrix = getCurrentCamera().getProjectionMatrix();
    }

    @Override
    public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
        super.onRenderSurfaceCreated(config, gl, width, height);
        mStartTime = System.currentTimeMillis();
    }

    @Override
	public void onRender(final long elapsedTime, final double deltaTime) {
        super.onRender(elapsedTime, deltaTime);
		
		// -- set the background color to be transparent
		// you need to have called setGLBackgroundTransparent(true); in the
		// activity for this to work.
		if(showTracking){
			getCurrentScene().setBackgroundColor(0, 0, 0, 0);
		} else {
			getCurrentScene().setBackgroundColor(0, 0, 0, 255);
		}

        mMaterial.setTime((System.currentTimeMillis() - mStartTime) / 1000f);
        mMaterialPlugin.setCameraPosition(getCurrentCamera().getPosition());
	}

    @Override
    public void onTouchEvent(MotionEvent event){
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getObjectAt(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                moveSelectedObject(event.getX(),
                        event.getY());
                break;
            case MotionEvent.ACTION_UP:
                stopMovingSelectedObject();
                break;
        }
    }

    public void getObjectAt(float x, float y) {
        mPicker.getObjectAt(x, y);
    }

    public void onObjectPicked(Object3D object) {
        mSelectedObject = object;
    }

    public void moveSelectedObject(float x, float y) {
        if (mSelectedObject == null){
            Log.e(TAG,"mSelectedObject = null");
            return;
        }

        //
        // -- unproject the screen coordinate (2D) to the camera's near plane
        //

        GLU.gluUnProject(x, getViewportHeight() - y, 0, mViewMatrix.getDoubleValues(), 0,
                mProjectionMatrix.getDoubleValues(), 0, mViewport, 0, mNearPos4, 0);

        //
        // -- unproject the screen coordinate (2D) to the camera's far plane
        //

        GLU.gluUnProject(x, getViewportHeight() - y, 1.f, mViewMatrix.getDoubleValues(), 0,
                mProjectionMatrix.getDoubleValues(), 0, mViewport, 0, mFarPos4, 0);

        //
        // -- transform 4D coordinates (x, y, z, w) to 3D (x, y, z) by dividing
        // each coordinate (x, y, z) by w.
        //

        mNearPos.setAll(mNearPos4[0] / mNearPos4[3], mNearPos4[1]
                / mNearPos4[3], mNearPos4[2] / mNearPos4[3]);
        mFarPos.setAll(mFarPos4[0] / mFarPos4[3],
                mFarPos4[1] / mFarPos4[3], mFarPos4[2] / mFarPos4[3]);

        //
        // -- now get the coordinates for the selected object
        //

        double factor = (Math.abs(mSelectedObject.getZ()) + mNearPos.z)
                / (getCurrentCamera().getFarPlane() - getCurrentCamera()
                .getNearPlane());

        mNewObjPos.setAll(mFarPos);
        mNewObjPos.subtract(mNearPos);
        mNewObjPos.multiply(factor);
        mNewObjPos.add(mNearPos);

        mSelectedObject.setX(mNewObjPos.x);
        mSelectedObject.setY(mNewObjPos.y);
    }

    public void stopMovingSelectedObject() {
        mSelectedObject = null;
    }

    public void onOffsetsChanged(float x, float y, float z, float w, int i, int j){}

    public void setAccelerometerValues(float x, float y, float z) {
        mAccValues.setAll(-x, -y, -z);
    }

    public Vector3 getAccelerometerValues(){ return mAccValues; }

    public void setCamTracking(){
        trackingMode = TRACKING_MODE_CAM;
    }

    public void setSensorTracking(){
        trackingMode = TRACKING_MODE_SENSOR;
    }

}
