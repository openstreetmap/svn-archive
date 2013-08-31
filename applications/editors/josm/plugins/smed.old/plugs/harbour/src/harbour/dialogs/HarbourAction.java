package harbour.dialogs;

import harbour.layers.LayerHarbour;
import harbour.panels.*;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MapView.EditLayerChangeListener;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import javax.swing.SwingConstants;

public class HarbourAction implements PropertyChangeListener, LayerChangeListener, EditLayerChangeListener, ComponentListener {

	private JPanel harbourPanel = null;
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
	private JPanel curPanel = null;
	private PanelSearchPois panelSearchPois = null;
	private static LayerHarbour curLayer = null;
	
	public HarbourAction() {
		Rectangle rect = new Rectangle(2, 56, 330, 270);
		
		panelGeneral= new PanelGeneral();
		panelGeneral.setBounds(rect);
		panelGeneral.setVisible(true);
		curPanel = panelGeneral;
		
		panelLimits = new PanelLimits();
		panelLimits.setBounds(rect);
		panelLimits.setVisible(false);
		
		panelServices = new PanelServices();
		panelServices.setBounds(rect);
		panelServices.setVisible(false);
		
		panelEnv = new PanelEnv();
		panelEnv.setBounds(rect);
		panelEnv.setVisible(false);
		
		panelRelations = new PanelRelations();
		panelRelations.setBounds(rect);
		panelRelations.setVisible(false);
		
		panelSearchPois = new PanelSearchPois();
		panelSearchPois.setBounds(rect);
		panelSearchPois.setVisible(false);
		
		curLayer = new LayerHarbour("Harbour", panelSearchPois);
		panelSearchPois.setLayerHarbour(curLayer);
	}
	
