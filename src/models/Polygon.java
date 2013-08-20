package models;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Formatter;

public class Polygon {

	private boolean hidden;
	private Color reflectivity;
	private Color shade;
	private Vector3D normal;
	private Vector3D vertices[];
	private Rectangle2D.Float bounds;
	private float height;

	public Polygon(Vector3D vertices[], Color reflectivity){
		assert(vertices.length == 3);
		assert(reflectivity != null);

		hidden = false;
		this.vertices = vertices;
		this.reflectivity = reflectivity;
		calculateNormal();
		bounds();
		height();
	}

	public Color getShade(Vector3D lightNormal, Color intensity, Color ambience){
		float costh = normal.dotProduct(lightNormal);

		// Colour component intensity
		float ir = intensity.getRed() / 255f;
		float ig = intensity.getGreen() / 255f;
		float ib = intensity.getBlue() / 255f;

		// Colour component ambience
		float ar = ambience.getRed() / 255f;
		float ag = ambience.getGreen() / 255f;
		float ab = ambience.getBlue() / 255f;

		int r = (int)((ar + (ir * costh)) * reflectivity.getRed());
		int g = (int)((ag + (ig * costh)) * reflectivity.getGreen());
		int b = (int)((ab + (ib * costh)) * reflectivity.getBlue());

		// Restrict colour components to range (0-255)
		r = r >= 0 ? r : 0;
		g = g >= 0 ? g : 0;
		b = b >= 0 ? b : 0;
		r = r <= 255 ? r : 255;
		g = g <= 255 ? g : 255;
		b = b <= 255 ? b : 255;

		shade = new Color(r, g, b);
		return shade;
	}

	public Vector3D getNormal(){
		return normal;
	}

	public void applyMatrix(Matrix4D matrix){
		for(int i = 0; i < vertices.length; i++){
			vertices[i] = matrix.multiplyByVector(vertices[i]);
		}

		bounds();
		calculateNormal();
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

		bounds = new Rectangle2D.Float(xMin, yMin, xMax - xMin, yMax - yMin);

	}

	public Rectangle2D.Float getBounds(){
		return bounds;
	}

	private void height(){
		float miny = Float.MAX_VALUE;
		float maxy = -Float.MAX_VALUE;

		for(int i = 0; i < vertices.length; i++){
			miny = vertices[i].y < miny ? vertices[i].y : miny;
			maxy = vertices[i].y > maxy ? vertices[i].y : maxy;
		}

		this.height =  maxy - miny;
	}

	public float getHeight(){
		return height;
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
			f.format("b:(%3f %3f %3f %3f)",
					bounds.x, bounds.y, bounds.width, bounds.height);
		if (shade!=null) {
			f.format("s:(%3d-%3d-%3d)",
					shade.getRed(), shade.getGreen(), shade.getBlue());}
		f.close();
		return ans.toString();
	}

	public Color getReflectivity(){
		return reflectivity;
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

	/**
	 * Gets the specified vertex from 0-2
	 * @param vertexNo
	 * @return
	 */
	public Vector3D vertex(int vertexNo){
		assert(vertexNo >= 0 && vertexNo <= 2);
		if(vertexNo < 0 || vertexNo > 2) return null;
		return vertices[vertexNo];
	}
}
