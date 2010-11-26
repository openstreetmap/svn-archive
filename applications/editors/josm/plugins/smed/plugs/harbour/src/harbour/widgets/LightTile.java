package harbour.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public class LightTile extends JButton {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JButton tile = null;
	
	public LightTile(String text) {
		super(text);
		
		tile = this;
		
		tile.setBackground(Color.LIGHT_GRAY);
		tile.setToolTipText("Click to change state");
		tile.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(tile.getBackground() == Color.LIGHT_GRAY)	tile.setBackground(Color.GREEN);
				else if(tile.getBackground() == Color.GREEN)	tile.setBackground(Color.YELLOW);
				else if(tile.getBackground() == Color.YELLOW)	tile.setBackground(Color.RED);
				else if(tile.getBackground() == Color.RED)		tile.setBackground(Color.LIGHT_GRAY);
			}
		});

	}	
}
