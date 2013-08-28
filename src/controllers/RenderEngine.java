package controllers;

import gui.Gui;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import models.EdgeListNode;
import models.Polygon;
import models.Transform;
import models.Vector3D;

// TODO javadoc/commenting
// Change the lightsource to be adjustable (intensity) and handle multiple sources
// write report



public class RenderEngine {

	// TODO use an array set to no of lines - 1
	private ArrayList<Polygon> polygons;
	private Gui gui;
	private Vector3D lightSource;
	private Rectangle2D.Float bounds;
	private int width;
	private int height;
	private Color colourBuffer[][];
	private float zBuffer[][];
	private Color intensity = new Color(100,100,100);
	private Color ambience = new Color(200,200,200);
	private float zMin;
	private float zMax;
	private Transform translate;
	private Transform rotate;
	private Transform lightSourceRotate;
	private Transform scale;
	private String file;

	public RenderEngine(Gui gui, int width, int height){
		this.gui = gui;
		this.width = width;
		this.height = height;

		initialiseRenderer();
	}

	private void initialiseRenderer(){
		colourBuffer = new Color[width][height];
		zBuffer = new float[width][height];
		scale = null;
		translate = null;
		rotate = null;
	}

	public void openModel(String file) throws IOException{
		this.file = file;

		// Load lightsource and polygons
		BufferedReader input = new BufferedReader(new FileReader(file));
		lightSource = Vector3D.loadVector3D(input.readLine());
		polygons = loadPolygons(input);

		// Load polygons
		calculateBounds();

		// Transform and the polygons to make [0,0,0] the centre of the polygons
		float polyX = bounds.x + (bounds.width / 2);
		float polyY = bounds.y + (bounds.height / 2);
		float polyZ = zMin + ((zMax - zMin)/2);
		transformPolygons(Transform.newTranslation(0-polyX, 0-polyY, 0-polyZ));

		// Scale the polygons if they do not fit in the viewing area
		// Check to see that they don't already fit before scaling
		if(bounds.width > this.width || bounds.height > this.height){

			float widthScale = this.width / bounds.width;
			float heightScale = this.height / bounds.height;
			float scale = widthScale < heightScale ? widthScale : heightScale;

			transformPolygons(Transform.newScale(scale, scale, scale));
		}
	}

	public void resetModel() throws IOException{
		initialiseRenderer();
		openModel(this.file);
	}

	private void transformPolygons(Transform t) {
		for(Polygon p : polygons)
			p.applyTransform(t);
		calculateBounds();
	}

	public void rotate(float x, float y, float z){
		rotate = Transform.newYRotation((-2*x)/this.width);
		rotate = rotate.compose(Transform.newXRotation((2*y)/this.height));
		rotate = rotate.compose(Transform.newZRotation((-2*z)/this.width));
		draw();
	}
	
	public void rotateLightSource(int x, int y, float z) {
		lightSourceRotate = Transform.newYRotation((-4*x)/this.width);
		lightSourceRotate = lightSourceRotate.compose(Transform.newXRotation((4*y)/this.height));
		lightSourceRotate = lightSourceRotate.compose(Transform.newZRotation((-4*z)/this.width));
		draw();
	}

	public void translate(float x, float y, float z){
		translate = Transform.newTranslation(x, y, z);
		draw();
	}

	public void scale(float s){
		scale = Transform.newScale(s, s, s);
		draw();
	}

	public void applyTransform(){
		if(translate != null){
			transformPolygons(translate);
			translate = null;
			draw();
		}
		else if(rotate != null){
			transformPolygons(rotate);
			lightSource = rotate.multiply(lightSource);
			rotate = null;
			draw();
		}
		else if(lightSourceRotate != null){
			lightSource = lightSourceRotate.multiply(lightSource);
			lightSourceRotate = null;
			draw();
		}
	}

	/**
	 * Calculates the bounding box of all polygons
	 */
	private void calculateBounds(){

		float xMin = Float.MAX_VALUE;
		float yMin = Float.MAX_VALUE;
		float zMin = Float.MAX_VALUE;
		float xMax = -Float.MAX_VALUE;
		float yMax = -Float.MAX_VALUE;
		float zMax = -Float.MAX_VALUE;

		for(Polygon p : polygons){
			Rectangle2D.Float r = p.getBounds();
			if(r == null){System.out.println("Null bounds in polygon"); continue;}
			float rxMax = r.x + r.width;
			float ryMax = r.y + r.height;
			float zMi = p.getZMin();
			float zMa = p.getZMax();

			xMin = r.x < xMin ? r.x : xMin;
			yMin = r.y < yMin ? r.y : yMin;
			zMin = zMi < zMin ? zMi : zMin;
			xMax = rxMax > xMax ? rxMax : xMax;
			yMax = ryMax > yMax ? ryMax : yMax;
			zMax = zMa > zMax ? zMa : zMax;
		}

		this.zMin = zMin;
		this.zMax = zMax;
		bounds = new Rectangle2D.Float(xMin, yMin, xMax - xMin, yMax - yMin);
	}

