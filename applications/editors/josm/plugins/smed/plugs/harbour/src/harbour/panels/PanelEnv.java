package harbour.panels;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JLabel;

import java.awt.Color;
import java.awt.Rectangle;

public class PanelEnv extends JPanel {

	private static final long serialVersionUID = 1L;
	private JLabel envLabel = null;
	

	/**
	 * This is the default constructor
	 */
	public PanelEnv() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		envLabel = new JLabel();
		envLabel.setBounds(new Rectangle(126, 17, 99, 46));
		envLabel.setText("nvironment");
		
		this.setSize(330, 270);
		this.setLayout(null);
		this.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		this.add(envLabel, null);
	}
}
