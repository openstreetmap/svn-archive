package harbour.panels;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Dimension;

public class PanelRelations extends JPanel {

	private static final long serialVersionUID = 1L;
	private JLabel relLabel = null;

	/**
	 * This is the default constructor
	 */
	public PanelRelations() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		relLabel = new JLabel();
		relLabel.setBounds(new Rectangle(101, 18, 120, 36));
		relLabel.setText("Relationen");
		
		this.setSize(330, 270);
		this.setLayout(null);
		this.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		this.add(relLabel, null);
	}
}
