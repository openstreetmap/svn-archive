package harbour.dialogs;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JComboBox;
import javax.swing.ImageIcon;

public class HarbourAction {

	private JPanel harbourPanel = null;  //  @jve:decl-index=0:visual-constraint="68,31"
	private JPanel comArea = null;
	private JButton comButton = null;
	private JButton restButton = null;
	private JButton servButton = null;
	private JButton envButton = null;
	private JButton relButton = null;
	private JTextField nameTextField = null;
	private JLabel nameLabel = null;
	private JButton fastbackButton = null;
	private JLabel setLabel = null;
	private JButton backButton = null;
	private JTextField setTextField = null;
	private JButton forButton = null;
	private JButton fastforButton = null;
	private JLabel jLabel = null;
	private JButton queraButton = null;
	private JButton changequeryButton = null;
	private JComboBox typComboBox = null;
	private JLabel typeLabel = null;
	private JComboBox countryComboBox = null;
	private JLabel countryLabel = null;
	private JTextField noTextField = null;
	private JLabel noLabel1 = null;
	private JLabel regLabel = null;
	private JTextField regTextField = null;
	/**
	 * This method initializes harbourPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	public JPanel getHarbourPanel() {
		if (harbourPanel == null) {
			regLabel = new JLabel();
			regLabel.setBounds(new Rectangle(100, 32, 54, 20));
			regLabel.setText("Region:");
			noLabel1 = new JLabel();
			noLabel1.setBounds(new Rectangle(232, 32, 26, 20));
			noLabel1.setText("Nr.:");
			countryLabel = new JLabel();
			countryLabel.setBounds(new Rectangle(2, 32, 40, 20));
			countryLabel.setText("Land:");
			typeLabel = new JLabel();
			typeLabel.setBounds(new Rectangle(289, 5, 43, 20));
			typeLabel.setText("Type:");
			jLabel = new JLabel();
			jLabel.setBounds(new Rectangle(212, 334, 80, 15));
			jLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel.setText("Suche nach:");
			setLabel = new JLabel();
			setLabel.setBounds(new Rectangle(2, 330, 68, 21));
			setLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
			setLabel.setText("Datensatz:");
			nameLabel = new JLabel();
			nameLabel.setBounds(new Rectangle(2, 5, 86, 20));
			nameLabel.setText("Hafenname:");
			harbourPanel = new JPanel();
			harbourPanel.setLayout(null);
			harbourPanel.setSize(new Dimension(400, 360));
			harbourPanel.add(getComArea(), null);
			harbourPanel.add(getComButton(), null);
			harbourPanel.add(getRestButton(), null);
			harbourPanel.add(getServButton(), null);
			harbourPanel.add(getEnvButton(), null);
			harbourPanel.add(getRelButton(), null);
			harbourPanel.add(getNameTextField(), null);
			harbourPanel.add(getFastbackButton(), null);
			harbourPanel.add(nameLabel, null);
			harbourPanel.add(setLabel, null);
			harbourPanel.add(getBackButton(), null);
			harbourPanel.add(getSetTextField(), null);
			harbourPanel.add(getForButton(), null);
			harbourPanel.add(getFastforButton(), null);
			harbourPanel.add(jLabel, null);
			harbourPanel.add(getQueraButton(), null);
			harbourPanel.add(getChangequeryButton(), null);
			harbourPanel.add(getTypComboBox(), null);
			harbourPanel.add(typeLabel, null);
			harbourPanel.add(getCountryComboBox(), null);
			harbourPanel.add(countryLabel, null);
			harbourPanel.add(getNoTextField(), null);
			harbourPanel.add(noLabel1, null);
			harbourPanel.add(regLabel, null);
			harbourPanel.add(getRegTextField(), null);
		}
		return harbourPanel;
	}

	/**
	 * This method initializes comArea	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getComArea() {
		if (comArea == null) {
			comArea = new JPanel();
			comArea.setLayout(null);
			comArea.setBounds(new Rectangle(2, 56, 330, 270));
			comArea.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		}
		return comArea;
	}

	/**
	 * This method initializes comButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getComButton() {
		if (comButton == null) {
			comButton = new JButton();
			comButton.setBounds(new Rectangle(340, 56, 50, 50));
			comButton.setText("");
			comButton.setIcon(new ImageIcon(getClass().getResource("/images/Windrose.png")));
			comButton.setToolTipText("Allgemeines");
		}
		return comButton;
	}

	/**
	 * This method initializes restButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getRestButton() {
		if (restButton == null) {
			restButton = new JButton();
			restButton.setBounds(new Rectangle(340, 111, 50, 50));
			restButton.setIcon(new ImageIcon(getClass().getResource("/images/Zeichen_264.png")));
			restButton.setToolTipText("Einfahrtbeschränkungen");
		}
		return restButton;
	}

	/**
	 * This method initializes servButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getServButton() {
		if (servButton == null) {
			servButton = new JButton();
			servButton.setBounds(new Rectangle(340, 166, 50, 50));
			servButton.setIcon(new ImageIcon(getClass().getResource("/images/Kran.png")));
			servButton.setToolTipText("Dienste");
		}
		return servButton;
	}

	/**
	 * This method initializes envButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getEnvButton() {
		if (envButton == null) {
			envButton = new JButton();
			envButton.setBounds(new Rectangle(340, 221, 50, 50));
			envButton.setToolTipText("Umgebung");
		}
		return envButton;
	}

	/**
	 * This method initializes relButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getRelButton() {
		if (relButton == null) {
			relButton = new JButton();
			relButton.setBounds(new Rectangle(340, 276, 50, 50));
			relButton.setToolTipText("Relationen");
		}
		return relButton;
	}

	/**
	 * This method initializes nameTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getNameTextField() {
		if (nameTextField == null) {
			nameTextField = new JTextField();
			nameTextField.setBounds(new Rectangle(92, 2, 185, 25));
		}
		return nameTextField;
	}

	/**
	 * This method initializes fastbackButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getFastbackButton() {
		if (fastbackButton == null) {
			fastbackButton = new JButton();
			fastbackButton.setBounds(new Rectangle(70, 330, 20, 20));
		}
		return fastbackButton;
	}

	/**
	 * This method initializes backButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBackButton() {
		if (backButton == null) {
			backButton = new JButton();
			backButton.setBounds(new Rectangle(89, 330, 20, 20));
		}
		return backButton;
	}

	/**
	 * This method initializes setTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getSetTextField() {
		if (setTextField == null) {
			setTextField = new JTextField();
			setTextField.setBounds(new Rectangle(109, 329, 60, 23));
			setTextField.setText("");
		}
		return setTextField;
	}

	/**
	 * This method initializes forButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getForButton() {
		if (forButton == null) {
			forButton = new JButton();
			forButton.setBounds(new Rectangle(168, 330, 20, 20));
		}
		return forButton;
	}

	/**
	 * This method initializes fastforButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getFastforButton() {
		if (fastforButton == null) {
			fastforButton = new JButton();
			fastforButton.setBounds(new Rectangle(187, 330, 20, 20));
		}
		return fastforButton;
	}

	/**
	 * This method initializes queraButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getQueraButton() {
		if (queraButton == null) {
			queraButton = new JButton();
			queraButton.setBounds(new Rectangle(292, 330, 80, 20));
			queraButton.setFont(new Font("Dialog", Font.PLAIN, 12));
			queraButton.setToolTipText("");
			queraButton.setText("Hafen");
		}
		return queraButton;
	}

	/**
	 * This method initializes changequeryButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getChangequeryButton() {
		if (changequeryButton == null) {
			changequeryButton = new JButton();
			changequeryButton.setBounds(new Rectangle(371, 330, 20, 20));
		}
		return changequeryButton;
	}

	/**
	 * This method initializes typComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getTypComboBox() {
		if (typComboBox == null) {
			typComboBox = new JComboBox();
			typComboBox.setBounds(new Rectangle(335, 3, 60, 25));
		}
		return typComboBox;
	}

	/**
	 * This method initializes countryComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getCountryComboBox() {
		if (countryComboBox == null) {
			countryComboBox = new JComboBox();
			countryComboBox.setBounds(new Rectangle(44, 29, 50, 25));
		}
		return countryComboBox;
	}

	/**
	 * This method initializes noTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getNoTextField() {
		if (noTextField == null) {
			noTextField = new JTextField();
			noTextField.setBounds(new Rectangle(262, 29, 70, 25));
			noTextField.setText("");
		}
		return noTextField;
	}

	/**
	 * This method initializes regTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getRegTextField() {
		if (regTextField == null) {
			regTextField = new JTextField();
			regTextField.setBounds(new Rectangle(158, 29, 70, 25));
		}
		return regTextField;
	}

}
