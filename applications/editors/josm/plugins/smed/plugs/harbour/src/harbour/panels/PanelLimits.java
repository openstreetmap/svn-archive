package harbour.panels;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import javax.swing.JLabel;
import java.awt.Rectangle;


public class PanelLimits extends JPanel {

	private JLabel limLabel = null;

	public PanelLimits() {
		super();
		initialize();
	}
	
	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        limLabel = new JLabel();
        limLabel.setBounds(new Rectangle(110, 18, 93, 30));
        limLabel.setText("Limits");
        this.setSize(new Dimension(330, 270));
        this.setLayout(null);
        this.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        this.add(limLabel, null);
	}
}
