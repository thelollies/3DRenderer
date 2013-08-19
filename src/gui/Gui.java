package gui;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.swing.JFrame;

import controllers.RenderEngine;

public class Gui extends JFrame{
	private DrawPanel drawPanel;
	private RenderEngine renderEngine;
	public Gui(String fileName){
		add(createRenderPanel());

		// Start the render engine
		try{
			renderEngine = new RenderEngine(this, new FileReader(fileName));
			renderEngine.draw();
		}catch(FileNotFoundException e){
			System.out.printf("File: %s not found, exiting...", fileName);
			System.exit(0);
		}

		pack();
		setVisible(true);
	}

	private DrawPanel createRenderPanel(){
		drawPanel = new DrawPanel();
		return drawPanel;
	}
	
	public void drawImage(BufferedImage img){
		drawPanel.drawImage(img);
	}

	public static void main(String args[]){
		new Gui("ball.txt");
	}
}
