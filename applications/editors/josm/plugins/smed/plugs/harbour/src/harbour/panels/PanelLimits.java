package harbour.panels;

import harbour.widgets.LightTile;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import javax.swing.JLabel;
import java.awt.Rectangle;
import javax.swing.JCheckBox;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.ImageIcon;


public class PanelLimits extends JPanel {

	private JLabel limLabel = null;
	private JCheckBox tideCheckBox = null;
	private JCheckBox swellCheckBox1 = null;
	private JCheckBox iceCheckBox = null;
	private JCheckBox otherCheckBox = null;
	private JTextField tideTextField = null;
	private JLabel mLabel = null;
	private JComboBox allComboBox = null;
	private JLabel olLabel = null;
	private JComboBox olComboBox = null;
	private JLabel grLabel = null;
	private JComboBox turnComboBox = null;
	private JLabel turnLabel = null;
	private JButton grButton = null;
	private JComboBox impComboBox = null;
	private JLabel impLabel = null;
	private JComboBox usComboBox = null;
	private JLabel usLabel = null;
	private JComboBox etaComboBox = null;
	private JLabel etaLabel = null;
	private JTextField sizeTextField = null;
	private JLabel sizeLabel = null;
	private JRadioButton sizRadioButton = null;
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
	public PanelLimits() {
		super();
		initialize();
	}
	
	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		oilLabel = new JLabel();
		oilLabel.setBounds(new Rectangle(215, 189, 65, 16));
		oilLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		oilLabel.setText("Oelterminal");
		cargoLabel = new JLabel();
		cargoLabel.setBounds(new Rectangle(215, 165, 80, 16));
		cargoLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		cargoLabel.setText("Frachtanleger");
		tieLabel = new JLabel();
		tieLabel.setBounds(new Rectangle(215, 142, 63, 16));
		tieLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		tieLabel.setText("Ankerplatz");
		chLabel = new JLabel();
		chLabel.setBounds(new Rectangle(215, 119, 50, 16));
		chLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		chLabel.setText("Channel");
		sizeLabel = new JLabel();
        sizeLabel.setBounds(new Rectangle(215, 96, 85, 16));
        sizeLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        sizeLabel.setText("Schiffsgroesse");
        etaLabel = new JLabel();
        etaLabel.setBounds(new Rectangle(55, 189, 80, 16));
        etaLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        etaLabel.setText("ETA Nachricht");
        usLabel = new JLabel();
        usLabel.setBounds(new Rectangle(55, 167, 80, 13));
        usLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        usLabel.setText("US.Vertretung");
        impLabel = new JLabel();
        impLabel.setBounds(new Rectangle(55, 143, 55, 16));
        impLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        impLabel.setText("Zollhafen");
        turnLabel = new JLabel();
        turnLabel.setBounds(new Rectangle(55, 121, 64, 16));
        turnLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        turnLabel.setText("Wendeplatz");
        grLabel = new JLabel();
        grLabel.setBounds(new Rectangle(215, 222, 70, 16));
        grLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        grLabel.setText("Ankergrund");
        olLabel = new JLabel();
        olLabel.setBounds(new Rectangle(55, 96, 90, 16));
        olLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        olLabel.setText("Overhead Limits");
        mLabel = new JLabel();
        mLabel.setBounds(new Rectangle(30, 49, 20, 20));
        mLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        mLabel.setText("m");
        limLabel = new JLabel();
        limLabel.setBounds(new Rectangle(80, 2, 160, 20));
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
        this.add(getAllComboBox(), null);
        this.add(olLabel, null);
        this.add(getOlComboBox(), null);
        this.add(grLabel, null);
        this.add(getTurnComboBox(), null);
        this.add(turnLabel, null);
        this.add(getGrButton(), null);
        this.add(getImpComboBox(), null);
        this.add(impLabel, null);
        this.add(getUsComboBox(), null);
        this.add(usLabel, null);
        this.add(getEtaComboBox(), null);
        this.add(etaLabel, null);
        this.add(getSizeTextField(), null);
        this.add(sizeLabel, null);
        this.add(getSizRadioButton(), null);
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
			swellCheckBox1.setBounds(new Rectangle(86, 23, 76, 20));
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
			allComboBox.setBounds(new Rectangle(125, 70, 200, 20));
		}
		return allComboBox;
	}

	/**
	 * This method initializes olComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getOlComboBox() {
		if (olComboBox == null) {
			olComboBox = new JComboBox();
			olComboBox.setBounds(new Rectangle(2, 95, 50, 20));
		}
		return olComboBox;
	}

	/**
	 * This method initializes turnComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getTurnComboBox() {
		if (turnComboBox == null) {
			turnComboBox = new JComboBox();
			turnComboBox.setBounds(new Rectangle(2, 118, 50, 20));
		}
		return turnComboBox;
	}

	/**
	 * This method initializes grButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getGrButton() {
		if (grButton == null) {
			grButton = new LightTile("");
			grButton.setBounds(new Rectangle(164, 220, 50, 20));
			grButton.setIcon(new ImageIcon(getClass().getResource("/images/anker.png")));
		}
		return grButton;
	}

	/**
	 * This method initializes impComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getImpComboBox() {
		if (impComboBox == null) {
			impComboBox = new JComboBox();
			impComboBox.setBounds(new Rectangle(2, 141, 50, 20));
		}
		return impComboBox;
	}

	/**
	 * This method initializes usComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getUsComboBox() {
		if (usComboBox == null) {
			usComboBox = new JComboBox();
			usComboBox.setBounds(new Rectangle(2, 164, 50, 20));
		}
		return usComboBox;
	}

	/**
	 * This method initializes etaComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getEtaComboBox() {
		if (etaComboBox == null) {
			etaComboBox = new JComboBox();
			etaComboBox.setBounds(new Rectangle(2, 187, 50, 20));
		}
		return etaComboBox;
	}

	/**
	 * This method initializes sizeTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getSizeTextField() {
		if (sizeTextField == null) {
			sizeTextField = new JTextField();
			sizeTextField.setBounds(new Rectangle(150, 95, 65, 20));
		}
		return sizeTextField;
	}

	/**
	 * This method initializes sizRadioButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getSizRadioButton() {
		if (sizRadioButton == null) {
			sizRadioButton = new JRadioButton();
			sizRadioButton.setBounds(new Rectangle(305, 95, 20, 20));
		}
		return sizRadioButton;
	}

	/**
	 * This method initializes chTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getChTextField() {
		if (chTextField == null) {
			chTextField = new JTextField();
			chTextField.setBounds(new Rectangle(150, 118, 65, 20));
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
			chRadioButton.setBounds(new Rectangle(305, 118, 20, 20));
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
			tieTextField.setBounds(new Rectangle(150, 141, 65, 20));
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
			tieRadioButton.setBounds(new Rectangle(305, 141, 20, 20));
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
			cargoTextField.setBounds(new Rectangle(150, 164, 65, 20));
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
			cargoRadioButton.setBounds(new Rectangle(305, 164, 20, 20));
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
			oilTextField.setBounds(new Rectangle(150, 187, 65, 20));
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
			oilRadioButton.setBounds(new Rectangle(305, 188, 20, 20));
		}
		return oilRadioButton;
	}
}
