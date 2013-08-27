package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import controllers.RenderEngine;

public class Gui extends JFrame{

	private DrawPanel drawPanel;
	private RenderEngine renderEngine;
	private Dimension panelSize = new Dimension(600,600);
	private JRadioManager radioManager;
	private JRadioButton[] buttons;
	private JSlider slider;

	public Gui(String fileName){

		setLayout(new BorderLayout());
		add(createTopPanel(), BorderLayout.NORTH);
		add(createRenderPanel(), BorderLayout.WEST);

		// Start the render engine
		renderEngine = new RenderEngine(this, fileName, panelSize.width, panelSize.height);
		renderEngine.draw();

		this.setFocusable(true);
		addListeners();

		pack();
		setVisible(true);
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

		// TODO make the translate cursor a 4 arrow thing (resize one) and rotate the hand icon

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
				renderEngine.scale((float)slider.getValue() / ((float)slider.getMaximum()/2));
				renderEngine.applyTransform();
			}});

		sliderPanel.add(sliderLabel);
		sliderPanel.add(slider);

		// Create the Reset panel

		JButton resetButton = new JButton("Reset Model");
		resetButton.setPreferredSize(new Dimension(150, 30));
		resetButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				renderEngine.resetModel();
				renderEngine.draw();
				slider.setValue(slider.getMaximum() / 2);
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

			if(hold != null){
				Point pos = arg0.getPoint();

				Point offset = new Point(pos.x - hold.x, pos.y - hold.y);

				switch(radioManager.getSelectedIndex()){
				case(0):
					renderEngine.translate(offset.x, offset.y);
				break;
				case(1):
					renderEngine.rotate(offset.x, offset.y);
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
			hold = null;
			renderEngine.applyTransform();
		}

		@Override	public void mouseMoved(MouseEvent arg0) {}

	}

	private class MouseClickHandler implements MouseListener{

		private MouseDragHandler dragHandler;

		public MouseClickHandler(MouseDragHandler dragHandler){
			this.dragHandler = dragHandler;
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
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