	/**
	 * This method initializes harbourPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	public JPanel getHarbourPanel() {
		if (harbourPanel == null) {
			regLabel = new JLabel();
			regLabel.setBounds(new Rectangle(93, 32, 54, 20));
			regLabel.setText("Region:");
			noLabel1 = new JLabel();
			noLabel1.setBounds(new Rectangle(205, 32, 26, 20));
			noLabel1.setText("Nr.:");
			countryLabel = new JLabel();
			countryLabel.setBounds(new Rectangle(2, 32, 40, 20));
			countryLabel.setText("Land:");
			typeLabel = new JLabel();
			typeLabel.setBounds(new Rectangle(289, 32, 39, 20));
			typeLabel.setText("Type:");
			queryLabel = new JLabel();
			queryLabel.setBounds(new Rectangle(201, 334, 78, 15));
			queryLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
			queryLabel.setText("Suche nach:");
			setLabel = new JLabel();
			setLabel.setBounds(new Rectangle(2, 330, 67, 21));
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
			harbourPanel.add(panelSearchPois, null);
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
			
			comButton.setEnabled(false);
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
					
					comButton.setEnabled(false);
					restButton.setEnabled(true);
					servButton.setEnabled(true);
					envButton.setEnabled(true);
					relButton.setEnabled(true);
					
					Main.map.mapView.removeLayerChangeListener(curLayer);
					Main.main.removeLayer(curLayer);
					curPanel = panelGeneral;
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
					
					comButton.setEnabled(true);
					restButton.setEnabled(false);
					servButton.setEnabled(true);
					envButton.setEnabled(true);
					relButton.setEnabled(true);
					
					Main.map.mapView.removeLayerChangeListener(curLayer);
					Main.main.removeLayer(curLayer);
					curPanel = panelLimits;
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
					
					comButton.setEnabled(true);
					restButton.setEnabled(true);
					servButton.setEnabled(false);
					envButton.setEnabled(true);
					relButton.setEnabled(true);
					
					Main.map.mapView.removeLayerChangeListener(curLayer);
					Main.main.removeLayer(curLayer);
					curPanel = panelServices;
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
					String name = null;
					
					panelGeneral.setVisible(false);
					panelLimits.setVisible(false);
					panelServices.setVisible(false);
					panelRelations.setVisible(false);
					panelEnv.setVisible(true);
					chartButton.setEnabled(true);
					
					comButton.setEnabled(true);
					restButton.setEnabled(true);
					servButton.setEnabled(true);
					envButton.setEnabled(false);
					relButton.setEnabled(true);

					panelSearchPois.iniLayer();
					
					name = panelSearchPois.getActiveLayer();
					if(name != null) curLayer.setName("Harbour - " + name);
					else curLayer.setName("Harbour");

					Layer tmp = Main.map.mapView.getActiveLayer();
					
					Main.map.mapView.addLayerChangeListener(curLayer);
					Main.main.addLayer(curLayer);

					Main.map.mapView.setActiveLayer(tmp);
					
					curPanel = panelEnv;
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
					
					comButton.setEnabled(true);
					restButton.setEnabled(true);
					servButton.setEnabled(true);
					envButton.setEnabled(true);
					relButton.setEnabled(false);
					
					Main.map.mapView.removeLayerChangeListener(curLayer);
					Main.main.removeLayer(curLayer);
					curPanel = panelRelations;
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
			fastbackButton.setBounds(new Rectangle(72, 330, 20, 20));
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
			backButton.setBounds(new Rectangle(90, 330, 20, 20));
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
			setTextField.setBounds(new Rectangle(110, 329, 51, 23));
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
			typComboBox.setBounds(new Rectangle(328, 28, 69, 25));
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
			countryComboBox.setBounds(new Rectangle(42, 29, 50, 25));
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
			noTextField.setBounds(new Rectangle(230, 29, 60, 25));
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
			regTextField.setBounds(new Rectangle(145, 29, 60, 25));
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
			queryComboBox.setBounds(new Rectangle(279, 331, 86, 20));
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
			queryjButton.setBounds(new Rectangle(364, 331, 28, 20));
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
			chartButton.setBounds(new Rectangle(375, 4, 20, 20));
			chartButton.setSelected(false);
			chartButton.setIcon(new ImageIcon(getClass().getResource("/images/oseam_20x20.png")));
			chartButton.setHorizontalTextPosition(SwingConstants.LEADING);
			chartButton.setEnabled(false);
			chartButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					comButton.setEnabled(false);
					if(chartButton.isSelected()) {
						comButton.setEnabled(false);
						restButton.setEnabled(false);
						servButton.setEnabled(false);
						envButton.setEnabled(false);
						relButton.setEnabled(false);
						panelSearchPois.setVisible(true);
						
						if(curPanel == panelEnv) panelEnv.setVisible(false);
						else if(curPanel == panelRelations) panelRelations.setVisible(false);
						
					} else {
						comButton.setEnabled(true);
						restButton.setEnabled(true);
						servButton.setEnabled(true);
						panelSearchPois.setVisible(false);
						
						if(curPanel == panelEnv) {
							panelEnv.setVisible(true);
							relButton.setEnabled(true);
						} else if(curPanel == panelRelations) { 
							panelRelations.setVisible(true);
							envButton.setEnabled(true);
						}
					}
				}
			});
		}
		return chartButton;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(curPanel == panelEnv) System.out.println("Start HarbourAction.propertyChange");
	}

	@Override
	public void componentHidden(ComponentEvent arg0) {
		if(curPanel == panelEnv) System.out.println("Start HarbourAction.componentHidden");
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
		if(curPanel == panelEnv) System.out.println("Start HarbourAction.componentMoved");
	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		if(curPanel == panelEnv) System.out.println("Start HarbourAction.componentResized");
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
		if(curPanel == panelEnv) System.out.println("Start HarbourAction.componentShown");
	}

	@Override
	public void activeLayerChange(Layer arg0, Layer arg1) {
		if(curPanel == panelEnv) System.out.println("Start HarbourAction.activeLayerChange");
	}

	@Override
	public void layerAdded(Layer arg0) {
		if(curPanel == panelEnv) System.out.println("Start HarbourAction.layerAdded");
	}

	@Override
	public void layerRemoved(Layer arg0) {
		if(curPanel == panelServices) System.out.println("Start HarbourAction.layerRemoved");
	}

	@Override
	public void editLayerChanged(OsmDataLayer arg0, OsmDataLayer arg1) {
		System.out.println("Start HarbourAction.editLayerChanged");
	}

}
