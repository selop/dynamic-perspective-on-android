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

    protected boolean showTracking = true;

    public void toggleShowTracking(){
        if(showTracking)
            showTracking = false;
        else
            showTracking = true;
    }
}
