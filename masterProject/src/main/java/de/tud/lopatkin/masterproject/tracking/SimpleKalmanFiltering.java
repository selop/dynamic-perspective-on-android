package de.tud.lopatkin.masterproject.tracking;

import org.opencv.core.Point;
import org.opencv.video.KalmanFilter;

public class SimpleKalmanFiltering {

	public KalmanFilter initKalman(Point coord){
		
		KalmanFilter kF = new KalmanFilter(4,2,0,0);
		
		// TODO: usable in android version??
		
		
		return kF;
	}
}
