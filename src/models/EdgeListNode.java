package models;

public class EdgeListNode {
	private float lx;
	private float lz;
	private float rx;
	private float rz;

	public EdgeListNode(){
		lx = Float.MAX_VALUE;
		lz = Float.MAX_VALUE;
		rx = -Float.MAX_VALUE;
		rz = Float.MAX_VALUE;
	}

	public void putPoint(float x, float z){
		if(x < lx) {lx = x; lz = z;}
		if(x > rx) {rx = x; rz = z;}
	}
	
	public float leftX(){
		return lx;
	}
	
	public float leftZ(){
		return lz;
	}
	
	public float rightX(){
		return rx;
	}
	
	public float rightZ(){
		return rz;
	}

}
