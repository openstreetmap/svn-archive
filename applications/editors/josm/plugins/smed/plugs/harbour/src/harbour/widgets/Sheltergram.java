package harbour.widgets;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

public class Sheltergram extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JButton  tile = null;
	private String[] dirs = { "nw","n","ne","w","anker","e","sw","s","se" };
	
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
				} else tile = new LightTile("");
				
				tile.setIcon(new ImageIcon(getClass().getResource("/images/" + dirs[k++] + ".png")));
				tile.setBounds(new Rectangle(j*25,i*25, 25, 25));
				add(tile);
			}
				
		
	}
}
