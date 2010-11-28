package harbour.widgets;

import java.awt.Button;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

public class Sheltergram extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JButton  tile = null;
	private Icon stateGrey;
	private Icon stateGreen;
	private Icon stateYellow;
	private Icon stateRed;
	private String[] dirs = { "nw","n","ne","w","e","sw","s","se" };
	
	public Sheltergram() {
		super();
		
		setLayout(null);
		
		int k = 0;
		for(int i = 0; i < 3; i++)
			for(int j=0; j < 3; j++) {
				if(i == 1 && j == 1) {
					tile = new JButton();
					tile.setBackground(Color.WHITE);
					tile.setEnabled(false);
					tile.setIcon(new ImageIcon(getClass().getResource("/images/anker.png")));
				} else {
					stateGrey   = new ImageIcon(getClass().getResource("/images/" + dirs[k] + "_grey.png"));
					stateGreen  = new ImageIcon(getClass().getResource("/images/" + dirs[k] + "_green.png"));
					stateYellow = new ImageIcon(getClass().getResource("/images/" + dirs[k] + "_yellow.png"));
					stateRed    = new ImageIcon(getClass().getResource("/images/" + dirs[k++] + "_red.png"));
					
					tile = new LightTile(stateGrey, stateGreen, stateYellow, stateRed);
				}
				
				tile.setBounds(new Rectangle(j*25,i*25, 25, 25));
				add(tile);
			}
				
		
	}
}
