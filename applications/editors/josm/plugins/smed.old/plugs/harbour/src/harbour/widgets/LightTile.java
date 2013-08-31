package harbour.widgets;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

public class LightTile extends JButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JButton tile;
	private Icon stateGrey;
	private Icon stateGreen;
	private Icon stateYellow;
	private Icon stateRed;
	
	public LightTile(Icon greyState, Icon greenState, Icon yellowState, Icon redState) {
		super();
		
		tile = this;
		stateGrey   = greyState;
		stateGreen  = greenState;
		stateYellow = yellowState;
		stateRed    = redState;
		
		tile.setBackground(Color.LIGHT_GRAY);
		tile.setToolTipText("Click to change state");
		tile.setIcon(stateGrey);
		tile.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(tile.getBackground() == Color.LIGHT_GRAY)	{ 
					tile.setBackground(Color.GREEN);
					tile.setIcon(stateGreen);
				} else if(tile.getBackground() == Color.GREEN)	{
					tile.setBackground(Color.YELLOW);
					tile.setIcon(stateYellow);
				} else if(tile.getBackground() == Color.YELLOW)	 {
					tile.setBackground(Color.RED);
					tile.setIcon(stateRed);
				} else if(tile.getBackground() == Color.RED) {
					tile.setBackground(Color.LIGHT_GRAY);
					tile.setIcon(stateGrey);
				}
			}
		});

	}

}
