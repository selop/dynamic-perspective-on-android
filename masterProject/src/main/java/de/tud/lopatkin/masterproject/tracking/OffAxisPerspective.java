package de.tud.lopatkin.masterproject.tracking;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;

public class OffAxisPerspective {
	
	private float cx,cy;
	
	private Matrix4 proj, view;
	
	
	public void calculateOffAxisProjection(Camera cam){
		Vector3 pa,pb,pc, pe;
		double near, far;
		
		near = cam.getNearPlane();
		far = cam.getFarPlane();
		
		//-- space corners coordinates
	    pa = new Vector3(-cx,-cy,0);
	    pb = new Vector3( cx,-cy,0);
	    pc = new Vector3(-cx, cy,0);
	    pe = cam.getPosition();
	    
	    //-- Compute an orthonormal basis for the screen.
        Vector3 Vr = pb.subtract(pa);
	    Vr.normalize();
        Vector3 Vu = pc.subtract(pa);
	    Vu.normalize();
        Vector3 Vn = Vr.cross(Vu);
	    Vn.normalize();
	    
	    //-- Compute the screen corner vectors.
        Vector3 Va = pa.subtract(pe);
        Vector3 Vb = pb.subtract(pe);
        Vector3 Vc = pc.subtract(pe);
	    
	    //-- Find the distance from the eye to screen plane.
	    double d = Va.dot(Vn);
	    
	    //-- Find the extent of the perpendicular projection.
        double left   = Vr.dot(Va) * near / d;
        double right  = Vr.dot(Vb) * near / d;
        double bottom = Vu.dot(Va) * near / d;
        double top    = Vu.dot(Vc) * near / d;
	    
	    //-- Load the perpendicular projection.	    
        double[] perp =
			{(2*near)/(right-left), 0, (right+left)/(right-left), 0,
				0, 2*near/(top-bottom), (top+bottom)/(top-bottom), 0,
				0,0,-((far+near)/(near-far)), -(2*far*near/(near-far)),
				0,0,-1,0
			};
	    //Matrix.frustumM(cam.getProjectionMatrix(), 0, left, right, bottom, top, near, far);
	    //-- Rotate the projection to be non-perpendicular.
	    //-- Move the apex of the frustum to the origin.
        double[] trans =
	 		   {Vr.x, Vr.y, Vr.z, -pe.x,
	 			Vu.x, Vu.y, Vu.z, -pe.y,
	 			Vn.x, Vn.y, Vn.z, -pe.z,
	 			0f,    0f,    0f, 1f};


	 	//Matrix.setLookAtM(trans, 0, cam.getX(), cam.getY(), cam.getZ(), 0, 0, 0, 0, 1, 0);
	}
	
	private float PxToCm(int size){
		float pixelNumberPerCentimeter = 8.33f;
		return (float) size/ pixelNumberPerCentimeter;
		
	}


	public float getCx() {
		return cx;
	}


	public void setCx(int width) {
		this.cx = PxToCm(width);
	}


	public float getCy() {
		return cy;
	}


	public void setCy(int height) {
		this.cy = PxToCm(height);
	}


	public Matrix4 getProj() {
		return proj;
	}


	public void setProj(Matrix4 proj) {
		this.proj = proj;
	}


	public Matrix4 getView() {
		return view;
	}


	public void setView(Matrix4 view) {
		this.view = view;
	}
	
	
}
