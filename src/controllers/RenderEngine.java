package controllers;

import gui.Gui;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import models.EdgeListNode;
import models.Polygon;
import models.Vector3D;

public class RenderEngine {

	// TODO use an array set to no of lines - 1
	private ArrayList<Polygon> polygons;
	private Gui gui;
	private Vector3D lightSource;
	private Rectangle bounds;

	public RenderEngine(Gui gui, FileReader file){
		this.gui = gui;

		// Text file reader
		BufferedReader input = new BufferedReader(file);

		// Get the lightsource
		try{
			lightSource = Vector3D.loadVector3D(input.readLine());
		}
		catch(IOException e){e.printStackTrace();}

		// Load polygons
		loadPolygons(input);
		calculateBounds();
	}

	/**
	 * Calculates the bounding box of all polygons
	 */
	private void calculateBounds(){

		int xMin = Integer.MAX_VALUE;
		int yMin = Integer.MAX_VALUE;
		int xMax = -Integer.MAX_VALUE;
		int yMax = -Integer.MAX_VALUE;

		for(Polygon p : polygons){
			Rectangle r = p.getBounds();
			if(r == null){System.out.println("Null bounds in polygon"); continue;}
			int rxMax = r.x + r.width;
			int ryMax = r.y + r.height;

			xMin = r.x < xMin ? r.x : xMin;
			yMin = r.y < yMin ? r.y : yMin;
			xMax = rxMax > xMax ? rxMax : xMax;
			yMax = ryMax > yMax ? ryMax : yMax;
		}

		bounds = new Rectangle(xMin, yMin, xMax - xMin, yMax - yMin);
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
		generateEdgeLists();
	}

	public void generateEdgeLists(){
		for(Polygon p : polygons){
			// TODO make the edgelist
			Rectangle polyBounds = p.getBounds();
			int minY = polyBounds.y;
			int maxY = polyBounds.y + polyBounds.height;

			EdgeListNode edgeList[] = new EdgeListNode[maxY - minY];
			// for each edge
				//blablabla TODO http://ecs.victoria.ac.nz/foswiki/pub/Courses/COMP261_2013T2/LectureSchedule/13-4up.pdf

		}
	}
}
