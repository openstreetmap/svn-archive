package harbour.dialogs;

import harbour.panels.*;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JComboBox;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

public class HarbourAction {

	private JPanel harbourPanel = null;  //  @jve:decl-index=0:visual-constraint="68,31"
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
	private JLabel queryLabel = null;
	private JComboBox typComboBox = null;
	private JLabel typeLabel = null;
	private JComboBox countryComboBox = null;
	private JLabel countryLabel = null;
	private JTextField noTextField = null;
	private JLabel noLabel1 = null;
	private JLabel regLabel = null;
	private JTextField regTextField = null;
	private PanelGeneral panelGeneral = null;
	private PanelLimits panelLimits = null;
	private PanelServices panelServices = null;
	private PanelEnv panelEnv = null;
	private PanelRelations panelRelations = null;
	private JComboBox queryComboBox = null;
	private JButton queryjButton = null;
	private JToggleButton chartButton = null;
	public HarbourAction() {
		panelGeneral= new PanelGeneral();
		panelGeneral.setBounds(new Rectangle(2, 56, 330, 270));
		panelGeneral.setVisible(true);
		
		panelLimits = new PanelLimits();
		panelLimits.setBounds(new Rectangle(2, 56, 330, 270));
		panelLimits.setVisible(false);
		
		panelServices = new PanelServices();
		panelServices.setBounds(new Rectangle(2, 56, 330, 270));
		panelServices.setVisible(false);
		
		panelEnv = new PanelEnv();
		panelEnv.setBounds(new Rectangle(2, 56, 330, 270));
		panelEnv.setVisible(false);
		
		panelRelations = new PanelRelations();
		panelRelations.setBounds(new Rectangle(2, 56, 330, 270));
		panelRelations.setVisible(false);
	}
	
