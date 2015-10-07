package de.tud.lopatkin.masterproject.views;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.cameras.ArcballCamera;
import org.rajawali3d.lights.ALight;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.util.ObjectColorPicker;
import org.rajawali3d.util.OnObjectPickedListener;
import org.rajawali3d.util.debugvisualizer.GridFloor;

import java.util.Random;

import de.tud.lopatkin.masterproject.R;

/**
 * This class displays a room to test object picking.
 */
public class CubeRoomRenderer extends AbstractTrackingRenderer implements OnObjectPickedListener {
    /**
     * The tag for logging.
     */
    private static final String TAG = "CubeRoomRenderer";
    private ALight mLight;
    private Object3D mObjectGroup;
    private Object3D crate;
    private Animation3D mCameraAnim;

    public CubeRoomRenderer(Context context) {
        super(context);
        setFrameRate(60);
        mAccValues = new Vector3();
    }

    protected void initScene() {

        mLight = new DirectionalLight(.3f, -.3f, -1);
        mLight.setPower(1f);
        getCurrentScene().addLight(mLight);
        mLight.setPosition(-2, 1, 4);

        mPicker = new ObjectColorPicker(this);
        mPicker.setOnObjectPickedListener(this);

        ArcballCamera arcball = new ArcballCamera(mContext, ((Activity)mContext).findViewById(R.id.drawer_layout));
        arcball.setPosition(0, 0, 10);
        getCurrentScene().replaceAndSwitchCamera(getCurrentCamera(), arcball);

        Material material = new Material();
        material.enableLighting(true);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());

        LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.industryrobot_obj);
        try {
            objParser.parse();
            mObjectGroup = objParser.getParsedObject();
            mObjectGroup.setScale(0.0025f);
            mObjectGroup.setZ(-2);
            mObjectGroup.setY(-5);
            mPicker.registerObject(mObjectGroup);
            //getCurrentScene().addChild(mObjectGroup);
        } catch (ParsingException e) {
            e.printStackTrace();
        }

        box();

        fillBox(material);

        mObjectGroup.setMaterial(material);
    }

    @Override
    public void onRender(final long elapsedTime, final double deltaTime) {
        super.onRender(elapsedTime, deltaTime);

        // -- set the background color to be transparent
        // you need to have called setGLBackgroundTransparent(true);
        // in the activity for this to work.
        if(showTracking)
            getCurrentScene().setBackgroundColor(0);
        else
            getCurrentScene().setBackgroundColor(0, 0, 0, 255);

        switch (trackingMode){
            case TRACKING_MODE_CAM:
                //getCurrentScene().setBackgroundColor(0, 0, 0, 255);
                break;
            case TRACKING_MODE_SENSOR:
                //mObjectGroup.setRotation(mAccValues.x, mAccValues.y + 180, mAccValues.z);
                mObjectGroup.rotate(new Vector3(1,0,0), mAccValues.y);
                mObjectGroup.rotate(new Vector3(0,1,0), mAccValues.x);
                break;
        }
    }

    public void onOffsetsChanged(float x, float y, float z, float w, int i, int j){}

    @Override
    public void onTouchEvent(MotionEvent event){
    }

    private void box(){
        GridFloor floor = new GridFloor(10, 0xff003300, 5, 20);
        floor.moveUp(-5);
        getCurrentScene().addChild(floor);

        GridFloor ceiling = new GridFloor(10, 0xff003300, 5, 20);
        ceiling.moveUp(5);
        getCurrentScene().addChild(ceiling);

        GridFloor back = new GridFloor(10, 0xff003300, 5, 20);
        back.setPosition(0, 0, -5);
        back.setRotZ(90);
        getCurrentScene().addChild(back);

        GridFloor side1 = new GridFloor(10, 0xff003300, 5, 20);
        side1.setPosition(-5, 0, 0);
        mPicker.registerObject(side1);
        side1.setRotX(90);
        getCurrentScene().addChild(side1);

        GridFloor side2 = new GridFloor(10, 0xff003300, 5, 20);
        side2.setPosition(5, 0, 0);
        mPicker.registerObject(side2);
        side2.setRotX(90);
        getCurrentScene().addChild(side2);
    }

    private void fillBox(Material mat){
        Sphere sphere = new Sphere(3, 25, 25);
        sphere.moveUp(2);
        sphere.setScale(0.12f);
        sphere.setMaterial(mat);
        sphere.setColor(0xFFA50000);
        getCurrentScene().addChild(sphere);

        for(int i = 0; i < 105; i++){
            Object3D tmp = sphere.clone(true,false); // clone material, no children though
            tmp.setPosition(0, 0,0);
            tmp.setScale(getRndDouble(0.04,0.25));
            tmp.moveForward(getRndDouble(-5, 5));
            tmp.moveRight(getRndDouble(-5, 5));
            tmp.moveUp(getRndDouble(-5, 5));
            getCurrentScene().addChild(tmp);
        }

        // TODO: Balken am Boden
    }

    private double getRndDouble(double min, double max){
        Random r = new Random();
        return min + (max - min) * r.nextDouble();
    }

    public void setAccelerometerValues(float x, float y, float z) {
        mAccValues.setAll(-x, -y, -z);
    }

    public Vector3 getAccelerometerValues() {
        return mAccValues;
    }

    public void setCamTracking(){
        trackingMode = TRACKING_MODE_CAM;
    }

    public void setSensorTracking(){
        trackingMode = TRACKING_MODE_SENSOR;
    }
}