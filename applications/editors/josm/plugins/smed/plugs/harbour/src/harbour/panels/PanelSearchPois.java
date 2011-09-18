package harbour.panels;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JComboBox;
import javax.swing.table.TableModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

import harbour.layers.LayerHarbour;
import harbour.models.SearchTableModel;

public class PanelSearchPois extends JPanel implements PropertyChangeListener{

	private static final long serialVersionUID = 1L;
	private JLabel radLabel 			= null;
	private JTextField radTextField 	= null;
	private JLabel unitLabel 			= null;
	private JButton searchButton 		= null;
	private JScrollPane jScrollPane 	= null;
	private JTable searchTable 			= null;
	private JLabel layerLabel 			= null;
	private JComboBox layerComboBox 	= null;
	private String activeLayer 			= null;
	private LayerHarbour layerHarbour 	= null;  //  @jve:decl-index=0:
	private List<Layer> layers 			= new ArrayList<Layer>();  //  @jve:decl-index=0:
	private JButton setButton 			= null;
	private DataSet pois 				= null;
	private DataSet activeDS			= null;  //  @jve:decl-index=0:
	
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
		unitLabel.setText("km");
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
		this.add(getSearchButton(), null);
		this.add(getJScrollPane(), null);
		this.add(layerLabel, null);
		this.add(getLayerComboBox(), null);
		this.add(getSetButton(), null);
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
			radTextField.addFocusListener(new FocusListener() {

				@Override
				public void focusGained(FocusEvent arg0) {
					// nothing to do yet
				}

				@Override
				public void focusLost(FocusEvent arg0) {
					double r = Double.parseDouble(radTextField.getText());

					layerHarbour.setRadius(r);
				}
				
			});
			
			radTextField.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyTyped(java.awt.event.KeyEvent e) {
					if(e.getKeyChar() == KeyEvent.VK_ENTER) {
						double r = Double.parseDouble(radTextField.getText());

						layerHarbour.setRadius(r);
					}
				}
			});
		}
		return radTextField;
	}

	/**
	 * This method initializes searchButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSearchButton() {
		if (searchButton == null) {
			searchButton = new JButton();
			searchButton.setBounds(new Rectangle(240, 240, 80, 20));
			searchButton.setFont(new Font("Dialog", Font.PLAIN, 12));
			searchButton.setText("Suche");
			searchButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					searchPois();
				}
			});
		}

		return searchButton;
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
			jScrollPane.setViewportView(getSearchTable());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes searchTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getSearchTable() {
		if (searchTable == null) {
			searchTable = new JTable();
			searchTable.setModel(new SearchTableModel());
		}
		return searchTable;
	}

	/**
	 * This method initializes layerComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	public JComboBox getLayerComboBox() {
		if (layerComboBox == null) {
			layerComboBox = new JComboBox();
			layerComboBox.setBounds(new Rectangle(70, 5, 250, 20));
			layerComboBox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int i = layerComboBox.getSelectedIndex();
					
					if(i >= 0) { 
						Main.map.mapView.setActiveLayer(layers.get(i));
						activeDS = Main.main.getCurrentDataSet();
					}
				}
			});
		}
		return layerComboBox;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		Object src = event.getSource();
		
		System.out.println("Hello, world");
		
		if (src instanceof MapView) {
			MapView mapView = (MapView) src;
			
			for (Layer l: mapView.getAllLayers()) {
				if(l instanceof OsmDataLayer) layerComboBox.addItem(l.getName());
			}
		}

	}

	public void iniLayer() {
		Layer a = Main.map.mapView.getActiveLayer();
		String name = null;
		 
		activeDS = Main.main.getCurrentDataSet();
		
		layerComboBox.removeAllItems();
		layers.clear();

		for (Layer l: Main.map.mapView.getAllLayers()) {
			if(l instanceof OsmDataLayer) {
				name = l.getName();
				layers.add(l);
				
				layerComboBox.addItem(name);
				if(l.equals(a)) activeLayer = name;
			}
		}
	}

	public String getActiveLayer() { return activeLayer; }
	public void setLayerHarbour(LayerHarbour layer) { layerHarbour = layer; }

	/**
	 * This method initializes setButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSetButton() {
		if (setButton == null) {
			setButton = new JButton();
			setButton.setBounds(new Rectangle(160, 240, 55, 20));
			setButton.setFont(new Font("Dialog", Font.PLAIN, 12));
			setButton.setText("Set");
			setButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					DataSet ds = Main.main.getCurrentDataSet();
					Collection<Node> selection = ds.getSelectedNodes();
					int nodes = selection.size();

					if(nodes != 0) {
						Iterator<Node> it = selection.iterator();
						Node node = it.next();
						
						layerHarbour.setCenter(node.getCoor());
					}
				}
			});
		}
		return setButton;
	}

	public void searchPois() {
		// DataSet data = Main.main.getCurrentDataSet();
		Collection<Node> nodes = activeDS.getNodes();
		SearchTableModel searchModel = (SearchTableModel) searchTable.getModel();

		pois.clear();
		Main.map.mapView.setActiveLayer(layerHarbour);
		
		for(Node n : nodes) {
			if(layerHarbour.isNodeinCircle(n)) {
				Map<String, String> keys = n.getKeys();
				
				for(int i = 0; i < searchTable.getRowCount(); i++){
					if(searchModel.isWanted(i)) {
						String key = searchModel.getKey(i);
						
						if(keys.containsKey(key)) {
							String type = keys.get(key);
							if(type.equals(searchModel.getValue(i))) {
								Node nc = new Node(1);
								
								nc.cloneFrom(n);
								pois.addPrimitive(nc);
							}
						}
					}
				}
			}
		}
		
		layerHarbour.setChanged(true);
		Main.map.repaint();
	}
	
	public void setPois(DataSet pois) { this.pois = pois; }
	
}
