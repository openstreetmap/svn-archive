package harbour.widgets;

import harbour.widgets.TristateCheckBox.State;

import java.awt.GridBagLayout;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Rectangle;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JRadioButton;

public class TextFieldEx extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private ButtonGroup buttons = new ButtonGroup();
	private JTextField sizeTextField = null;
	private JLabel sizeLabel = null;
	private JRadioButton sizeRadioButton = null;
	private JTextField chTextField = null;
	private JLabel chLabel = null;
	private JRadioButton chRadioButton = null;
	private JTextField tieTextField = null;
	private JLabel tieLabel = null;
	private JRadioButton tieRadioButton = null;
	private JTextField cargoTextField = null;
	private JLabel cargoLabel = null;
	private JRadioButton cargoRadioButton = null;

	private JTextField oilTextField = null;

	private JLabel oilLabel = null;

	private JRadioButton oilRadioButton = null;
	
	private JComboBox comboBox = null;
	private Ssize sizeType = VS_SIZE;  //  @jve:decl-index=0:
	public JTextField selTextField = null;

	// Enumarations
	public static class Ssize {private Ssize() {} }
	public final static Ssize VS_SIZE = new Ssize();  //  @jve:decl-index=0:
	public final static Ssize AN_SIZE = new Ssize();  //  @jve:decl-index=0:
	
	public final static int NOT_SELECTED = 0;
	public final static int LARGE        = 1;
	public final static int MEDIUM       = 2;
	public final static int UNKNOWN      = 3;
	
	/**
	 * This is the default constructor
	 */
	public TextFieldEx() {
		super();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	public void initialize() {
		oilLabel = new JLabel();
		oilLabel.setBounds(new Rectangle(75, 94, 80, 15));
		oilLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		oilLabel.setText("Oelterminal");
		cargoLabel = new JLabel();
		cargoLabel.setBounds(new Rectangle(75, 71, 90, 15));
		cargoLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		cargoLabel.setText("Frachtanleger");
		tieLabel = new JLabel();
		tieLabel.setBounds(new Rectangle(75, 48, 75, 15));
		tieLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		tieLabel.setText("Ankerplatz");
		chLabel = new JLabel();
		chLabel.setBounds(new Rectangle(75, 25, 60, 15));
		chLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		chLabel.setText("Channel");
		sizeLabel = new JLabel();
		sizeLabel.setBounds(new Rectangle(75, 2, 95, 15));
		sizeLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		sizeLabel.setText("Schiffsgroesse");
		this.setSize(190, 112);
		this.setLayout(null);
		this.add(getSizeTextField(), null);
		this.add(sizeLabel, null);
		this.add(getSizeRadioButton(), null);
		this.add(getChTextField(), null);
		this.add(chLabel, null);
		this.add(getChRadioButton(), null);
		this.add(getTieTextField(), null);
		this.add(tieLabel, null);
		this.add(getTieRadioButton(), null);
		this.add(getCargoTextField(), null);
		this.add(cargoLabel, null);
		this.add(getCargoRadioButton(), null);
		this.add(getOilTextField(), null);
		this.add(oilLabel, null);
		this.add(getOilRadioButton(), null);
		
		buttons.add(sizeRadioButton);
		buttons.add(chRadioButton);
		buttons.add(tieRadioButton);
		buttons.add(cargoRadioButton);
		buttons.add(oilRadioButton);
		
		sizeRadioButton.setSelected(true);
		selTextField = 	sizeTextField;
	}

	/**
	 * This method initializes sizeTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getSizeTextField() {
		if (sizeTextField == null) {
			sizeTextField = new JTextField();
			sizeTextField.setBounds(new Rectangle(0, 0, 75, 20));
			sizeTextField.setEditable(false);
		}
		return sizeTextField;
	}

	/**
	 * This method initializes sizeRadioButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getSizeRadioButton() {
		if (sizeRadioButton == null) {
			sizeRadioButton = new JRadioButton();
			sizeRadioButton.setBounds(new Rectangle(170, 0, 20, 20));

			setComboBoxSize();
			
			sizeRadioButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					sizeType = VS_SIZE;
					selTextField = sizeTextField ;
					comboBox.removeAllItems();
					setComboBoxSize();
				}
			});
		}
		return sizeRadioButton;
	}


	/**
	 * This method initializes chTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getChTextField() {
		if (chTextField == null) {
			chTextField = new JTextField();
			chTextField.setBounds(new Rectangle(0, 23, 75, 20));
		}
		return chTextField;
	}

	/**
	 * This method initializes chRadioButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getChRadioButton() {
		if (chRadioButton == null) {
			chRadioButton = new JRadioButton();
			chRadioButton.setBounds(new Rectangle(170, 23, 20, 20));
			chRadioButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					sizeType = AN_SIZE;
					selTextField = chTextField ;
					comboBox.removeAllItems();
					setAnchorageSize();
				}
			});
		}
		return chRadioButton;
	}


	/**
	 * This method initializes tieTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTieTextField() {
		if (tieTextField == null) {
			tieTextField = new JTextField();
			tieTextField.setBounds(new Rectangle(0, 46, 75, 20));
		}
		return tieTextField;
	}

	/**
	 * This method initializes tieRadioButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getTieRadioButton() {
		if (tieRadioButton == null) {
			tieRadioButton = new JRadioButton();
			tieRadioButton.setBounds(new Rectangle(170, 46, 20, 20));
			tieRadioButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					sizeType = AN_SIZE;
					selTextField = tieTextField;
					comboBox.removeAllItems();
					setAnchorageSize();
				}
			});
		}
		return tieRadioButton;
	}

	/**
	 * This method initializes cargoTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getCargoTextField() {
		if (cargoTextField == null) {
			cargoTextField = new JTextField();
			cargoTextField.setBounds(new Rectangle(0, 69, 75, 20));
		}
		return cargoTextField;
	}

	/**
	 * This method initializes cargoRadioButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getCargoRadioButton() {
		if (cargoRadioButton == null) {
			cargoRadioButton = new JRadioButton();
			cargoRadioButton.setBounds(new Rectangle(170, 69, 20, 20));
			cargoRadioButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					sizeType = AN_SIZE;
					selTextField = cargoTextField;
					comboBox.removeAllItems();
					setAnchorageSize();
				}
			});
		}
		return cargoRadioButton;
	}

	/**
	 * This method initializes oilTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getOilTextField() {
		if (oilTextField == null) {
			oilTextField = new JTextField();
			oilTextField.setBounds(new Rectangle(0, 92, 75, 20));
		}
		return oilTextField;
	}

	/**
	 * This method initializes oilRadioButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getOilRadioButton() {
		if (oilRadioButton == null) {
			oilRadioButton = new JRadioButton();
			oilRadioButton.setBounds(new Rectangle(170, 92, 20, 20));
			oilRadioButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					sizeType = AN_SIZE;
					selTextField = oilTextField;
					comboBox.removeAllItems();
					setAnchorageSize();					
				}
			});
		}
		return oilRadioButton;
	}

	public void setComboBox(JComboBox box) {
		comboBox = box;
	}

	private void setComboBoxSize() {
		comboBox.addItem("* Waehle Groesse ...");
		comboBox.addItem("> 500 ft lang");
		comboBox.addItem("bis 500 ft lang");
		comboBox.addItem("Unbekannt");
	}

	private void setAnchorageSize() {
		comboBox.addItem("* Waehle Groesse ...");
		comboBox.addItem("A > 76 ft       > 23.2m");
		comboBox.addItem("B 71-75ft      21.6-22.9m");
		comboBox.addItem("C 66-70ft      20.1-21.3m");
		comboBox.addItem("D 61-65ft      18.6-19.8m");
		comboBox.addItem("E 56-60ft      17.1-18.2m");
		comboBox.addItem("F 51-55ft      15.5-16.0m");
		comboBox.addItem("G 46-50ft      14.0-15.2m");
		comboBox.addItem("H 41-45ft      12.5-13.7m");
		comboBox.addItem("J 36-40ft      11.0-12.2m");
		comboBox.addItem("K 31-35ft       9.4-10.0m");
		comboBox.addItem("L 26-30ft       7.9- 9.1m");
		comboBox.addItem("M 21-25ft       6.4- 7.6m");
		comboBox.addItem("N 16-20ft       4.9- 6.1m");
		comboBox.addItem("O 11-15ft       3.4- 4.6m");
		comboBox.addItem("P  6-10ft       1.8- 3.0m");
		comboBox.addItem("Q  0- 5ft       0.0- 1.5m");
		comboBox.addItem("U Unbekannt    Unbekannt");
	}
	
	public Ssize getSizeType() {
		return sizeType;
	}
}