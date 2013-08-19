package gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class DrawPanel extends JPanel{

	private Dimension size = new Dimension(500,500);
	private BufferedImage img;

	public DrawPanel(){
		super();
	}

	public void drawImage(BufferedImage img){
		this.img = img;
		repaint();
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if(img != null) g.drawImage(img, 0, 0, null);
	}

	@Override
	public Dimension getPreferredSize() {
		return size;
	}

}
