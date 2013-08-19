package utility;
/* Code for COMP261 Assignment
 */

import java.io.File;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.*;
import java.awt.Graphics;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;


/** Code that you may choose to use if you wish.*/

public class ImageSample{

    private int imageWidth = 800;
    private int imageHeight = 800;

    private JFrame frame;
    private BufferedImage image;
    private JComponent drawing;
    private JTextArea textOutput;

    private float shift = 1.0f;


    public static void main(String[] args){
	new ImageSample();
    }
    private ImageSample(){
	setupFrame();
	textOutput.setText("Click Render to make an image\n Use left and right arrows to shift the image\n");
    }

    private void createImage(){
	Color[][] bitmap = new Color[imageWidth][imageHeight];

	//make the bitmap of smoothly changing colors;
	//Your program should render a model
	for (int x=0; x<imageWidth; x++){
	    for (int y=0; y<imageHeight; y++){
		float hue = (float)Math.sin((x+Math.pow(y,shift))/(imageWidth));
		bitmap[x][y] = Color.getHSBColor(hue, 1.0f, 1.0f);
	    }
	}

	// render the bitmap to the image so it can be displayed (and saved)
	convertBitmapToImage(bitmap);

	// draw it.
	drawing.repaint();
    }


    /** Converts a 2D array of Colors to a BufferedImage.
        Assumes that bitmap is indexed by column then row and has
	imageHeight rows and imageWidth columns.
	Note that image.setRGB requires x (col) and y (row) are given in that order.
    */
    private BufferedImage convertBitmapToImage(Color[][] bitmap) {
	image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
	for (int x = 0; x < imageWidth; x++) {
	    for (int y = 0; y < imageHeight; y++) {
		image.setRGB(x, y, bitmap[x][y].getRGB());
	    }
	}
	return image;
    }

    /** writes the current image to a file of the specified name
    */
    private void saveImage(String fname){
	try {ImageIO.write(image, "png", new File(fname));}
	catch(IOException e){System.out.println("Image saving failed: "+e);}
    }



    /** Creates a frame with a JComponent in it.
     *  Clicking in the frame will close it. */
    private void setupFrame(){
	frame = new JFrame("Graphics Example");
	frame.setSize(imageWidth+10, imageHeight+20);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	drawing = new JComponent(){
		protected void paintComponent(Graphics g){
		    g.drawImage(image, 0, 0, null);}
	    };
	frame.add(drawing, BorderLayout.CENTER);

	JPanel panel = new JPanel();
	frame.add(panel, BorderLayout.NORTH);
	addButton("Render", panel, new ActionListener(){
		public void actionPerformed(ActionEvent ev){createImage();}});
	addButton("Save", panel, new ActionListener(){
		public void actionPerformed(ActionEvent ev){saveImage("TestImage.png");}});
	addButton("Quit", panel, new ActionListener(){
		public void actionPerformed(ActionEvent ev){System.exit(0);}});

	// make the arrow keys shift the image
	InputMap iMap = drawing.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
	ActionMap aMap= drawing.getActionMap();
	iMap.put(KeyStroke.getKeyStroke("LEFT"), "shiftLeft");
	iMap.put(KeyStroke.getKeyStroke("RIGHT"), "shiftRight");
	aMap.put("shiftLeft", new AbstractAction(){
		public void actionPerformed(ActionEvent e){
		    shift+=.1;
		    createImage();}});
	aMap.put("shiftRight", new AbstractAction(){
		public void actionPerformed(ActionEvent e){
		    shift-=.1;
		    createImage();}});


	textOutput = new JTextArea(4, 100);
	textOutput.setEditable(false);
	JScrollPane textSP = new JScrollPane(textOutput);
	frame.add(textSP, BorderLayout.SOUTH);


	frame.setVisible(true);
    }

    private void addButton(String name, JComponent comp, ActionListener listener){
	JButton button = new JButton(name);
	comp.add(button);
	button.addActionListener(listener);
    }




}





