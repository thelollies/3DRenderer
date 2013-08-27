package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class JRadioManager {

	private JRadioButton[] buttons;
	private int curSelected = -1; //If a non-zero length array is passed, this defaults to zero

	public JRadioManager(JRadioButton[] buttons){
		this.buttons = buttons;
		
		for(JRadioButton button : buttons){
			button.setSelected(false);
			
			button.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent arg0) {
					JRadioManager.this.handleSelection((JRadioButton)arg0.getSource());
				}});
		}
		
		if(buttons.length > 0){
			curSelected = 0;
			buttons[0].setSelected(true);
		}
	}	

	private void handleSelection(JRadioButton selected){
		
		for(int i = 0; i < buttons.length; i++){
			if(selected.equals(buttons[i])){
				curSelected = i;
			}else{
				buttons[i].setSelected(false);
			}
			
		}
	}
	
	public int getSelectedIndex(){
		return curSelected;
	}

}
