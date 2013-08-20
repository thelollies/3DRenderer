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
import models.Matrix4D;
import models.Polygon;
import models.Vector3D;

public class RenderEngine {

	// TODO use an array set to no of lines - 1
	private ArrayList<Polygon> polygons;
	private Gui gui;
	private Vector3D lightSource;
	private Rectangle2D.Float bounds;
	private int width = 500;
	private int height = 500;
	private Color colourBuffer[][] = new Color[width][height];
	private float zBuffer[][] = new float[width][height];
	private Color intensity = new Color(100,100,100);
	private Color ambience = new Color(255,255,255);

	public RenderEngine(Gui gui, FileReader file){
		this.gui = gui;

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

		// Scale if bigger than view area
		scaleDown();
		transformToCenter();

		// Transform to center
		System.out.printf("Bounds: %s\n", bounds.toString());
	}

	public void rotateOnY(float angleY){
		for(Polygon p : polygons){
			p.applyMatrix(Matrix4D.yRotateMatrix(angleY));
		}
		calculateBounds();
		draw();
	}

	private void scaleDown(){
		// If it doesn't need to be scaled, don't scale;
		if(bounds.width <= this.width && bounds.height <= this.height) return;
	}

	private void transformToCenter(){
		// If it is already centred, don't do anything
		if(width/2 == (int)bounds.getCenterX() && height/2 == (int)bounds.getCenterY())return;

		float transformX = (int)((width / 2) - bounds.getCenterX());
		float transformY = (int)((height / 2) - bounds.getCenterY());

		// TODO apply the transformation
	}

	/**
	 * Calculates the bounding box of all polygons
	 */
	private void calculateBounds(){

		float xMin = Integer.MAX_VALUE;
		float yMin = Integer.MAX_VALUE;
		float xMax = -Integer.MAX_VALUE;
		float yMax = -Integer.MAX_VALUE;

		for(Polygon p : polygons){
			Rectangle2D.Float r = p.getBounds();
			if(r == null){System.out.println("Null bounds in polygon"); continue;}
			float rxMax = r.x + r.width;
			float ryMax = r.y + r.height;

			xMin = r.x < xMin ? r.x : xMin;
			yMin = r.y < yMin ? r.y : yMin;
			xMax = rxMax > xMax ? rxMax : xMax;
			yMax = ryMax > yMax ? ryMax : yMax;
		}

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
				polygons.add(Polygon.loadPolygon(currentLine));
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
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				img.setRGB(x, y, colourBuffer[x][y].getRGB());
			}
		}
		return img;
	}

	private void generateZBuffer(){

		for(Polygon p : polygons){
			if(p.getNormal().z > 0) continue;

			int polyHeight = (int)Math.floor(p.getBounds().getMaxY());

			EdgeListNode edgeList[] = new EdgeListNode[polyHeight+1];

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

		int i = (int)Math.floor(a.y);
		int maxi = (int)Math.floor(b.y);

		float mx = (b.x - a.x)/(b.y - a.y);
		float mz = (b.z - a.z)/(b.y - a.y);
		float x = a.x;
		float z = a.z;

		while(i < maxi){
			// Initialise the item in the edge list if it does not exist
			if(edgeList[i] == null) edgeList[i] = new EdgeListNode();
			edgeList[i].putPoint(x, z);
			i++;
			x += mx;
			z += mz;
		}
		if(edgeList[maxi] == null) edgeList[maxi] = new EdgeListNode();
		edgeList[maxi].putPoint(x, z);
	}

	private void initialiseZBuffer(){
		for(int h = 0; h < this.height; h++){
			for(int w = 0; w < this.width; w++){
				colourBuffer[w][h] = Color.GRAY;
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