	/**
	 * Loads polygons and returns an arraylist of those polygons. Input should
	 * be a buffered reader pointing to a set of lines corresponding to polygons
	 * with 9 floats for vertices and 3 more numbers for colour.
	 * @param input
	 * @return
	 * @throws IOException
	 */
	private ArrayList<Polygon> loadPolygons(BufferedReader input) throws IOException{
		ArrayList<Polygon> polys = new ArrayList<Polygon>();

		// Read the polygons
		String currentLine;
		while((currentLine = input.readLine()) != null)
			polys.add(Polygon.loadPolygon(currentLine));

		return polys;
	}

	/**
	 * Draws the polygons to the gui.
	 */
	public void draw(){
		initialiseZBuffer();
		generateZBuffer();
		gui.drawImage(getImage());
	}

	/**
	 * Converts the z buffer to a 2D image
	 * @return
	 */
	public BufferedImage getImage(){
		BufferedImage img = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < this.width; x++){
			for (int y = 0; y < this.height; y++){

				if(x >= this.width || x >= this.height) continue;
				img.setRGB(x, y, colourBuffer[x][y].getRGB());
			}
		}

		return img;
	}

	/**
	 * Populates the z buffer from polygons
	 */
	private void generateZBuffer(){
		Vector3D lightSourceCopy = lightSource.clone();
		if(rotate != null)	lightSourceCopy = rotate.multiply(lightSourceCopy);
		if(lightSourceRotate != null) lightSourceCopy = lightSourceRotate.multiply(lightSourceCopy);

		Transform t = Transform.identity();
		if(translate != null) t = t.compose(translate);
		if(rotate != null) t = t.compose(rotate);
		if(scale != null) t = t.compose(scale);


		for(Polygon pOriginal : polygons){
			Polygon p = pOriginal.clone();

			p.applyTransform(t);


			if(p.getNormal().z > 0) continue;

			int polyHeight = (int)Math.floor(p.getBounds().getMaxY());

			if((polyHeight+1+(this.height/2)) < 0) continue;
			EdgeListNode edgeList[] = new EdgeListNode[polyHeight+2+(this.height/2)]; //adjusts for centred image

			parseEdge(edgeList, p.vertex(0), p.vertex(1));
			parseEdge(edgeList, p.vertex(1), p.vertex(2));
			parseEdge(edgeList, p.vertex(2), p.vertex(0));

			drawToZBuffer(p.getShade(lightSourceCopy, intensity, ambience), edgeList);
		}


	}

	/**
	 * Interpolates between two vertices adding the values to an edgelist
	 * @param edgeList
	 * @param from
	 * @param to
	 */
	private void parseEdge(EdgeListNode[] edgeList, Vector3D from, Vector3D to){
		Vector3D a = from.y < to.y ? from : to;
		Vector3D b = a == from ? to : from;

		int i = (int)Math.floor(a.y)+(this.height/2);
		int maxi = (int)Math.floor(b.y)+(this.height/2);

		float mx = (b.x - a.x)/(b.y - a.y);
		float mz = (b.z - a.z)/(b.y - a.y);
		float x = a.x;
		float z = a.z;

		if(maxi < 0) return;
		while(i < maxi){
			if(i<0) {i++;x += mx;z += mz;continue;}
			// Initialise the item in the edge list if it does not exist
			if(edgeList[i] == null) edgeList[i] = new EdgeListNode();
			edgeList[i].putPoint((int)x+(this.width/2), z);
			i++;
			x += mx;
			z += mz;
		}

	}

	/**
	 * Refreshes the ZBuffer with empty values
	 */
	private void initialiseZBuffer(){
		for(int h = 0; h < this.height; h++){
			for(int w = 0; w < this.width; w++){
				colourBuffer[w][h] = Color.WHITE;
				zBuffer[w][h] = Integer.MAX_VALUE;
			}
		}
	}

	private void drawToZBuffer(Color colour, EdgeListNode[] edgeList){
		for(int y = 0; y < edgeList.length; y++){
			EdgeListNode node = edgeList[y];
			if(node == null) continue;

			int x = (int)node.leftX();
			float z = node.leftZ();
			float mz = ((node.rightZ() - node.leftZ()) / (node.rightX() - node.leftX()));

			while(x <= (int)Math.floor(node.rightX())){ //TODO round up?
				if(x < 0 || y < 0 || x >= width || y >= height) {x++;z += mz; continue;}

				if(z < zBuffer[x][y]){
					zBuffer[x][y] = z;
					colourBuffer[x][y] = colour;
				}
				x++;
				z += mz;
			}
		}
	}

}
