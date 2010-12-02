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

public class SizePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private ButtonGroup buttons = new ButtonGroup();
	public  TextFieldEx sizeTextField = null;
	private JLabel sizeLabel = null;
	private JRadioButton sizeRadioButton = null;
	private TextFieldEx chTextField = null;
	private JLabel chLabel = null;
	private JRadioButton chRadioButton = null;
	private TextFieldEx tieTextField = null;
	private JLabel tieLabel = null;
	private JRadioButton tieRadioButton = null;
	private TextFieldEx cargoTextField = null;
	private JLabel cargoLabel = null;
	private JRadioButton cargoRadioButton = null;
	private TextFieldEx oilTextField = null;
	private JLabel oilLabel = null;
	private JRadioButton oilRadioButton = null;
	
	private JComboBox comboBox = null;
	private Cat catType = VS_SIZE;  //  @jve:decl-index=0:
	public TextFieldEx selTextField = null;

	// Enumarations
	public static class Cat {private Cat() {} }
	public final static Cat VS_SIZE = new Cat();  //  @jve:decl-index=0:
	public final static Cat AN_SIZE = new Cat();  //  @jve:decl-index=0:
	
	public final static int NOT_SELECTED =  0;
	public final static int UNKNOWN      =  3;
	
	public final static int CAT_UNKNOWN  = 17;
	public final static int FEET         =  0;
	public final static int METER        =  1;
	
	public int units = METER;
	public String[][] anSize = {{"> 76ft"," > 23.2m"},
								{"71-75ft","21.6-22.9m"},
								{"66-70ft","20.1-21.3m"},
								{"61-65ft","18.6-19.8m"},
								{"56-60ft","17.1-18.2m"},
								{"51-55ft","15.5-16.0m"},
								{"46-50ft","14.0-15.2m"},
								{"41-45ft","12.5-13.7m"},
								{"36-40ft","11.0-12.2m"},
								{"31-35ft"," 9.4-10.0m"},
								{"26-30ft"," 7.9- 9.1m"},
								{"21-25ft"," 6.4- 7.6m"},
								{"16-20ft"," 4.9- 6.1m"},
								{"11-15ft"," 3.4- 4.6m"},
								{" 6-10ft"," 1.8- 3.0m"},
								{" 0- 5ft"," 0.0- 1.5m"}};

	public String[][] vsSize = {{"> 500ft lang","> 152m lang"},
								{"bis 500ft lang","bis 152m lang"}};
	
	/**
	 * This is the default constructor
	 */
	public SizePanel() {
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
		chLabel.setBounds(new Rectangle(75, 25, 70, 15));
		chLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		chLabel.setText("Fahrwasser");
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
			sizeTextField = new TextFieldEx(new Bounds(NOT_SELECTED, UNKNOWN), vsSize);
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
					catType = VS_SIZE;
					selTextField.setIndex(comboBox.getSelectedIndex(), true);
					selTextField = sizeTextField ;
					comboBox.removeAllItems();
					setComboBoxSize();
					comboBox.setSelectedIndex(selTextField.index);
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
			chTextField = new TextFieldEx(new Bounds(NOT_SELECTED, CAT_UNKNOWN), anSize);
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
					catType = AN_SIZE;
					selTextField.setIndex(comboBox.getSelectedIndex(), true);
					selTextField = chTextField ;
					comboBox.removeAllItems();
					setAnchorageSize();
					comboBox.setSelectedIndex(selTextField.index);
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
			tieTextField = new TextFieldEx(new Bounds(NOT_SELECTED, CAT_UNKNOWN), anSize);
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
					catType = AN_SIZE;
					selTextField.setIndex(comboBox.getSelectedIndex(), true);
					selTextField = tieTextField;
					comboBox.removeAllItems();
					setAnchorageSize();
					comboBox.setSelectedIndex(selTextField.index);
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
			cargoTextField = new TextFieldEx(new Bounds(NOT_SELECTED, CAT_UNKNOWN), anSize);
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
					catType = AN_SIZE;
					selTextField.setIndex(comboBox.getSelectedIndex(), true);
					selTextField = cargoTextField;
					comboBox.removeAllItems();
					setAnchorageSize();
					comboBox.setSelectedIndex(selTextField.index);
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
			oilTextField = new TextFieldEx(new Bounds(NOT_SELECTED, CAT_UNKNOWN), anSize);
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
					catType = AN_SIZE;
					selTextField.setIndex(comboBox.getSelectedIndex(), true);
					selTextField = oilTextField;
					comboBox.removeAllItems();
					setAnchorageSize();
					comboBox.setSelectedIndex(selTextField.index);
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
		comboBox.addItem(vsSize[0][units]);
		comboBox.addItem(vsSize[1][units]);
		comboBox.addItem("Unbekannt");
	}

	
	private void setAnchorageSize() {
		comboBox.addItem("* Waehle Groesse ...");
		
		for(int i = 0; i < 16; i++) {
			comboBox.addItem(anSize[i][units]);
		}
		
		comboBox.addItem("Unbekannt");
	}
	
	public Cat getCatType() {
		return catType;
	}
	
	public void changeUnits() {
		int i;
		
		selTextField.setIndex(comboBox.getSelectedIndex(),true);
		comboBox.removeAllItems();
		
		if(catType == AN_SIZE ) setAnchorageSize();
		else setComboBoxSize();
		
		comboBox.setSelectedIndex(selTextField.index);

		sizeTextField.setUnits(units);
		chTextField.setUnits(units);
		tieTextField.setUnits(units);
		cargoTextField.setUnits(units);
		oilTextField.setUnits(units);
		// comboBox.setSelectedIndex(i);
	}
	
	public class TextFieldEx extends JTextField {

		private int index;
		private int units;
		private Bounds bounds;
		private String [][] str;
		
		public TextFieldEx(Bounds b,String[][] s) {
			super();
			
			index = 0;
			bounds = b;
			units = METER;
			str = s;
			setText("");
		}
		
		public void setIndex(int i, boolean upd) {
			if(upd) this.index = i;
			
			if(i == bounds.getBottom()) setText("");
			if(i > bounds.getBottom() && i < bounds.getTop()) setText(str[i-1][units]);
			if(i == bounds.getTop()) setText("unbekannt");

		}
		
		private void setUnits(int u) {
			units = u;
			setIndex(index, false);
		}

	}
	
	public class Bounds {
		
		private int bottom;
		private int top;
		
		private Bounds(int b,int t) {
			bottom = b;
			top    = t;
		}
		
		private int getTop()	{ return top; }
		private int getBottom() { return bottom; }
	}
}