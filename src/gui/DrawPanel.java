package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.beans.Transient;
import java.io.File;

import javax.swing.JPanel;

public class DrawPanel extends JPanel{

	private Dimension size = new Dimension(500,500);

	public DrawPanel(){
		super();
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(Color.red);
		g.drawRect(5, 5, 80, 80);
	}

	@Override
	@Transient
	public Dimension getPreferredSize() {
		return size;
	}

}
