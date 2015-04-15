package de.tud.lopatkin.masterproject.views;

import android.content.Context;
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
import org.rajawali3d.math.vector.Vector3;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import de.tud.lopatkin.masterproject.R;
import de.tud.lopatkin.masterproject.model.PlanesGalore;
import de.tud.lopatkin.masterproject.model.PlanesGaloreMaterialPlugin;

public class PlanesRenderer extends AbstractTrackingRenderer {

    public Context context;

    private Material mMaterial;
    private PlanesGaloreMaterialPlugin mMaterialPlugin;

	//private OffAxisPerspective mOffAxisPerspective;

	private long mStartTime;

	public PlanesRenderer(Context context) {
		super(context);
        this.context = context;
		setFrameRate(60);
	}

	protected void initScene() {
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

    public void onTouchEvent(MotionEvent event){}

    public void onOffsetsChanged(float x, float y, float z, float w, int i, int j){}

}
