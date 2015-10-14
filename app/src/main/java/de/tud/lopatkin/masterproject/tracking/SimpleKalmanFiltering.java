package de.tud.lopatkin.masterproject.tracking;

import org.opencv.core.Point;
import org.opencv.video.KalmanFilter;

public class SimpleKalmanFiltering {

	public KalmanFilter initKalman(Point coord){

		KalmanFilter kF = new KalmanFilter();

		// DONE: usable in android version??

		// Unfortunately the android wrapper does not provide the kalman functions
		
		return kF;
	}
}
