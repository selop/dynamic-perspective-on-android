package de.tud.lopatkin.masterproject.views;

import android.content.Context;
import android.view.MotionEvent;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.util.ObjectColorPicker;
import org.rajawali3d.util.OnObjectPickedListener;

import de.tud.lopatkin.masterproject.R;

/**
 * This class displays a room with monkeys to test object picking.
 */
public class CubeRoomRenderer extends AbstractTrackingRenderer implements OnObjectPickedListener {
    /**
     * The tag for logging.
     */
    private static final String TAG = "CubeRoomRenderer";
    private PointLight mLight;
    private Object3D back,side1,side2,bottom,top;
    private Object3D mObjectGroup;
    private Animation3D mCameraAnim;

    public CubeRoomRenderer(Context context) {
        super(context);
        setFrameRate(60);
    }

    protected void initScene() {

        mLight = new PointLight();
        mLight.setPosition(-2, 1, 4);
        mLight.setPower(0.5f);

        mPicker = new ObjectColorPicker(this);
        mPicker.setOnObjectPickedListener(this);
        getCurrentScene().addLight(mLight);
        getCurrentCamera().setZ(15);

        // TODO: DS parsing may be very time consuming, async loading would be good here

        LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.industryrobot_obj);
        try {
            objParser.parse();
            mObjectGroup = objParser.getParsedObject();
            mObjectGroup.setScale(0.0025f);
            mObjectGroup.setY(-2);
            mPicker.registerObject(mObjectGroup);
            getCurrentScene().addChild(mObjectGroup);

            mCameraAnim = new RotateOnAxisAnimation(Vector3.Axis.Y, 360);
            mCameraAnim.setDurationMilliseconds(8000);
            mCameraAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
            mCameraAnim.setTransformable3D(mObjectGroup);
        } catch (ParsingException e) {
            e.printStackTrace();
        }

        Material material = new Material();
        material.enableLighting(true);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());

        //planeObjects(material);

        mObjectGroup.setMaterial(material);
        mObjectGroup.setColor(0x3F3F3F);
        getCurrentScene().registerAnimation(mCameraAnim);
        mCameraAnim.play();

    }

    @Override
    public void onRender(final long elapsedTime, final double deltaTime) {
        super.onRender(elapsedTime, deltaTime);

        // -- set the background color to be transparent
        // you need to have called setGLBackgroundTransparent(true);
        // in the activity for this to work.
        if(showTracking)
            getCurrentScene().setBackgroundColor(0, 0, 0, 0);
        else
            getCurrentScene().setBackgroundColor(0, 0, 0, 255);
    }

    public void onOffsetsChanged(float x, float y, float z, float w, int i, int j){}

    @Override
    public void onTouchEvent(MotionEvent event){
    }

    private void planeObjects(Material material){
        back = new Plane(10,10,1,1);
        back.setPosition(0,0, -10);
        back.setDoubleSided(true);

        getCurrentScene().addChild(back);

        side1 = new Plane(10,10,1,1);
        side1.setDoubleSided(true);
        side1.setPosition(-5,0,-5);
        side1.setRotY(270);
        mPicker.registerObject(side1);
        getCurrentScene().addChild(side1);

        side2 = new Plane(10,10,1,1);
        side2.setDoubleSided(true);
        side2.setPosition(5,0, -5);
        side2.setRotY(90);
        mPicker.registerObject(side2);
        getCurrentScene().addChild(side2);

        bottom = new Plane(10,10,1,1);
        bottom.setDoubleSided(true);
        bottom.setPosition(0,-5,-5);
        bottom.setRotX(90);
        mPicker.registerObject(bottom);
        getCurrentScene().addChild(bottom);

        top = new Plane(10,10,1,1);
        top.setDoubleSided(true);
        top.setPosition(0,5,-5);
        top.setRotX(270);
        mPicker.registerObject(top);
        getCurrentScene().addChild(top);

        back.setMaterial(material);
        back.setColor(0x663333);
        bottom.setMaterial(material);
        bottom.setColor(0x336633);
        top.setMaterial(material);
        top.setColor(0x333366);
        side1.setMaterial(material);
        side1.setColor(0x333333);
        side2.setMaterial(material);
        side2.setColor(0x333333);
    }


}