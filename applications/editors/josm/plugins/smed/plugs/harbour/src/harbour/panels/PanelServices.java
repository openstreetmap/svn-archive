package harbour.panels;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Rectangle;

public class PanelServices extends JPanel {

	private static final long serialVersionUID = 1L;
	private JLabel servLabel = null;

	/**
	 * This is the default constructor
	 */
	public PanelServices() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		servLabel = new JLabel();
		servLabel.setBounds(new Rectangle(128, 22, 69, 36));
		servLabel.setText("Services");
		
		this.setSize(330, 270);
		this.setLayout(null);
		this.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		this.add(servLabel, null);
	}

}
