package harbour.panels;

import harbour.widgets.Sheltergram;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import java.awt.Cursor;

public class PanelGeneral extends JPanel {

	private JLabel latLabel = null;
	private JLabel posLabel = null;
	private JTextField latTextField = null;
	private JLabel lonLabel = null;
	private JTextField lonTextField = null;
	private JLabel harborLabel = null;
	private JComboBox sizeComboBox = null;
	private JComboBox typeComboBox = null;
	private JLabel pubLabel = null;
	private JTextField pubTextField = null;
	private JLabel chartLabel = null;
	private JTextField chartTextField = null;
	private JLabel pilotLabel = null;
	private JCheckBox oblCheckBox = null;
	private JCheckBox jCheckBox = null;
	private JCheckBox locCheckBox1 = null;
	private JCheckBox jCheckBox2 = null;
	private JLabel tugLabel = null;
	private JCheckBox tugCheckBox = null;
	private JCheckBox tugAssCheckBox = null;
	private JLabel qLabel = null;
	private JCheckBox usualCheckBox = null;
	private JCheckBox desCheckBox = null;
	private JCheckBox jCheckBox1 = null;
	private JLabel tshelterLabel = null;
	private JPanel shelterPanel = null;
	private JLabel sizeLabel = null;
	private JLabel typeLabel = null;
	public PanelGeneral() {
		super();
		initialize();
	}



	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        typeLabel = new JLabel();
        typeLabel.setBounds(new Rectangle(270, 2, 43, 20));
        typeLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        typeLabel.setText("Type");
        sizeLabel = new JLabel();
        sizeLabel.setBounds(new Rectangle(210, 2, 34, 20));
        sizeLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        sizeLabel.setText("Size");
        tshelterLabel = new JLabel();
        tshelterLabel.setBounds(new Rectangle(246, 144, 50, 20));
        tshelterLabel.setText("Schutz");
        qLabel = new JLabel();
        qLabel.setBounds(new Rectangle(50, 205, 85, 20));
        qLabel.setText("Quarantäne");
        tugLabel = new JLabel();
        tugLabel.setBounds(new Rectangle(46, 140, 110, 20));
        tugLabel.setText("Schleppdienst");
        pilotLabel = new JLabel();
        pilotLabel.setBounds(new Rectangle(55, 80, 100, 20));
        pilotLabel.setText("Lotsendienst");
        chartLabel = new JLabel();
        chartLabel.setBounds(new Rectangle(210, 50, 35, 20));
        chartLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        chartLabel.setText("Karte");
        pubLabel = new JLabel();
        pubLabel.setBounds(new Rectangle(53, 50, 70, 20));
        pubLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        pubLabel.setText("Publication");
        harborLabel = new JLabel();
        harborLabel.setBounds(new Rectangle(148, 2, 55, 20));
        harborLabel.setText("Harbor:");
        lonLabel = new JLabel();
        lonLabel.setBounds(new Rectangle(100, 25, 23, 20));
        lonLabel.setText("Lon");
        lonLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        posLabel = new JLabel();
        posLabel.setBounds(new Rectangle(45, 2, 61, 20));
        posLabel.setText("Position");
        latLabel = new JLabel();
        latLabel.setBounds(new Rectangle(2, 25, 20, 20));
        latLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        latLabel.setText("Lat");

