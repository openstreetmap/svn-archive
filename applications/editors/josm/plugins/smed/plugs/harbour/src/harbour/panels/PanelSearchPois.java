package harbour.panels;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Rectangle;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JComboBox;

public class PanelSearchPois extends JPanel {

	private static final long serialVersionUID = 1L;
	private JLabel radLabel = null;
	private JTextField radTextField = null;
	private JLabel unitLabel = null;
	private JButton jButton = null;
	private JScrollPane jScrollPane = null;
	private JTable jTable = null;
	private JLabel layerLabel = null;
	private JComboBox jComboBox = null;

	/**
	 * This is the default constructor
	 */
	public PanelSearchPois() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		layerLabel = new JLabel();
		layerLabel.setBounds(new Rectangle(10, 5, 50, 20));
		layerLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		layerLabel.setText("Ebene:");
		unitLabel = new JLabel();
		unitLabel.setBounds(new Rectangle(130, 240, 20, 21));
		unitLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		unitLabel.setText("m");
		radLabel = new JLabel();
		radLabel.setBounds(new Rectangle(5, 240, 55, 20));
		radLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		radLabel.setText("Umkreis:");
		this.setSize(330, 270);
		this.setLayout(null);
		this.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		this.add(radLabel, null);
		this.add(getRadTextField(), null);
		this.add(unitLabel, null);
		this.add(getJButton(), null);
		this.add(getJScrollPane(), null);
		this.add(layerLabel, null);
		this.add(getJComboBox(), null);
	}

	/**
	 * This method initializes radTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getRadTextField() {
		if (radTextField == null) {
			radTextField = new JTextField();
			radTextField.setBounds(new Rectangle(60, 240, 70, 20));
		}
		return radTextField;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setBounds(new Rectangle(240, 240, 80, 20));
			jButton.setText("Suche");
		}
		return jButton;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setBounds(new Rectangle(10, 35, 310, 195));
			jScrollPane.setViewportView(getJTable());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getJTable() {
		if (jTable == null) {
			jTable = new JTable();
		}
		return jTable;
	}

	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJComboBox() {
		if (jComboBox == null) {
			jComboBox = new JComboBox();
			jComboBox.setBounds(new Rectangle(70, 5, 250, 20));
		}
		return jComboBox;
	}

}
