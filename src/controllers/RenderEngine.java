package controllers;

import gui.Gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import models.EdgeListNode;
import models.Polygon;
import models.Vector3D;

// TODO javadoc/commenting
// TODO make the rotate work as described not by rotating all
// the polygons (e.g. rotate the camera and draw relative to it rather than the polys
// themselves as this means the lightsource cannot be observed)

// Change the lightsource to be adjustable
// Fix the zbuffer error (HINT: only happens when the poly has a horizontal edge)
// Optimisations (e.g. make polygons an  arry of polygons rather than arraylist)
// Do mouse drag rotations (and use ctrl to allow z rotation too)
// have shortcut for translations and rotations using the mouse plus allow
// a reset position, reset scale buttons
// fix buttons to avoid them holding focus (tell them to relinquish focus once they're done or
// set allowfocus to false)


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
	private Color ambience = new Color(255,255,255);
	private float zMin;
	private float zMax;

	public RenderEngine(Gui gui, FileReader file, int width, int height){
		this.gui = gui;
		this.width = width;
		this.height = height;
		colourBuffer = new Color[width][height];
		zBuffer = new float[width][height];

		System.out.println(this.width + " " + this.height);

		// Text file reader
		BufferedReader input = new BufferedReader(file);

		// Get the lightsource
		try{
			lightSource = Vector3D.loadVector3D(input.readLine()).unitVector();
		}
		catch(IOException e){e.printStackTrace();System.exit(0);}

		// Load polygons
		loadPolygons(input);
		calculateBounds();
		transformPolygons(getCentreTranslation());

		// Scale if bigger than view area
		scaleDown();

		// Transform to center
		System.out.printf("Bounds: %s\n", bounds.toString());
	}

	private void transformPolygons(Transform t) {
		for(Polygon p : polygons)
			p.applyTransform(t);
		calculateBounds();
	}

	private Transform getCentreTranslation() {
		float polyX = bounds.x + (bounds.width / 2);
		float polyY = bounds.y + (bounds.height / 2);
		float polyZ = zMin + ((zMax - zMin)/2);

		float desiredX = 0;
		float desiredY = 0;
		float desiredZ = 0;

		return Transform.newTranslation(0-polyX, 0-polyY, 0-polyZ);
	}

	public void rotateOnY(float angleY){
		transformPolygons(Transform.newYRotation(angleY));
		draw();
	}

	public void rotateOnX(float angleY){
		transformPolygons(Transform.newXRotation(angleY));
		draw();
	}

	public void rotateOnZ(float angleY){
		transformPolygons(Transform.newZRotation(angleY));

		draw();
	}

	private void scaleDown(){
		// If it doesn't need to be scaled, don't scale;
		if(bounds.width <= this.width && bounds.height <= this.height) return;
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
	 *
	 * @param input
	 */

	private void loadPolygons(BufferedReader input){
		polygons = new ArrayList<Polygon>();

		// Read the polygons
		String currentLine;
		try{
			while((currentLine = input.readLine()) != null){
				Polygon p = Polygon.loadPolygon(currentLine);
				polygons.add(p);
			}
		}catch(IOException e){e.printStackTrace();}
	}

	public void draw(){
		initialiseZBuffer();
		generateZBuffer();
		gui.drawImage(getImage());
	}

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

	private void generateZBuffer(){

		for(Polygon p : polygons){
			if(p.getNormal().z > 0) continue;

			int polyHeight = (int)Math.floor(p.getBounds().getMaxY());

			if((polyHeight+1+(this.height/2)) < 0) continue;
			EdgeListNode edgeList[] = new EdgeListNode[polyHeight+1+(this.height/2)]; //TODO;

			parseEdge(edgeList, p.vertex(0), p.vertex(1));
			parseEdge(edgeList, p.vertex(1), p.vertex(2));
			parseEdge(edgeList, p.vertex(2), p.vertex(0));

			long start = System.currentTimeMillis();
			drawToZBuffer(p.getShade(lightSource, intensity, ambience), edgeList);
			if(System.currentTimeMillis() - start > 10){
				System.out.println(System.currentTimeMillis() - start);
			}
		}


	}

	private void parseEdge(EdgeListNode[] edgeList, Vector3D from, Vector3D to){
		Vector3D a = from.y < to.y ? from : to;
		Vector3D b = a == from ? to : from;

		int i = (int)Math.floor(a.y)+(this.height/2);
		int maxi = (int)Math.floor(b.y)+(this.height/2);

		float mx = (b.x - a.x)/(b.y - a.y);
		float mz = (b.z - a.z)/(b.y - a.y);
		float x = a.x;
		float z = a.z;

		while(i < maxi){
			if(i<0) {i++;x += mx;z += mz;continue;}
			// Initialise the item in the edge list if it does not exist
			if(edgeList[i] == null) edgeList[i] = new EdgeListNode();
			edgeList[i].putPoint(x+(this.width/2), z);
			i++;
			x += mx;
			z += mz;
		}
		if(maxi < 0) return;

		if(edgeList[maxi] == null) edgeList[maxi] = new EdgeListNode();
		edgeList[maxi].putPoint(x+(this.width/2), z);
	}

	private void initialiseZBuffer(){
		for(int h = 0; h < this.height; h++){
			for(int w = 0; w < this.width; w++){
				colourBuffer[w][h] = Color.BLACK;
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