	/**
	 * This method initializes harbourPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	public JPanel getHarbourPanel() {
		if (harbourPanel == null) {
			regLabel = new JLabel();
			regLabel.setBounds(new Rectangle(90, 32, 45, 20));
			regLabel.setText("Region:");
			noLabel1 = new JLabel();
			noLabel1.setBounds(new Rectangle(200, 32, 26, 20));
			noLabel1.setText("Nr.:");
			countryLabel = new JLabel();
			countryLabel.setBounds(new Rectangle(2, 32, 32, 20));
			countryLabel.setText("Land:");
			typeLabel = new JLabel();
			typeLabel.setBounds(new Rectangle(289, 32, 30, 20));
			typeLabel.setText("Type:");
			queryLabel = new JLabel();
			queryLabel.setBounds(new Rectangle(201, 334, 72, 15));
			queryLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
			queryLabel.setText("Suche nach:");
			setLabel = new JLabel();
			setLabel.setBounds(new Rectangle(2, 330, 60, 21));
			setLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
			setLabel.setText("Datensatz:");
			nameLabel = new JLabel();
			nameLabel.setBounds(new Rectangle(2, 5, 86, 20));
			nameLabel.setText("Hafenname:");
			harbourPanel = new JPanel();
			harbourPanel.setLayout(null);
			harbourPanel.setSize(new Dimension(400, 360));
			harbourPanel.add(panelGeneral,    null);
			harbourPanel.add(panelLimits,     null);
			harbourPanel.add(panelServices,   null);
			harbourPanel.add(panelEnv,        null);
			harbourPanel.add(panelRelations,  null);
			harbourPanel.add(getComButton(),  null);
			harbourPanel.add(getRestButton(), null);
			harbourPanel.add(getServButton(), null);
			harbourPanel.add(getEnvButton(),  null);
			harbourPanel.add(getRelButton(),  null);
			harbourPanel.add(getNameTextField(), null);
			harbourPanel.add(getFastbackButton(), null);
			harbourPanel.add(nameLabel, null);
			harbourPanel.add(setLabel, null);
			harbourPanel.add(getBackButton(), null);
			harbourPanel.add(getSetTextField(), null);
			harbourPanel.add(getForButton(), null);
			harbourPanel.add(getFastforButton(), null);
			harbourPanel.add(queryLabel, null);
			harbourPanel.add(getTypComboBox(), null);
			harbourPanel.add(typeLabel, null);
			harbourPanel.add(getCountryComboBox(), null);
			harbourPanel.add(countryLabel, null);
			harbourPanel.add(getNoTextField(), null);
			harbourPanel.add(noLabel1, null);
			harbourPanel.add(regLabel, null);
			harbourPanel.add(getRegTextField(), null);
			harbourPanel.add(getQueryComboBox(), null);
			harbourPanel.add(getQueryjButton(), null);
			harbourPanel.add(getChartButton(), null);
		}
		return harbourPanel;
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
			comButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					panelLimits.setVisible(false);
					panelServices.setVisible(false);
					panelEnv.setVisible(false);
					panelRelations.setVisible(false);
					panelGeneral.setVisible(true);
					chartButton.setEnabled(false);
				}
			});
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
			restButton.setIcon(new ImageIcon(getClass().getResource("/images/Schranken.png")));
			restButton.setToolTipText("Einfahrtbeschränkungen");
			restButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					panelGeneral.setVisible(false);
					panelServices.setVisible(false);
					panelEnv.setVisible(false);
					panelRelations.setVisible(false);
					panelLimits.setVisible(true);
					chartButton.setEnabled(false);
				}
			});
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
			servButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					panelGeneral.setVisible(false);
					panelLimits.setVisible(false);
					panelEnv.setVisible(false);
					panelRelations.setVisible(false);
					panelServices.setVisible(true);
					chartButton.setEnabled(false);
				}
			});
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
			envButton.setIcon(new ImageIcon(getClass().getResource("/images/Env.png")));
			envButton.setToolTipText("Umgebung");
			envButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					panelGeneral.setVisible(false);
					panelLimits.setVisible(false);
					panelServices.setVisible(false);
					panelRelations.setVisible(false);
					panelEnv.setVisible(true);
					chartButton.setEnabled(true);
				}
			});
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
			relButton.setIcon(new ImageIcon(getClass().getResource("/images/Relationen.png")));
			relButton.setToolTipText("Relationen");
			relButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					panelGeneral.setVisible(false);
					panelLimits.setVisible(false);
					panelServices.setVisible(false);
					panelEnv.setVisible(false);
					panelRelations.setVisible(true);
					chartButton.setEnabled(true);
				}
			});
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
			nameTextField.setBounds(new Rectangle(88, 2, 196, 25));
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
			fastbackButton.setBounds(new Rectangle(62, 330, 20, 20));
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
			backButton.setBounds(new Rectangle(81, 330, 20, 20));
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
			setTextField.setBounds(new Rectangle(101, 329, 60, 23));
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
			forButton.setBounds(new Rectangle(160, 330, 20, 20));
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
			fastforButton.setBounds(new Rectangle(179, 330, 20, 20));
		}
		return fastforButton;
	}

	/**
	 * This method initializes typComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getTypComboBox() {
		if (typComboBox == null) {
			typComboBox = new JComboBox();
			typComboBox.setBounds(new Rectangle(323, 28, 72, 25));
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
			countryComboBox.setBounds(new Rectangle(36, 29, 50, 25));
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
			noTextField.setBounds(new Rectangle(225, 29, 60, 25));
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
			regTextField.setBounds(new Rectangle(135, 29, 60, 25));
		}
		return regTextField;
	}

	/**
	 * This method initializes queryComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getQueryComboBox() {
		if (queryComboBox == null) {
			queryComboBox = new JComboBox();
			queryComboBox.addItem("Hafen");
			queryComboBox.addItem("Land");
			queryComboBox.addItem("Nummer");
			queryComboBox.addItem("Region");
			queryComboBox.addItem("Type");
			queryComboBox.addItem("Query");
			queryComboBox.setBounds(new Rectangle(272, 331, 86, 20));
		}
		return queryComboBox;
	}

	/**
	 * This method initializes queryjButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getQueryjButton() {
		if (queryjButton == null) {
			queryjButton = new JButton();
			queryjButton.setBounds(new Rectangle(364, 330, 28, 20));
		}
		return queryjButton;
	}

	/**
	 * This method initializes chartButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JToggleButton getChartButton() {
		if (chartButton == null) {
			chartButton = new JToggleButton();
			chartButton.setBounds(new Rectangle(365, 5, 28, 18));
			chartButton.setEnabled(false);
		}
		return chartButton;
	}

}
