package de.tud.lopatkin.masterproject.views;

import android.content.Context;
import android.view.MotionEvent;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.util.ObjectColorPicker;
import org.rajawali3d.util.OnObjectPickedListener;

/**
 * This class renderes a room with monkeys to test object picking.
 */
public class CubeRoomRenderer extends AbstractTrackingRenderer implements OnObjectPickedListener {

    private PointLight mLight;
    private Object3D back,side1,side2,bottom,top;
    private ObjectColorPicker mPicker;

    public CubeRoomRenderer(Context context) {
        super(context);
        setFrameRate(60);
    }

    protected void initScene() {
        try {
            mPicker = new ObjectColorPicker(this);
            mPicker.setOnObjectPickedListener(this);
            mLight = new PointLight();
            mLight.setPosition(-2, 1, 4);
            mLight.setPower(2.5f);
            getCurrentCamera().setPosition(0, 0, 10);

            mPicker = new ObjectColorPicker(this);
            mPicker.setOnObjectPickedListener(this);
            mLight = new PointLight();
            mLight.setPosition(-2, 1, 4);
            mLight.setPower(1.5f);
            getCurrentScene().addLight(mLight);
            getCurrentCamera().setPosition(0, 0, 7);

            Material material = new Material();
            material.enableLighting(true);
            material.setDiffuseMethod(new DiffuseMethod.Lambert());

            back = new Plane(10,10,1,1);
            back.setPosition(0,0, -10);
            getCurrentScene().addChild(back);

            side1 = new Plane(10,10,1,1);
            side1.setPosition(-5,0,-5);
            side1.setRotY(270);
            getCurrentScene().addChild(side1);

            side2 = new Plane(10,10,1,1);
            side2.setPosition(5,0, -5);
            side2.setRotY(90);
            getCurrentScene().addChild(side2);

            bottom = new Plane(10,10,1,1);
            bottom.setPosition(0,-5,-5);
            bottom.setRotX(90);
            getCurrentScene().addChild(bottom);

            top = new Plane(10,10,1,1);
            top.setPosition(0,5,-5);
            top.setRotX(270);
            getCurrentScene().addChild(top);

            back.setMaterial(material);
            back.setColor(0xc4c4c4);
            bottom.setMaterial(material);
            bottom.setColor(0xc4c4c4);
            top.setMaterial(material);
            top.setColor(0xc4c4c4);
            side1.setMaterial(material);
            side1.setColor(0xc4c4c4);
            side2.setMaterial(material);
            side2.setColor(0xc4c4c4);
        } catch(Exception e) {
            e.printStackTrace();
        }
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

    public void onTouchEvent(MotionEvent event){}

    public void onOffsetsChanged(float x, float y, float z, float w, int i, int j){}

    public void getObjectAt(float x, float y) {
        mPicker.getObjectAt(x, y);
    }

    @Override
    public void onObjectPicked(Object3D object) {
        object.setZ(object.getZ() == 0 ? -2 : 0);
    }
}