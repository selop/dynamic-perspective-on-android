package de.tud.lopatkin.masterproject.tracking;

import org.opencv.core.Point;
import org.opencv.video.KalmanFilter;

public class SimpleKalmanFiltering {

	public KalmanFilter initKalman(Point coord){
		
		KalmanFilter kF = new KalmanFilter(4,2,0,0);
		
		// DONE: usable in android version??

		// Unfortunately the java wrapper does not provide the kalman functions
		
		return kF;
	}
}
