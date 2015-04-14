package de.tud.lopatkin.masterproject.views;

import android.content.Context;

import org.rajawali3d.renderer.RajawaliRenderer;


/**
 * Created by selop on 12/04/15.
 */
public abstract class AbstractTrackingRenderer extends RajawaliRenderer {

    public AbstractTrackingRenderer(Context context){
        super(context);
    }

    boolean showTracking = true;

    public abstract boolean isShowTracking();

    public abstract void setShowTracking(boolean showTracking);
}
