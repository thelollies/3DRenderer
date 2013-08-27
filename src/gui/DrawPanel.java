package gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import javax.swing.Action;
import javax.swing.JPanel;

public class DrawPanel extends JPanel{

	private Dimension size;
	private BufferedImage img;

	public DrawPanel(Dimension size){
		super();
		this.size = size;
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
