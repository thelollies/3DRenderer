package gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import controllers.RenderEngine;

public class Gui extends JFrame{

	private DrawPanel drawPanel;
	private RenderEngine renderEngine;
	private Dimension panelSize = new Dimension(700,400);
	private JRadioManager radioManager;
	private JRadioButton[] buttons;
	private JSlider slider;
	private JSlider intensitySlider;
	private JSlider ambienceSlider;
	public Gui(String fileName){

		setLayout(new BorderLayout());
		add(createTopPanel(), BorderLayout.NORTH);
		add(createRenderPanel(), BorderLayout.WEST);
		setJMenuBar(createMenu());

		setFocusable(true);
		addListeners();

		pack();
		setVisible(true);
	}

	private JMenuBar createMenu() {
		JMenuBar menu = new JMenuBar();
		JMenu file = new JMenu("File");

		JMenuItem fileOpen = new JMenuItem("Open");
		JMenuItem fileExit = new JMenuItem("Exit");

		fileOpen.setMnemonic(KeyEvent.VK_O);
		fileExit.setMnemonic(KeyEvent.VK_E);

		fileOpen.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// show open file dialogue then if it is a good file 
				// kick off the renderer otherwise show a popup and back to waiting
				JFileChooser open = new JFileChooser();
				int choice = open.showDialog(Gui.this, "Open");
				if(choice == JFileChooser.APPROVE_OPTION){
					try{
						startRenderer(open.getSelectedFile());
					}
					catch(IOException e){
						JOptionPane.showConfirmDialog(Gui.this, "Incompatible File");
					}
				}
			}
		});
		fileExit.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		file.add(fileOpen);
		file.add(fileExit);

		menu.add(file);

		return menu;
	}

	private void startRenderer(File file) throws IOException {
		// Start the render engine
		slider.setValue(slider.getMaximum() / 2);
		renderEngine = new RenderEngine(this, panelSize.width, panelSize.height);
		renderEngine.openModel(file.getAbsolutePath());
		renderEngine.draw();
	}

	private void addListeners() {
		MouseDragHandler dragHandler = new MouseDragHandler(drawPanel);
		drawPanel.addMouseMotionListener(dragHandler);
		drawPanel.addMouseListener(new MouseClickHandler(dragHandler));
	}

	/**
	 * The returned panel contains a set of controls for specifying selection
	 * modes on the draw panel.
	 * @return
	 */
	private JPanel createTopPanel(){
		JPanel topPanel = new JPanel(new GridBagLayout());

		// Create the panel of selection modes
		JPanel selectionPanel = new JPanel();
		selectionPanel.setLayout(new BoxLayout(selectionPanel, BoxLayout.Y_AXIS));
		selectionPanel.setBorder(BorderFactory.createTitledBorder("Selection Modes"));
		selectionPanel.setPreferredSize(new Dimension(200,85));

		JRadioButton translate = new JRadioButton("Translate");
		JRadioButton rotate = new JRadioButton("Rotate");

		buttons = new JRadioButton[]{translate, rotate};
		radioManager = new JRadioManager(buttons);

		for(JRadioButton button : buttons)
			selectionPanel.add(button);

		// Create the scale slider panel
		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.Y_AXIS));
		sliderPanel.setBorder(BorderFactory.createEmptyBorder(10,5,5,5));

		JLabel sliderLabel = new JLabel("Scale: ");
		slider = new JSlider(0,50,25);
		slider.setPreferredSize(new Dimension(150, 16));

		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				if(renderEngine == null) return;
				renderEngine.scale((float)slider.getValue() / ((float)slider.getMaximum()/2));
				renderEngine.applyTransform();
			}});

		sliderPanel.add(sliderLabel);
		sliderPanel.add(slider);

		// Create the colour sliders
		JPanel colourPanel = new JPanel();
		colourPanel.setLayout(new BoxLayout(colourPanel, BoxLayout.Y_AXIS));
		colourPanel.setBorder(BorderFactory.createTitledBorder("Colour"));

		// Ambience Slider
		JLabel ambienceLabel = new JLabel("Ambience: ");
		ambienceSlider = new JSlider(0,255,127);
		ambienceSlider.setPreferredSize(new Dimension(150, 16));

		ambienceSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				renderEngine.setAmbience((float)ambienceSlider.getValue() / (float)ambienceSlider.getMaximum());
			}});

		// Ambience Slider
		JLabel intensityLabel = new JLabel("Intensity: ");
		intensitySlider = new JSlider(0,255,127);
		intensitySlider.setPreferredSize(new Dimension(150, 16));

		intensitySlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				renderEngine.setIntensity((float)intensitySlider.getValue() / (float)intensitySlider.getMaximum());
			}});

		colourPanel.add(ambienceLabel);
		colourPanel.add(ambienceSlider);
		colourPanel.add(intensityLabel);
		colourPanel.add(intensitySlider);

		// Create the Reset panel
		JButton resetButton = new JButton("Reset Model");
		resetButton.setPreferredSize(new Dimension(150, 30));
		resetButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(renderEngine == null) return;
				try{renderEngine.resetModel();}
				catch(IOException e){JOptionPane.showConfirmDialog(Gui.this, "Failed to reset, could not reopen polygon file.");}
				renderEngine.draw();
				slider.setValue(slider.getMaximum() / 2);
				ambienceSlider.setValue(ambienceSlider.getMaximum() / 2);
				intensitySlider.setValue(intensitySlider.getMaximum() / 2);
			}
		});

		// Add the sub-panels to the top panel
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 2;
		topPanel.add(selectionPanel, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		topPanel.add(sliderPanel, gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridheight = 1;
		topPanel.add(resetButton, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridheight = 2;
		topPanel.add(colourPanel, gbc);
		return topPanel;
	}

	private DrawPanel createRenderPanel(){
		drawPanel = new DrawPanel(panelSize);
		return drawPanel;
	}

	public void drawImage(BufferedImage img){
		drawPanel.drawImage(img);
	}

	private class MouseDragHandler implements MouseMotionListener{

		private int width;
		private int height;
		private Point hold; // hold is the point where dragging started

		public MouseDragHandler(JPanel panel){
			width = panel.getWidth();
			height = panel.getHeight();
		}

		/**
		 * Handle selection modes:
		 * 1. Translate (with a boolean option of making a z translate)
		 * 2. Rotate
		 */
		@Override
		public void mouseDragged(MouseEvent arg0) {
			if(renderEngine == null) return;
			if(hold != null){
				Point pos = arg0.getPoint();

				Point offset = new Point(pos.x - hold.x, pos.y - hold.y);

				switch(radioManager.getSelectedIndex()){
				case(0):
					renderEngine.translate(offset.x, offset.y, 0f);
				break;
				case(1):
					if(arg0.isShiftDown()) renderEngine.rotate(0f, 0f, offset.y);
					else if(arg0.isControlDown()) renderEngine.rotateLightSource(offset.x, offset.y, 0f);
					else renderEngine.rotate(offset.x, offset.y, 0f);
				break;
				}


				if(pos.x > width){;} //TODO set mouse location to 0 on panel and add height to offset?
				if(pos.y > height){;} // TODO set mouse location to 0 on panel and add height to offset?
			}

			//hold = pos;
		}

		public void startDrag(Point start){
			hold = start;
		}

		public void stopDrag(){
			if(renderEngine == null) return;
			hold = null;
			renderEngine.applyTransform();
		}

		@Override	public void mouseMoved(MouseEvent arg0) {
			if(renderEngine == null) return;
			if(renderEngine.containsPoint(arg0.getPoint())){
				Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR); 
			    setCursor(cursor);
			}
			else{
				setCursor(Cursor.getDefaultCursor());
			}
		}

	}

	private class MouseClickHandler implements MouseListener{

		private MouseDragHandler dragHandler;

		public MouseClickHandler(MouseDragHandler dragHandler){
			this.dragHandler = dragHandler;
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			if(renderEngine.containsPoint(arg0.getPoint()) || (radioManager.getSelectedIndex() == 1 && arg0.isControlDown()))
				dragHandler.startDrag(arg0.getPoint());
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			dragHandler.stopDrag();
		}

		// Unused methods required by interface
		@Override	public void mouseClicked(MouseEvent arg0) {}
		@Override	public void mouseEntered(MouseEvent arg0) {}
		@Override	public void mouseExited(MouseEvent arg0) {}

	}

	public static void main(String args[]){
		System.out.println(Math.ceil(-64.997856f));
		new Gui("monkey.txt");
	}

}
