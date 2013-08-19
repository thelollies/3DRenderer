package models;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Formatter;

public class Polygon {

	private boolean hidden;
	private Color reflectivity;
	private Color shade;
	private Vector3D normal;
	private Vector3D vertices[];
	private Rectangle bounds;

	public Polygon(Vector3D vertices[], Color reflectivity){
		assert(vertices.length == 3);
		assert(reflectivity != null);

		hidden = false;
		this.vertices = vertices;
		this.reflectivity = reflectivity;
		calculateNormal();
		bounds();
	}

	private void bounds(){
		float xMin = Float.MAX_VALUE;
		float yMin = Float.MAX_VALUE;
		float xMax = -Float.MAX_VALUE;
		float yMax = -Float.MAX_VALUE;

		for(int i = 0; i < vertices.length; i++){
			Vector3D v = vertices[i];
			xMin = v.x < xMin ? v.x : xMin;
			yMin = v.y < yMin ? v.y : yMin;
			xMax = v.x > xMax ? v.x : xMax;
			yMax = v.y > yMax ? v.y : yMax;
		}

		bounds = new Rectangle((int)xMin, (int)yMin, (int)(xMax - xMin), (int)(yMax - yMin));
	}

	public Rectangle getBounds(){
		return bounds;
	}

	private void calculateNormal(){
		Vector3D twoMinusOne = vertices[1].minus(vertices[0]);
		Vector3D threeMinusTwo = vertices[2].minus(vertices[1]);
		Vector3D crossProduct = twoMinusOne.crossProduct(threeMinusTwo);
		normal = crossProduct.unitVector();
	}

	/**@author Pondy
	 * toString for my Polygon class */
	public String toString(){
		StringBuilder ans = new StringBuilder("Poly:");
		Formatter f = new Formatter(ans);
		ans.append(hidden?'h':' ');
		for (int i=0; i<3; i++){
			f.format("(%8.3f,%8.3f,%8.3f)",
					vertices[i].x, vertices[i].y, vertices[i].z);
		}
		f.format("n:(%6.3f,%6.3f,%6.3f)",
				normal.x, normal.y, normal.z);
		f.format("c:(%3d-%3d-%3d)",
				reflectivity.getRed(), reflectivity.getGreen(),
				reflectivity.getBlue());
		bounds();
		if(bounds != null)
			f.format("b:(%3d %3d %3d %3d)",
					bounds.x, bounds.y, bounds.width, bounds.height);
		if (shade!=null) {
			f.format("s:(%3d-%3d-%3d)",
					shade.getRed(), shade.getGreen(), shade.getBlue());}
		f.close();
		return ans.toString();
	}

	/**
	 * Returns the polygon equivalent form a line with 9 floats followed by 4 ints.
	 * The 9 floats specify the 3d vectors of the polygon and the 4 ints specify
	 * the rgba reflectivity.
	 * @param line
	 * @return Polygon equivalent of line
	 */

	public static Polygon loadPolygon(String line){
		String polyValues[] = line.split(" ");
		Vector3D polyVertices[] = new Vector3D[3];

		/* Extract 3D vectors */
		for(int i = 0; i < 3; i++){
			int offset = i*3;
			float x = Float.parseFloat(polyValues[0+offset]);
			float y = Float.parseFloat(polyValues[1+offset]);
			float z = Float.parseFloat(polyValues[2+offset]);

			polyVertices[i] = new Vector3D(x, y, z);
		}

		// Extract Colour
		int r = Integer.parseInt(polyValues[9]);
		int g = Integer.parseInt(polyValues[10]);
		int b = Integer.parseInt(polyValues[11]);

		assert(r <= 255 && r >= 0 && g <= 255 && g>= 0 && b <= 255 && b >= 0);
		Color reflect = new Color(r, g, b);

		return new Polygon(polyVertices, reflect);
	}
}