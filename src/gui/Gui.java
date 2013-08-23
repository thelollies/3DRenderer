package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import controllers.RenderEngine;

public class Gui extends JFrame{

	private DrawPanel drawPanel;
	private RenderEngine renderEngine;
	private Dimension panelSize = new Dimension(600,600);
	public Gui(String fileName){

		setLayout(new BorderLayout());
		add(interactPanel(), BorderLayout.NORTH);
		add(createRenderPanel(), BorderLayout.WEST);

		// Start the render engine
		try{
			renderEngine = new RenderEngine(this, new FileReader(fileName), panelSize.width, panelSize.height);
			renderEngine.draw();
		}catch(FileNotFoundException e){
			System.out.printf("File: %s not found, exiting...", fileName);
			System.exit(0);
		}

		this.setFocusable(true);
		this.addKeyListener(new RotateListener());

		pack();
		setVisible(true);
	}



	private JPanel interactPanel(){
		JPanel interactPanel = new JPanel();
		interactPanel.setLayout(new BoxLayout(interactPanel, BoxLayout.X_AXIS));

		JButton rotateLeft = new JButton("Rotate Left");
		JButton rotateRight = new JButton("Rotate Right");

		rotateLeft.addActionListener(new ActionListener(){
			@Override	public void actionPerformed(ActionEvent e) {
				renderEngine.rotateOnY(0.1f);
			}});
		rotateRight.addActionListener(new ActionListener(){
			@Override	public void actionPerformed(ActionEvent e) {
				renderEngine.rotateOnY(-0.1f);
			}});

		interactPanel.add(rotateLeft);
		interactPanel.add(rotateRight);

		return interactPanel;
	}

	private DrawPanel createRenderPanel(){
		drawPanel = new DrawPanel(panelSize);
		return drawPanel;
	}

	public void drawImage(BufferedImage img){
		drawPanel.drawImage(img);
	}

	public static void main(String args[]){
		new Gui("shapes.txt");
	}

	private class RotateListener implements KeyListener{

		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_LEFT){
				renderEngine.rotateOnY(0.1f);
			}
			else if(e.getKeyCode() == KeyEvent.VK_RIGHT){
				renderEngine.rotateOnY(-0.1f);
			}
			else if(e.getKeyCode() == KeyEvent.VK_UP){
				renderEngine.rotateOnX(-0.1f);
			}
			else if(e.getKeyCode() == KeyEvent.VK_DOWN){
				renderEngine.rotateOnX(0.1f);
			}
		}

		@Override	public void keyReleased(KeyEvent e) {}
		@Override	public void keyTyped(KeyEvent e) {}

	}

}
