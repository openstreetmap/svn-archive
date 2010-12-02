package harbour.panels;

import harbour.widgets.LightTile;
import harbour.widgets.SizePanel;
// import harbour.widgets.SizePanel.Ssize;
import harbour.widgets.TristateCheckBox;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JLabel;
import java.awt.Rectangle;
import javax.swing.JCheckBox;
import java.awt.Font;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.ImageIcon;


public class PanelLimits extends JPanel {

	private ButtonGroup buttons = new ButtonGroup();
	private JLabel limLabel = null;
	private JCheckBox tideCheckBox = null;
	private JCheckBox swellCheckBox1 = null;
	private JCheckBox iceCheckBox = null;
	private JCheckBox otherCheckBox = null;
	private JTextField tideTextField = null;
	private JLabel mLabel = null;
	private JComboBox allComboBox = null;
	private JLabel grLabel = null;
	private JButton grButton = null;
	private JCheckBox olCheckBox = null;
	private JCheckBox turnCheckBox = null;
	private JCheckBox impCheckBox = null;
	private JCheckBox usCheckBox = null;
	private JCheckBox etaCheckBox = null;
	private JPanel exPanel = null;
	private JLabel unitLabel = null;
	private JRadioButton meterRadioButton = null;
	private JRadioButton feetRadioButton = null;
		
	public PanelLimits() {
		super();
		initialize();
	}
	
	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        unitLabel = new JLabel();
        unitLabel.setBounds(new Rectangle(241, 53, 43, 20));
        unitLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        unitLabel.setText("Einheit:");
        grLabel = new JLabel();
        grLabel.setBounds(new Rectangle(213, 242, 80, 16));
        grLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        grLabel.setText("Ankergrund");
        mLabel = new JLabel();
        mLabel.setBounds(new Rectangle(30, 49, 20, 20));
        mLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        mLabel.setText("m");
        limLabel = new JLabel();
        limLabel.setBounds(new Rectangle(80, 2, 195, 20));
        limLabel.setText("Einfahrtsbeschraenkungen");
        this.setSize(new Dimension(330, 270));
        this.setLayout(null);
        this.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        this.setFont(new Font("Dialog", Font.PLAIN, 12));
        this.add(limLabel, null);
        this.add(getTideCheckBox(), null);
        this.add(getSwellCheckBox1(), null);
        this.add(getIceCheckBox(), null);
        this.add(getOtherCheckBox(), null);
        this.add(getTideTextField(), null);
        this.add(mLabel, null);
        this.add(grLabel, null);
        this.add(getGrButton(), null);
        this.add(getOlCheckBox(), null);
        this.add(getTurnCheckBox(), null);
        this.add(getImpCheckBox(), null);
        this.add(getUsCheckBox(), null);
        this.add(getEtaCheckBox(), null);
        this.add(getAllComboBox(), null);
        this.add(getExPanel(), null);
        this.add(unitLabel, null);
        this.add(getMeterRadioButton(), null);
        this.add(getFeetRadioButton(), null);
        
