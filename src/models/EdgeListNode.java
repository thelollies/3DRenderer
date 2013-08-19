package models;

public class EdgeListNode {
	private int lx;
	private int lz;
	private int rx;
	private int rz;

	public EdgeListNode(){
		lx = Integer.MAX_VALUE;
		lz = Integer.MAX_VALUE;
		rx = -Integer.MAX_VALUE;
		rz = Integer.MAX_VALUE;
	}

	public void putPoint(int x, int z){
		if(x < lx) {lx = x; lz = z;}
		if(x > rx) {rx = x; rz = z;}
	}

}