        this.setSize(new Dimension(330, 270));
        this.setLayout(null);
        this.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        this.add(latLabel, null);
        this.add(posLabel, null);
        this.add(getLatTextField(), null);
        this.add(lonLabel, null);
        this.add(getLonTextField(), null);
        this.add(harborLabel, null);
        this.add(getSizeComboBox(), null);
        this.add(getTypeComboBox(), null);
        this.add(pubLabel, null);
        this.add(getPubTextField(), null);
        this.add(chartLabel, null);
        this.add(getChartTextField(), null);
        this.add(pilotLabel, null);
        this.add(getOblCheckBox(), null);
        this.add(getJCheckBox(), null);
        this.add(getLocCheckBox1(), null);
        this.add(getJCheckBox2(), null);
        this.add(tugLabel, null);
        this.add(getTugCheckBox(), null);
        this.add(getTugAssCheckBox(), null);
        this.add(qLabel, null);
        this.add(getUsualCheckBox(), null);
        this.add(getDesCheckBox(), null);
        this.add(getJCheckBox1(), null);
        this.add(tshelterLabel, null);
        this.add(getShelterPanel(), null);
        this.add(sizeLabel, null);
        this.add(typeLabel, null);
	}



	/**
	 * This method initializes latTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getLatTextField() {
		if (latTextField == null) {
			latTextField = new JTextField();
			latTextField.setBounds(new Rectangle(22, 25, 75, 20));
			latTextField.setHorizontalAlignment(JTextField.RIGHT);
		}
		return latTextField;
	}



	/**
	 * This method initializes lonTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getLonTextField() {
		if (lonTextField == null) {
			lonTextField = new JTextField();
			lonTextField.setBounds(new Rectangle(123, 25, 75, 20));
			lonTextField.setHorizontalAlignment(JTextField.RIGHT);
		}
		return lonTextField;
	}



	/**
	 * This method initializes sizeComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getSizeComboBox() {
		if (sizeComboBox == null) {
			sizeComboBox = new JComboBox();
			sizeComboBox.setBounds(new Rectangle(210, 25, 55, 20));
		}
		return sizeComboBox;
	}



	/**
	 * This method initializes typeComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getTypeComboBox() {
		if (typeComboBox == null) {
			typeComboBox = new JComboBox();
			typeComboBox.setBounds(new Rectangle(270, 25, 55, 20));
			typeComboBox.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			typeComboBox.addItem("langer text");
		}
		return typeComboBox;
	}



	/**
	 * This method initializes pubTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getPubTextField() {
		if (pubTextField == null) {
			pubTextField = new JTextField();
			pubTextField.setBounds(new Rectangle(123, 50, 75, 20));
		}
		return pubTextField;
	}



	/**
	 * This method initializes chartTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getChartTextField() {
		if (chartTextField == null) {
			chartTextField = new JTextField();
			chartTextField.setBounds(new Rectangle(250, 50, 75, 20));
		}
		return chartTextField;
	}



	/**
	 * This method initializes oblCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getOblCheckBox() {
		if (oblCheckBox == null) {
			oblCheckBox = new JCheckBox();
			oblCheckBox.setBounds(new Rectangle(5, 100, 110, 20));
			oblCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			oblCheckBox.setText("obligatorisch");
		}
		return oblCheckBox;
	}



	/**
	 * This method initializes jCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBox() {
		if (jCheckBox == null) {
			jCheckBox = new JCheckBox();
			jCheckBox.setBounds(new Rectangle(5, 117, 110, 20));
			jCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			jCheckBox.setText("verfügbar");
		}
		return jCheckBox;
	}



	/**
	 * This method initializes locCheckBox1	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getLocCheckBox1() {
		if (locCheckBox1 == null) {
			locCheckBox1 = new JCheckBox();
			locCheckBox1.setBounds(new Rectangle(125, 100, 110, 20));
			locCheckBox1.setFont(new Font("Dialog", Font.PLAIN, 12));
			locCheckBox1.setText("lokale Hilfe");
		}
		return locCheckBox1;
	}



	/**
	 * This method initializes jCheckBox2	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBox2() {
		if (jCheckBox2 == null) {
			jCheckBox2 = new JCheckBox();
			jCheckBox2.setBounds(new Rectangle(125, 117, 70, 21));
			jCheckBox2.setFont(new Font("Dialog", Font.PLAIN, 12));
			jCheckBox2.setText("ratsam");
		}
		return jCheckBox2;
	}



	/**
	 * This method initializes tugCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getTugCheckBox() {
		if (tugCheckBox == null) {
			tugCheckBox = new JCheckBox();
			tugCheckBox.setBounds(new Rectangle(5, 160, 121, 20));
			tugCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			tugCheckBox.setText("Bergungsschiff");
		}
		return tugCheckBox;
	}



	/**
	 * This method initializes tugAssCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getTugAssCheckBox() {
		if (tugAssCheckBox == null) {
			tugAssCheckBox = new JCheckBox();
			tugAssCheckBox.setBounds(new Rectangle(5, 180, 110, 20));
			tugAssCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			tugAssCheckBox.setText("Schlepphilfe");
		}
		return tugAssCheckBox;
	}



	/**
	 * This method initializes usualCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getUsualCheckBox() {
		if (usualCheckBox == null) {
			usualCheckBox = new JCheckBox();
			usualCheckBox.setBounds(new Rectangle(5, 225, 62, 20));
			usualCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			usualCheckBox.setText("üblich");
		}
		return usualCheckBox;
	}



	/**
	 * This method initializes desCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getDesCheckBox() {
		if (desCheckBox == null) {
			desCheckBox = new JCheckBox();
			desCheckBox.setBounds(new Rectangle(70, 225, 110, 20));
			desCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			desCheckBox.setText("Desinfektion");
		}
		return desCheckBox;
	}



	/**
	 * This method initializes jCheckBox1	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBox1() {
		if (jCheckBox1 == null) {
			jCheckBox1 = new JCheckBox();
			jCheckBox1.setBounds(new Rectangle(5, 245, 110, 20));
			jCheckBox1.setFont(new Font("Dialog", Font.PLAIN, 12));
			jCheckBox1.setText("anderes");
		}
		return jCheckBox1;
	}



	/**
	 * This method initializes shelterPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getShelterPanel() {
		if (shelterPanel == null) {
			shelterPanel = new Sheltergram();
			shelterPanel.setBounds(new Rectangle(232, 170, 75, 75));
		}
		return shelterPanel;
	}

}