        buttons.add(feetRadioButton);
        buttons.add(meterRadioButton);
	}

	/**
	 * This method initializes tideCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getTideCheckBox() {
		if (tideCheckBox == null) {
			tideCheckBox = new JCheckBox();
			tideCheckBox.setBounds(new Rectangle(2, 23, 51, 20));
			tideCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			tideCheckBox.setText("Tide");
		}
		return tideCheckBox;
	}

	/**
	 * This method initializes swellCheckBox1	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getSwellCheckBox1() {
		if (swellCheckBox1 == null) {
			swellCheckBox1 = new JCheckBox();
			swellCheckBox1.setBounds(new Rectangle(86, 23, 85, 20));
			swellCheckBox1.setFont(new Font("Dialog", Font.PLAIN, 12));
			swellCheckBox1.setText("Seegang");
		}
		return swellCheckBox1;
	}

	/**
	 * This method initializes iceCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getIceCheckBox() {
		if (iceCheckBox == null) {
			iceCheckBox = new JCheckBox();
			iceCheckBox.setBounds(new Rectangle(170, 23, 43, 21));
			iceCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			iceCheckBox.setText("Eis");
		}
		return iceCheckBox;
	}

	/**
	 * This method initializes otherCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getOtherCheckBox() {
		if (otherCheckBox == null) {
			otherCheckBox = new JCheckBox();
			otherCheckBox.setBounds(new Rectangle(254, 23, 70, 20));
			otherCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			otherCheckBox.setText("Andere");
		}
		return otherCheckBox;
	}

	/**
	 * This method initializes tideTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTideTextField() {
		if (tideTextField == null) {
			tideTextField = new JTextField();
			tideTextField.setBounds(new Rectangle(2, 48, 25, 20));
		}
		return tideTextField;
	}

	/**
	 * This method initializes allComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getAllComboBox() {
		if (allComboBox == null) {
			allComboBox = new JComboBox();
			allComboBox.setBounds(new Rectangle(135, 83, 190, 20));
			allComboBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			allComboBox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
					if(((SizePanel) exPanel).selTextField == null ) return;
					int type = allComboBox.getSelectedIndex();
					if(type < 0) return;
					
					((SizePanel) exPanel).selTextField.setIndex(type, false);
					/*
					Ssize sType = ((SizePanel) exPanel).getSizeType();
					if(sType == SizePanel.VS_SIZE ) {
						switch (type) {
							case SizePanel.NOT_SELECTED:
								((SizePanel) exPanel).selTextField.setText("");
								break;

							case SizePanel.UNKNOWN:
								((SizePanel) exPanel).selTextField.setText("unbekannt");
								break;

							default:
								((SizePanel) exPanel).selTextField.setText(((SizePanel) exPanel).vsSize[type-1][((SizePanel) exPanel).units]);
								break;
						}
					}
					
					if(sType == SizePanel.AN_SIZE) {
						switch (type) {
						case SizePanel.NOT_SELECTED:
							((SizePanel) exPanel).selTextField.setText("");
							break;
							
						case SizePanel.CAT_UNKNOWN:
							((SizePanel) exPanel).selTextField.setText("unbekannt");
							break;
							
						default:
							((SizePanel) exPanel).selTextField.setText(((SizePanel) exPanel).anSize[type-1][((SizePanel) exPanel).units]);
							break;
						}
					} */
				}
			});
		}
		return allComboBox;
	}

	/**
	 * This method initializes grButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getGrButton() {
		if (grButton == null) {
			Icon grey   = new ImageIcon(getClass().getResource("/images/anker_grey.png"));
			Icon green  = new ImageIcon(getClass().getResource("/images/anker_green.png"));
			Icon yellow = new ImageIcon(getClass().getResource("/images/anker_yellow.png"));
			Icon red    = new ImageIcon(getClass().getResource("/images/anker_red.png"));
			
			grButton = new LightTile(grey, green, yellow, red);
			grButton.setBounds(new Rectangle(162, 240, 50, 20));
		}
		return grButton;
	}

	/**
	 * This method initializes olCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getOlCheckBox() {
		if (olCheckBox == null) {
			olCheckBox = new TristateCheckBox();
			olCheckBox.setBounds(new Rectangle(7, 109, 133, 20));
			olCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			olCheckBox.setText("Overhead Limits");
		}
		return olCheckBox;
	}

	/**
	 * This method initializes turnCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getTurnCheckBox() {
		if (turnCheckBox == null) {
			turnCheckBox = new TristateCheckBox();
			turnCheckBox.setBounds(new Rectangle(7, 132, 105, 20));
			turnCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			turnCheckBox.setText("Wendeplatz");
		}
		return turnCheckBox;
	}

	/**
	 * This method initializes impCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getImpCheckBox() {
		if (impCheckBox == null) {
			impCheckBox = new TristateCheckBox();
			impCheckBox.setBounds(new Rectangle(7, 155, 95, 20));
			impCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			impCheckBox.setText("Zollhafen");
		}
		return impCheckBox;
	}

	/**
	 * This method initializes usCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getUsCheckBox() {
		if (usCheckBox == null) {
			usCheckBox = new TristateCheckBox();
			usCheckBox.setBounds(new Rectangle(7, 178, 125, 20));
			usCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
			usCheckBox.setText("US. Vertretung");
		}
		return usCheckBox;
	}

	/**
	 * This method initializes etaCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getEtaCheckBox() {
		if (etaCheckBox == null) {
			etaCheckBox = new TristateCheckBox();
			etaCheckBox.setBounds(new Rectangle(7, 201, 120, 20));
			etaCheckBox.setText("ETA Nachricht");
			etaCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return etaCheckBox;
	}

	/**
	 * This method initializes exPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getExPanel() {
		if (exPanel == null) {
			exPanel = new SizePanel();
			exPanel.setLayout(null);
			exPanel.setBounds(new Rectangle(135, 110, 190, 112));
			((SizePanel) exPanel).setComboBox(allComboBox);
			((SizePanel) exPanel).initialize();
		}
		return exPanel;
	}

	/**
	 * This method initializes meterRadioButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getMeterRadioButton() {
		if (meterRadioButton == null) {
			meterRadioButton = new JRadioButton();
			meterRadioButton.setBounds(new Rectangle(290, 45, 38, 20));
			meterRadioButton.setFont(new Font("Dialog", Font.PLAIN, 12));
			meterRadioButton.setSelected(true);
			meterRadioButton.setText("m");
			meterRadioButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					((SizePanel) exPanel).units = SizePanel.METER;
					((SizePanel) exPanel).changeUnits();
				}
			});
		}
		return meterRadioButton;
	}

	/**
	 * This method initializes feetRadioButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getFeetRadioButton() {
		if (feetRadioButton == null) {
			feetRadioButton = new JRadioButton();
			feetRadioButton.setBounds(new Rectangle(290, 62, 38, 20));
			feetRadioButton.setFont(new Font("Dialog", Font.PLAIN, 12));
			feetRadioButton.setText("ft");
			feetRadioButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					((SizePanel) exPanel).units = SizePanel.FEET;
					((SizePanel) exPanel).changeUnits();
				}
			});
		}
		return feetRadioButton;
	}
}
