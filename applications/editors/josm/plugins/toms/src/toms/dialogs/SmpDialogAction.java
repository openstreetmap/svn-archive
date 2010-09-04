//License: GPL. For details, see LICENSE file.
// Copyright (c) 2009 / 2010 by Werner Koenig & Malcolm Herring

package toms.dialogs;

// necessary adaption to my environment 04.09.2010 kg

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.tools.Shortcut;

import toms.seamarks.SeaMark;
import toms.seamarks.buoys.Buoy;
import toms.seamarks.buoys.BuoyCard;
import toms.seamarks.buoys.BuoyLat;
import toms.seamarks.buoys.BuoySaw;
import toms.seamarks.buoys.BuoyUkn;
import toms.seamarks.buoys.BuoyIsol;
import toms.seamarks.buoys.BuoySpec;
import toms.seamarks.buoys.BuoyNota;

public class SmpDialogAction extends JosmAction {
	private static final long serialVersionUID = -2976230949744302905L;

	/**
	 * lokale Variable, private
	 */
	private SmpDialogAction dia = null; // Variable für den Handle von
																			// SmpDialogAction
	private Buoy buoy = null; // Variable für Objekte des Typs "Tonne" //
														// @jve:decl-index=0:
	private boolean isOpen = false; // zeigt den Status des Dialogs an
	private Node onode = null; // gemerkter Knoten
	private Buoy obuoy = null; // gemerkte Tonne // @jve:decl-index=0:
	private JMenuItem SmpItem = null; // Info über item in der Werkzeugleiste
	private String smt = ""; // value vom key "seamark:type" // @jve:decl-index=0:
	private String smb = ""; // value vom key "seamark" // @jve:decl-index=0:
	private Collection<? extends OsmPrimitive> Selection = null; // @jve:decl-index=0:
	private OsmPrimitive SelNode = null;
	private String Os = ""; // @jve:decl-index=0:
	private String UserHome = ""; // @jve:decl-index=0:

	// SelectionChangedListner der in die Eventqueue von josm eingehängt wird
	private SelectionChangedListener SmpListener = new SelectionChangedListener() {
		public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
			Node node;
			Selection = newSelection;

			// System.out.println("hello");
			for (OsmPrimitive osm : Selection) {
				if (osm instanceof Node) {
					node = (Node) osm;
					if (Selection.size() == 1)
						// Absicherung gegen Doppelevents
						if (node.compareTo(SelNode) != 0) {
							SelNode = node;
							parseSeaMark();
							buoy.paintSign();
						}
				}
			}

			Selection = null;

		}
	};

	/**
	 * lokale Variable der Maske
	 */
	private JDialog dM01SeaMap = null;
	private JPanel pM01SeaMap = null;
	private JLabel lM01Head = null;
	private JLabel lM01Region = null;
	private JLabel lM02Region = null;
	public ButtonGroup bgM01Region = null;
	public JRadioButton rbM01RegionA = null;
	public JRadioButton rbM01RegionB = null;
	public JLabel lM01Icon01 = null;
	public JLabel lM01Icon02 = null;
	public JLabel lM01FireMark = null;
	private JLabel lM01TypeOfMark = null;
	public JComboBox cbM01TypeOfMark = null;
	private JLabel lM01CatOfMark = null;
	public JComboBox cbM01CatOfMark = null;
	private JLabel lM01StyleOfMark = null;
	public JComboBox cbM01StyleOfMark = null;
	private JLabel lM01Name = null;
	public JTextField tfM01Name = null;
	private JLabel lM01Props02 = null;
	public JCheckBox cM01TopMark = null;
	public JComboBox cbM01TopMark = null;
	public JCheckBox cM01Radar = null;
	public JCheckBox cM01Racon = null;
	public JComboBox cbM01Racon = null;
	public JTextField tfM01Racon = null;
	private JLabel lM01Racon = null;
	public JCheckBox cM01Fog = null;
	public JComboBox cbM01Fog = null;
	private JLabel lM01FogGroup = null;
	public JTextField tfM01FogGroup = null;
	private JLabel lM01FogPeriod = null;
	public JTextField tfM01FogPeriod = null;
	public JCheckBox cM01Fired = null;
	public ButtonGroup bgM01Fired = null;
	public JRadioButton rbM01Fired1 = null;
	public JRadioButton rbM01FiredN = null;
	private JLabel lM01Kennung = null;
	public JComboBox cbM01Kennung = null;
	private JLabel lM01Sector = null;
	public JComboBox cbM01Sector = null;
	private JLabel lM01Group = null;
	public JTextField tfM01Group = null;
	private JLabel lM01RepeatTime = null;
	public JTextField tfM01RepeatTime = null;
	private JLabel lM01Height = null;
	private JLabel lM01Bearing = null;
	public JTextField tfM01Bearing = null;
	public JTextField tfM02Bearing = null;
	public JTextField tfM01Radius = null;
	public JTextField tfM01Height = null;
	private JLabel lM01Range = null;
	public JTextField tfM01Range = null;
	public JButton bM01Save = null;
	public JButton bM01Close = null;
	public JCheckBox cM01IconVisible = null;
	public JTextField sM01StatusBar = null;

	public JMenuItem getSmpItem() {
		return SmpItem;
	}

	public void setSmpItem(JMenuItem smpItem) {
		SmpItem = smpItem;
	}

	public boolean isOpen() {
		return isOpen;
	}

	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}

	public String getOs() {
		return Os;
	}

	public void setOs(String os) {
		Os = os;
	}

	public String getUserHome() {
		return UserHome;
	}

	public void setUserHome(String userHome) {
		UserHome = userHome;
	}

	public SmpDialogAction() {
		super(tr("Edit OpenSeaMap"), "Smp", tr("Seamark Editor"), Shortcut
				.registerShortcut("tools:Semarks",
						tr("Tool: {0}", tr("Seamark Editor")), KeyEvent.VK_S,
						Shortcut.GROUP_EDIT, Shortcut.SHIFT_DEFAULT), true);

		dia = this;
		String str = Main.pref.get("mappaint.style.sources");
		if (!str.contains("dev.openseamap.org")) {
			if (!str.equals(""))
				str += new String(new char[] { 0x1e });
			Main.pref.put("mappaint.style.sources", str
					+ "http://dev.openseamap.org/josm/seamark_styles.xml");
		}
		str = Main.pref.get("color.background");
		if (str.equals("#000000") || str.equals(""))
			Main.pref.put("color.background", "#606060");
	}

	public void CloseDialog() {
		onode = null;
		DataSet.selListeners.remove(SmpListener);
		// DataSet.removeSelectionListener(SmpListener);
		Selection = null;

		if (isOpen)
			dM01SeaMap.dispose();
		isOpen = false;

	}

	public void actionPerformed(ActionEvent e) {

		/*
		 * int option = JOptionPane.showConfirmDialog(Main.parent,
		 * tr("THIS IS EXPERIMENTAL. Save your work and verify before uploading.\n"
		 * + "Are you really sure to continue?"),
		 * tr("Please abort if you are not sure"), JOptionPane.YES_NO_OPTION,
		 * JOptionPane.WARNING_MESSAGE);
		 * 
		 * if (option != JOptionPane.YES_OPTION) { return; }
		 */

		onode = null;
		obuoy = null;

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JDialog dialog = getDM01SeaMap();

				if (SmpItem == null) {
				}
				dialog.setVisible(true);
			}
		});

		setOpen(true);

		if (SmpItem == null) {
			return;
		}
		SmpItem.setEnabled(false);

		// Ausprobe: Möglichkeit der Benachrichtigung, wenn etwas neu
		// selektiert wird (ueber SelectionChangedListener)
		// private Collection<? extends OsmPrimitive> sel;
		// siehe org.openstreetmap.josm.plugins.osb -> OsbLayer.java
		// Einhängen des Listeners in die Eventqueue von josm
		DataSet.selListeners.add(SmpListener);
		// DataSet.addSelectionListener(SmpListener);
	}

	private void PicRebuild() {

		DataSet ds = Main.main.getCurrentDataSet();

		if (obuoy == null) {
			return;
		}

		Node n = obuoy.getNode();

		if (n != null) {
			Command c;

			if (smb != "") {

				c = new ChangePropertyCommand(n, "seamark", smb);
				c.executeCommand();
				ds.fireSelectionChanged();

				smb = "";
			}

			if (smt != "") {

				c = new ChangePropertyCommand(n, "seamark:type", smt);
				c.executeCommand();
				ds.fireSelectionChanged();

				smt = "";
			}
		}

		obuoy = null;

	}

	private void parseSeaMark() {

		int nodes = 0;
		Node node = null;
		Collection<Node> selection = null;
		Map<String, String> keys;
		DataSet ds;

		ds = Main.main.getCurrentDataSet();

		if (ds == null) {
			buoy = new BuoyUkn(this, "active layer contains no OSM data");
			buoy.setNode(null);
			return;
		}

		selection = ds.getSelectedNodes();
		nodes = selection.size();

		if (nodes == 0) {
			buoy = new BuoyUkn(this, "Please select a node");
			buoy.setNode(null);
			return;
		}

		if (nodes > 1) {
			buoy = new BuoyUkn(this, "Please select only one node");
			buoy.setNode(null);
			return;
		}

		Iterator<Node> it = selection.iterator();
		node = it.next();

		if (onode != null)
			if (node.equals(onode))
				return;

		// Knoten wurde gewechselt -> die alten tags (benutzt zum Ausblenden der
		// Pictogramme) wiederherstellen
		if (obuoy != null)
			PicRebuild();

		onode = node;

		cM01IconVisible.setEnabled(true);
		cM01IconVisible.setIcon(new ImageIcon(getClass().getResource(
				"/images/Auge.png")));

		cbM01TypeOfMark.setEnabled(true);

		// Soweit das Vorspiel. Ab hier beginnt das Parsen
		String type = "";
		String str = "";

		keys = node.getKeys();

		// vorsorglich den Namen holen und verwenden, wenn es ein
		// Seezeichen ist. Name kann durch die weiteren Tags ueber-
		// schrieben werden

		if (keys.containsKey("seamark:type"))
			type = keys.get("seamark:type");

		if (type.equals("buoy_lateral") || type.equals("beacon_lateral")
				|| keys.containsKey("seamark:buoy_lateral:category")
				|| keys.containsKey("seamark:buoy_lateral:shape")
				|| keys.containsKey("seamark:buoy_lateral:colour")
				|| keys.containsKey("seamark:beacon_lateral:category")
				|| keys.containsKey("seamark:beacon_lateral:shape")
				|| keys.containsKey("seamark:beacon_lateral:colour")) {
			buoy = new BuoyLat(this, node);
			return;

		} else if (type.equals("buoy_cardinal") || type.equals("beacon_cardinal")
				|| keys.containsKey("seamark:buoy_cardinal:category")
				|| keys.containsKey("seamark:buoy_cardinal:shape")
				|| keys.containsKey("seamark:buoy_cardinal:colour")
				|| keys.containsKey("seamark:beacon_cardinal:category")
				|| keys.containsKey("seamark:beacon_cardinal:shape")
				|| keys.containsKey("seamark:beacon_cardinal:colour")) {
			buoy = new BuoyCard(this, node);
			return;

		} else if (type.equals("buoy_safe_water")
				|| type.equals("beacon_safe_water")
				|| keys.containsKey("seamark:buoy_safe_water:shape")
				|| keys.containsKey("seamark:buoy_safe_water:colour")
				|| keys.containsKey("seamark:beacon_safe_water:shape")
				|| keys.containsKey("seamark:beacon_safe_water:colour")) {
			buoy = new BuoySaw(this, node);
			return;

		} else if (type.equals("buoy_special_purpose")
				|| type.equals("beacon_special_purpose")
				|| keys.containsKey("seamark:buoy_special_purpose:shape")
				|| keys.containsKey("seamark:buoy_special_purpose:colour")
				|| keys.containsKey("seamark:beacon_special_purpose:shape")
				|| keys.containsKey("seamark:beacon_special_purpose:colour")) {
			buoy = new BuoySpec(this, node);
			return;

		} else if (type.equals("buoy_isolated_danger")
				|| type.equals("beacon_isolated_danger")
				|| keys.containsKey("seamark:buoy_isolated_danger:shape")
				|| keys.containsKey("seamark:buoy_isolated_danger:colour")
				|| keys.containsKey("seamark:beacon_isolated_danger:shape")
				|| keys.containsKey("seamark:beacon_isolated_danger:colour")) {
			buoy = new BuoyIsol(this, node);
			return;

		} else if (type.equals("light_float")) {
			if (keys.containsKey("seamark:light_float:colour")) {
				str = keys.get("seamark:light_float:colour");
				if (str.equals("red") || str.equals("green")
						|| str.equals("red;green;red") || str.equals("green;red;green")) {
					buoy = new BuoyLat(this, node);
					return;
				} else if (str.equals("black;yellow")
						|| str.equals("black;yellow;black") || str.equals("yellow;black")
						|| str.equals("yellow;black;yellow")) {
					buoy = new BuoyCard(this, node);
					return;
				} else if (str.equals("black;red;black")) {
					buoy = new BuoyIsol(this, node);
					return;
				} else if (str.equals("red;white")) {
					buoy = new BuoySaw(this, node);
					return;
				} else if (str.equals("yellow")) {
					buoy = new BuoySaw(this, node);
					return;
				} else {
					buoy = new BuoyUkn(this, "Parse-Error: Invalid colour");
					buoy.setNode(node);
					return;
				}
			} else if (keys.containsKey("seamark:light_float:topmark:shape")) {
				str = keys.get("seamark:light_float:topmark:shape");
				if (str.equals("cylinder") || str.equals("cone, point up")) {
					buoy = new BuoyLat(this, node);
					return;
				}
			} else if (keys.containsKey("seamark:light_float:topmark:colour")) {
				str = keys.get("seamark:light_float:topmark:colour");
				if (str.equals("red") || str.equals("green")) {
					buoy = new BuoyLat(this, node);
					return;
				}
			}
		}

		buoy = new BuoyUkn(this, "Seamark not set");
		buoy.setNode(node);
		return;
	}

	private JDialog getDM01SeaMap() {

		if (dM01SeaMap == null) {
			dM01SeaMap = new JDialog();
			dM01SeaMap.setSize(new Dimension(400, 400));
			dM01SeaMap.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dM01SeaMap.setModal(false);
			dM01SeaMap.setResizable(false);
			dM01SeaMap.setContentPane(getPM01SeaMap());
			dM01SeaMap.setTitle("Seamark Editor");
			dM01SeaMap.setVisible(false);
			dM01SeaMap.setAlwaysOnTop(true);
			dM01SeaMap.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {

					// Pictogramme wiederherstellen und aufraeumen
					if (obuoy != null)
						PicRebuild();
					// Deaktivierung des Listeners
					DataSet.selListeners.remove(SmpListener);
					// DataSet.removeSelectionListener(SmpListener);
					Selection = null;

					SmpItem.setEnabled(true);
				}

				public void windowActivated(WindowEvent arg0) {
					parseSeaMark();
					buoy.paintSign();
				}
			});
		}
		return dM01SeaMap;
	}

	private JPanel getPM01SeaMap() {
		if (pM01SeaMap == null) {

			lM01Icon01 = new JLabel();
			lM01Icon01.setBounds(new Rectangle(210, 30, 160, 200));
			lM01Icon01.setIcon(null);
			lM01Icon01.setText("");

			lM01Icon02 = new JLabel();
			lM01Icon02.setBounds(new Rectangle(210, 30, 160, 200));
			lM01Icon02.setIcon(null);
			lM01Icon02.setText("");

			lM01FireMark = new JLabel();
			lM01FireMark.setBounds(new Rectangle(305, 90, 90, 20));
			lM01FireMark.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01FireMark.setText("");

			lM01Head = new JLabel();
			lM01Head.setBounds(new Rectangle(5, 3, 316, 16));
			lM01Head.setText("Seamark Properties");

			lM01Region = new JLabel();
			lM01Region.setBounds(new Rectangle(220, 7, 120, 16));
			lM01Region.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01Region.setText("Region:");

			lM02Region = new JLabel();
			lM02Region.setBounds(new Rectangle(270, 7, 120, 16));
			lM02Region.setFont(new Font("Dialog", Font.BOLD, 12));
			lM02Region.setText("IALA");

			lM01TypeOfMark = new JLabel();
			lM01TypeOfMark.setBounds(new Rectangle(5, 28, 120, 16));
			lM01TypeOfMark.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01TypeOfMark.setText("Type:");

			lM01CatOfMark = new JLabel();
			lM01CatOfMark.setBounds(new Rectangle(5, 58, 120, 16));
			lM01CatOfMark.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01CatOfMark.setText("Category:");

			lM01StyleOfMark = new JLabel();
			lM01StyleOfMark.setBounds(new Rectangle(5, 88, 148, 16));
			lM01StyleOfMark.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01StyleOfMark.setText("Shape:");

			lM01Name = new JLabel();
			lM01Name.setBounds(new Rectangle(5, 120, 82, 16));
			lM01Name.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01Name.setText("Name:");

			lM01Props02 = new JLabel();
			lM01Props02.setBounds(new Rectangle(5, 150, 90, 16));
			lM01Props02.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01Props02.setText("Other Features:");

			lM01Racon = new JLabel();
			lM01Racon.setBounds(new Rectangle(335, 195, 65, 20));
			lM01Racon.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01Racon.setText("(          )");

			lM01FogGroup = new JLabel();
			lM01FogGroup.setBounds(new Rectangle(190, 220, 100, 20));
			lM01FogGroup.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01FogGroup.setText("Group: (          )");

			lM01FogPeriod = new JLabel();
			lM01FogPeriod.setBounds(new Rectangle(300, 220, 100, 20));
			lM01FogPeriod.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01FogPeriod.setText("Period:          s");

			lM01Kennung = new JLabel();
			lM01Kennung.setBounds(new Rectangle(240, 245, 60, 20));
			lM01Kennung.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01Kennung.setText("Character:");

			lM01Sector = new JLabel();
			lM01Sector.setBounds(new Rectangle(80, 270, 180, 20));
			lM01Sector.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01Sector.setText("Sector:");

			lM01Group = new JLabel();
			lM01Group.setBounds(new Rectangle(190, 270, 100, 20));
			lM01Group.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01Group.setText("Group: (          )");

			lM01RepeatTime = new JLabel();
			lM01RepeatTime.setBounds(new Rectangle(300, 270, 100, 20));
			lM01RepeatTime.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01RepeatTime.setText("Period:          s");

			lM01Bearing = new JLabel();
			lM01Bearing.setBounds(new Rectangle(35, 295, 180, 20));
			lM01Bearing.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01Bearing.setText("         �-        �, r:");

			lM01Height = new JLabel();
			lM01Height.setBounds(new Rectangle(190, 295, 100, 20));
			lM01Height.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01Height.setText("Height:           m");

			lM01Range = new JLabel();
			lM01Range.setBounds(new Rectangle(300, 295, 100, 20));
			lM01Range.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01Range.setText("Range:          M");

			rbM01RegionA = new JRadioButton("-A", Main.pref.get("tomsplugin.IALA")
					.equals("A"));
			rbM01RegionA.setBounds(new Rectangle(305, 0, 50, 30));
			rbM01RegionB = new JRadioButton("-B", Main.pref.get("tomsplugin.IALA")
					.equals("B"));
			rbM01RegionB.setBounds(new Rectangle(352, 0, 50, 30));
			bgM01Region = new ButtonGroup();
			bgM01Region.add(rbM01RegionA);
			bgM01Region.add(rbM01RegionB);

			ActionListener alM01Region = new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (buoy instanceof BuoyLat) {
						buoy.setRegion(rbM01RegionB.isSelected());
						buoy.setLightColour();
						buoy.paintSign();
					}
				}
			};
			rbM01RegionA.addActionListener(alM01Region);
			rbM01RegionB.addActionListener(alM01Region);

			rbM01Fired1 = new JRadioButton("Single", true);
			rbM01Fired1.setBounds(new Rectangle(80, 240, 65, 30));
			rbM01FiredN = new JRadioButton("Sectored", false);
			rbM01FiredN.setBounds(new Rectangle(145, 240, 80, 30));
			bgM01Fired = new ButtonGroup();
			bgM01Fired.add(rbM01Fired1);
			bgM01Fired.add(rbM01FiredN);

			ActionListener alM01Fired = new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					buoy.paintSign();
				}
			};
			rbM01Fired1.addActionListener(alM01Fired);
			rbM01FiredN.addActionListener(alM01Fired);

			pM01SeaMap = new JPanel();
			pM01SeaMap.setLayout(null);
			pM01SeaMap.add(lM01Head, null);
			pM01SeaMap.add(rbM01RegionA, null);
			pM01SeaMap.add(rbM01RegionB, null);
			pM01SeaMap.add(lM01Region, null);
			pM01SeaMap.add(lM02Region, null);
			pM01SeaMap.add(lM01Icon01, null);
			pM01SeaMap.add(lM01Icon02, null);
			pM01SeaMap.add(getCbM01TypeOfMark(), null);
			pM01SeaMap.add(lM01TypeOfMark, null);
			pM01SeaMap.add(getCbM01CatOfMark(), null);
			pM01SeaMap.add(lM01CatOfMark, null);
			pM01SeaMap.add(getCbM01StyleOfMark(), null);
			pM01SeaMap.add(lM01StyleOfMark, null);
			pM01SeaMap.add(lM01Name, null);
			pM01SeaMap.add(getTfM01Name(), null);
			pM01SeaMap.add(lM01Props02, null);
			pM01SeaMap.add(getCM01TopMark(), null);
			pM01SeaMap.add(getCbM01TopMark(), null);
			pM01SeaMap.add(getCM01Radar(), null);
			pM01SeaMap.add(getCM01Racon(), null);
			pM01SeaMap.add(getCbM01Racon(), null);
			pM01SeaMap.add(getTfM01Racon(), null);
			pM01SeaMap.add(lM01Racon, null);
			pM01SeaMap.add(getCM01Fog(), null);
			pM01SeaMap.add(getCbM01Fog(), null);
			pM01SeaMap.add(getTfM01FogGroup(), null);
			pM01SeaMap.add(lM01FogGroup, null);
			pM01SeaMap.add(getTfM01FogPeriod(), null);
			pM01SeaMap.add(lM01FogPeriod, null);
			pM01SeaMap.add(getCM01Fired(), null);
			pM01SeaMap.add(rbM01Fired1, null);
			pM01SeaMap.add(rbM01FiredN, null);
			pM01SeaMap.add(getTfM01RepeatTime(), null);
			pM01SeaMap.add(lM01RepeatTime, null);
			pM01SeaMap.add(getCbM01Kennung(), null);
			pM01SeaMap.add(lM01Kennung, null);
			pM01SeaMap.add(getCbM01Sector(), null);
			pM01SeaMap.add(lM01Group, null);
			pM01SeaMap.add(getTfM01Group(), null);
			pM01SeaMap.add(lM01Sector, null);
			pM01SeaMap.add(lM01Bearing, null);
			pM01SeaMap.add(getTfM01Bearing(), null);
			pM01SeaMap.add(getTfM02Bearing(), null);
			pM01SeaMap.add(getTfM01Radius(), null);
			pM01SeaMap.add(lM01Height, null);
			pM01SeaMap.add(getTfM01Height(), null);
			pM01SeaMap.add(lM01Range, null);
			pM01SeaMap.add(getTfM01Range(), null);
			pM01SeaMap.add(lM01FireMark, null);
			pM01SeaMap.add(getBM01Save(), null);
			pM01SeaMap.add(getSM01StatusBar(), null);
			pM01SeaMap.add(getBM01Close(), null);
			pM01SeaMap.add(getCM01IconVisible(), null);
		}
		return pM01SeaMap;
	}

	private JComboBox getCbM01TypeOfMark() {

		if (cbM01TypeOfMark == null) {

			cbM01TypeOfMark = new JComboBox();

			// Inhalt der ComboBox
			cbM01TypeOfMark.addItem("* Select Seamark *");
			cbM01TypeOfMark.addItem("Lateral Mark");
			cbM01TypeOfMark.addItem("Cardinal Mark");
			cbM01TypeOfMark.addItem("Safe Water Mark");
			cbM01TypeOfMark.addItem("Isolated Danger");
			cbM01TypeOfMark.addItem("Special Purpose");
			cbM01TypeOfMark.addItem("Light");

			cbM01TypeOfMark.setBounds(new Rectangle(50, 25, 150, 25));
			cbM01TypeOfMark.setEditable(false);
			cbM01TypeOfMark.setFont(new Font("Dialog", Font.PLAIN, 12));
			cbM01TypeOfMark.setEnabled(true);

			cbM01TypeOfMark.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int type = cbM01TypeOfMark.getSelectedIndex();

					if (buoy == null) {
						buoy = new BuoyUkn(dia, "Seamark not set");
						return;
					}

					Node n = buoy.getNode();
					if (n == null)
						return;

					switch (type) {

					case SeaMark.UNKNOWN_TYPE:
						if (!(buoy instanceof BuoyUkn))
							buoy = new BuoyUkn(dia, "Seamark not set");
						buoy.setBuoyIndex(type);
						break;

					case SeaMark.LATERAL:
						if (!(buoy instanceof BuoyLat)) {
							buoy.setBuoyIndex(0);
							cbM01CatOfMark.removeAllItems();
							cbM01CatOfMark.addItem("*Select Category*");
							cbM01CatOfMark.addItem("Port");
							cbM01CatOfMark.addItem("Starboard");
							cbM01CatOfMark.addItem("Preferred Port");
							cbM01CatOfMark.addItem("Preferred Starboard");
							cbM01CatOfMark.setEnabled(true);
						}
						break;

					case SeaMark.CARDINAL:
						if (!(buoy instanceof BuoyCard)) {
							buoy.setBuoyIndex(0);
							cbM01CatOfMark.removeAllItems();
							cbM01CatOfMark.addItem("*Select Category*");
							cbM01CatOfMark.addItem("North");
							cbM01CatOfMark.addItem("East");
							cbM01CatOfMark.addItem("South");
							cbM01CatOfMark.addItem("West");
							cbM01CatOfMark.setEnabled(true);
						}
						break;

					case SeaMark.SAFE_WATER:
						if (!(buoy instanceof BuoySaw))
							buoy = new BuoySaw(dia, n);
						buoy.setBuoyIndex(type);
						break;

					case SeaMark.ISOLATED_DANGER:
						if (!(buoy instanceof BuoyIsol))
							buoy = new BuoyIsol(dia, n);
						buoy.setBuoyIndex(type);
						break;

					case SeaMark.SPECIAL_PURPOSE:
						if (!(buoy instanceof BuoySpec))
							buoy = new BuoySpec(dia, n);
						buoy.setBuoyIndex(type);
						break;
					}

					buoy.refreshStyles();
					buoy.refreshLights();
					buoy.setLightColour();
					buoy.paintSign();
				}
			});
		}
		return cbM01TypeOfMark;
	}

	private JComboBox getCbM01CatOfMark() {
		if (cbM01CatOfMark == null) {
			cbM01CatOfMark = new JComboBox();
			cbM01CatOfMark.setBounds(new Rectangle(75, 55, 125, 25));
			cbM01CatOfMark.setFont(new Font("Dialog", Font.PLAIN, 12));
			cbM01CatOfMark.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int cat = cbM01CatOfMark.getSelectedIndex();

					if (buoy == null) {
						buoy = new BuoyUkn(dia, "Seamark not set");
						return;
					}
					if (cat == 0)
						return;

					Node n = buoy.getNode();
					if (n == null)
						return;

					if (cbM01TypeOfMark.getSelectedIndex() == SeaMark.LATERAL) {
						if (!(buoy instanceof BuoyLat))
							buoy = new BuoyLat(dia, n);
						buoy.setBuoyIndex(cat);
					}
					if (cbM01TypeOfMark.getSelectedIndex() == SeaMark.CARDINAL) {
						if (!(buoy instanceof BuoyCard))
							buoy = new BuoyCard(dia, n);
						buoy.setBuoyIndex(cat);
					}

					buoy.refreshStyles();
					buoy.refreshLights();
					buoy.setLightColour();
					buoy.paintSign();
				}
			});
		}
		return cbM01CatOfMark;
	}

	private JComboBox getCbM01StyleOfMark() {
		if (cbM01StyleOfMark == null) {
			cbM01StyleOfMark = new JComboBox();
			cbM01StyleOfMark.setBounds(new Rectangle(50, 85, 150, 25));
			cbM01StyleOfMark.setFont(new Font("Dialog", Font.PLAIN, 12));
			cbM01StyleOfMark.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int style = cbM01StyleOfMark.getSelectedIndex();
					if (buoy != null && style != buoy.getStyleIndex()) {
						buoy.setStyleIndex(style);
						buoy.paintSign();
					}
				}
			});
		}
		return cbM01StyleOfMark;
	}

	private JTextField getTfM01Name() {
		if (tfM01Name == null) {
			tfM01Name = new JTextField();
			tfM01Name.setBounds(new Rectangle(50, 120, 150, 20));
			tfM01Name.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					buoy.setName(tfM01Name.getText());
				}
			});
		}

		return tfM01Name;
	}

	private JCheckBox getCM01TopMark() {
		if (cM01TopMark == null) {
			cM01TopMark = new JCheckBox();
			cM01TopMark.setBounds(new Rectangle(10, 170, 90, 20));
			cM01TopMark.setFont(new Font("Dialog", Font.PLAIN, 12));
			cM01TopMark.setText("Topmark");
			cM01TopMark.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (buoy == null) {
						return;
					}
					buoy.setTopMark(cM01TopMark.isSelected());
					buoy.paintSign();
				}
			});
		}
		return cM01TopMark;
	}

	private JComboBox getCbM01TopMark() {
		if (cbM01TopMark == null) {
			cbM01TopMark = new JComboBox();
			cbM01TopMark.setBounds(new Rectangle(100, 170, 70, 20));
			cbM01TopMark.setFont(new Font("Dialog", Font.PLAIN, 12));
			cbM01TopMark.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int top = cbM01TopMark.getSelectedIndex();
				}
			});
		}
		return cbM01TopMark;
	}

	private JCheckBox getCM01Radar() {
		if (cM01Radar == null) {
			cM01Radar = new JCheckBox();
			cM01Radar.setBounds(new Rectangle(10, 195, 120, 20));
			cM01Radar.setFont(new Font("Dialog", Font.PLAIN, 12));
			cM01Radar.setText("Radar Reflector");
		}
		return cM01Radar;
	}

	private JCheckBox getCM01Racon() {
		if (cM01Racon == null) {
			cM01Racon = new JCheckBox();
			cM01Racon.setBounds(new Rectangle(130, 195, 110, 20));
			cM01Racon.setFont(new Font("Dialog", Font.PLAIN, 12));
			cM01Racon.setText("Radar Beacon");
		}
		return cM01Racon;
	}

	private JComboBox getCbM01Racon() {
		if (cbM01Racon == null) {
			cbM01Racon = new JComboBox();
			cbM01Racon.setBounds(new Rectangle(240, 195, 80, 20));
			cbM01Racon.setFont(new Font("Dialog", Font.PLAIN, 12));
			cbM01Racon.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int rac = cbM01Racon.getSelectedIndex();
				}
			});
		}
		return cbM01Racon;
	}

	private JTextField getTfM01Racon() {
		if (tfM01Racon == null) {
			tfM01Racon = new JTextField();
			tfM01Racon.setBounds(new Rectangle(345, 195, 30, 20));
			tfM01Racon.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					buoy.setName(tfM01Racon.getText());
				}
			});
		}
		return tfM01Racon;
	}

	private JCheckBox getCM01Fog() {
		if (cM01Fog == null) {
			cM01Fog = new JCheckBox();
			cM01Fog.setBounds(new Rectangle(10, 220, 90, 20));
			cM01Fog.setFont(new Font("Dialog", Font.PLAIN, 12));
			cM01Fog.setText("Fog Signal");
		}
		return cM01Fog;
	}

	private JComboBox getCbM01Fog() {
		if (cbM01Fog == null) {
			cbM01Fog = new JComboBox();
			cbM01Fog.setBounds(new Rectangle(100, 220, 70, 20));
			cbM01Fog.setFont(new Font("Dialog", Font.PLAIN, 12));
			cbM01Fog.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int fog = cbM01Fog.getSelectedIndex();
				}
			});
		}
		return cbM01Fog;
	}

	private JTextField getTfM01FogGroup() {
		if (tfM01FogGroup == null) {
			tfM01FogGroup = new JTextField();
			tfM01FogGroup.setBounds(new Rectangle(243, 220, 30, 20));
			tfM01FogGroup.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					buoy.setName(tfM01FogGroup.getText());
				}
			});
		}
		return tfM01FogGroup;
	}

	private JTextField getTfM01FogPeriod() {
		if (tfM01FogPeriod == null) {
			tfM01FogPeriod = new JTextField();
			tfM01FogPeriod.setBounds(new Rectangle(345, 220, 30, 20));
			tfM01FogPeriod.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					buoy.setName(tfM01FogPeriod.getText());
				}
			});
		}
		return tfM01FogPeriod;
	}

	private JCheckBox getCM01Fired() {
		if (cM01Fired == null) {
			cM01Fired = new JCheckBox();
			cM01Fired.setBounds(new Rectangle(10, 245, 70, 20));
			cM01Fired.setFont(new Font("Dialog", Font.PLAIN, 12));
			cM01Fired.setText("Lighted");
			cM01Fired.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (buoy == null) {
						return;
					}
					buoy.setFired(cM01Fired.isSelected());
					buoy.setLightColour();
					buoy.paintSign();
				}
			});
		}

		return cM01Fired;
	}

	private JComboBox getCbM01Kennung() {
		if (cbM01Kennung == null) {
			cbM01Kennung = new JComboBox();
			cbM01Kennung.setBounds(new Rectangle(305, 245, 70, 20));
			cbM01Kennung.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int i1, i2;
					String g = "", c = "";
					String it = (String) cbM01Kennung.getSelectedItem();

					if (it == null)
						return;
					if (it.compareTo("Not set") == 0)
						return;
					if (buoy == null)
						return;

					i1 = it.indexOf("(");
					i2 = it.indexOf(")", i1);
					tfM01Group.setText("");
					tfM01Group.setEnabled(false);
					if (i1 >= 0) {
						c = it.substring(0, i1);
						if (i1 >= 0) {
							g = it.substring(i1 + 1, i2);
							if (g.equals(""))
								tfM01Group.setEnabled(true);
						}
					}
					if (it.contains("+")) {
						i1 = it.indexOf("+");
						i2 = it.length();
						if (c.equals(""))
							c = it;
						else
							c = c + it.substring(i1, i2);
					}
					if (c.equals(""))
						c = it;
					buoy.setLightChar(c);
					buoy.setLightGroup(g);
					buoy.paintSign();
				}
			});
		}
		return cbM01Kennung;
	}

	private JComboBox getCbM01Sector() {
		if (cbM01Sector == null) {
			cbM01Sector = new JComboBox();
			cbM01Sector.setBounds(new Rectangle(120, 270, 50, 20));
			cbM01Sector.setFont(new Font("Dialog", Font.PLAIN, 12));
			cbM01Sector.addItem("0");
			cbM01Sector.addItem("1");
			cbM01Sector.addItem("2");
			cbM01Sector.addItem("3");
			cbM01Sector.addItem("4");
			cbM01Sector.addItem("5");
			cbM01Sector.addItem("6");
			cbM01Sector.addItem("7");
			cbM01Sector.addItem("8");
			cbM01Sector.addItem("9");
			cbM01Sector.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int sec = cbM01Sector.getSelectedIndex();
				}
			});
		}
		return cbM01Sector;
	}

	private JTextField getTfM01RepeatTime() {
		if (tfM01RepeatTime == null) {
			tfM01RepeatTime = new JTextField();
			tfM01RepeatTime.setBounds(new Rectangle(345, 270, 30, 20));
			tfM01RepeatTime.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String s = tfM01RepeatTime.getText();

					buoy.setLightPeriod(s);
					buoy.paintSign();
				}
			});

			tfM01RepeatTime.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					String s = tfM01RepeatTime.getText();

					buoy.setLightPeriod(s);
					buoy.paintSign();
				}
			});
		}

		return tfM01RepeatTime;
	}

	private JTextField getTfM01Group() {
		if (tfM01Group == null) {
			tfM01Group = new JTextField();
			tfM01Group.setBounds(new Rectangle(243, 270, 30, 20));
			tfM01Group.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					buoy.setLightGroup(tfM01Group.getText());
				}
			});
		}
		return tfM01Group;
	}

	private JTextField getTfM01Bearing() {
		if (tfM01Bearing == null) {
			tfM01Bearing = new JTextField();
			tfM01Bearing.setBounds(new Rectangle(40, 295, 30, 20));
			tfM01Bearing.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					buoy.setName(tfM01Bearing.getText());
				}
			});
		}
		return tfM01Bearing;
	}

	private JTextField getTfM02Bearing() {
		if (tfM02Bearing == null) {
			tfM02Bearing = new JTextField();
			tfM02Bearing.setBounds(new Rectangle(85, 295, 30, 20));
			tfM02Bearing.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					buoy.setName(tfM02Bearing.getText());
				}
			});
		}
		return tfM02Bearing;
	}

	private JTextField getTfM01Radius() {
		if (tfM01Radius == null) {
			tfM01Radius = new JTextField();
			tfM01Radius.setBounds(new Rectangle(140, 295, 30, 20));
			tfM01Radius.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					buoy.setName(tfM01Radius.getText());
				}
			});
		}
		return tfM01Radius;
	}

	private JTextField getTfM01Height() {
		if (tfM01Height == null) {
			tfM01Height = new JTextField();
			tfM01Height.setBounds(new Rectangle(243, 295, 30, 20));
			tfM01Height.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					buoy.setName(tfM01Height.getText());
				}
			});
		}
		return tfM01Height;
	}

	private JTextField getTfM01Range() {
		if (tfM01Range == null) {
			tfM01Range = new JTextField();
			tfM01Range.setBounds(new Rectangle(345, 295, 30, 20));
			tfM01Range.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					buoy.setName(tfM01Range.getText());
				}
			});
		}
		return tfM01Range;
	}

	private JButton getBM01Close() {
		if (bM01Close == null) {
			bM01Close = new JButton();
			bM01Close.setBounds(new Rectangle(20, 325, 80, 20));
			bM01Close.setText("Close");
			bM01Close.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// aufraeumen
					if (obuoy != null)
						PicRebuild();
					// Deaktivierung des Listeners
					DataSet.selListeners.remove(SmpListener);
					// DataSet.removeSelectionListener(SmpListener);
					Selection = null;
					SmpItem.setEnabled(true);
					onode = null;

					dM01SeaMap.dispose();
				}
			});
		}

		return bM01Close;
	}

	private JButton getBM01Save() {
		if (bM01Save == null) {
			bM01Save = new JButton();
			bM01Save.setBounds(new Rectangle(120, 325, 80, 20));
			bM01Save.setText("Save");
			bM01Save.setEnabled(false);

			bM01Save.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cM01IconVisible.setIcon(new ImageIcon(getClass().getResource(
							"/images/Auge.png")));
					cM01IconVisible.setSelected(true);

					buoy.saveSign();
				}
			});
		}

		return bM01Save;
	}

	private JCheckBox getCM01IconVisible() {
		if (cM01IconVisible == null) {
			cM01IconVisible = new JCheckBox();
			cM01IconVisible.setBounds(new Rectangle(310, 325, 30, 21));
			cM01IconVisible.setIcon(new ImageIcon(getClass().getResource(
					"/images/AugeN.png")));
			cM01IconVisible.setSelected(false);
			cM01IconVisible.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Command c;
					Node n = null;
					DataSet ds = Main.main.getCurrentDataSet();

					if (buoy != null)
						n = buoy.getNode();

					if (cM01IconVisible.isSelected()) {
						cM01IconVisible.setIcon(new ImageIcon(getClass().getResource(
								"/images/AugeN.png")));
						if (n != null) {
							// seamark loeschen, wenn notwendig
							if (n.getKeys().containsKey("seamark")) {
								smb = n.getKeys().get("seamark"); // smb merken

								c = new ChangePropertyCommand(n, "seamark", null);
								c.executeCommand();
								ds.fireSelectionChanged();
								obuoy = buoy;
							}

							// seamark:type loeschen, wenn notwendig
							if (n.getKeys().containsKey("seamark:type")) {
								smt = n.getKeys().get("seamark:type"); // smt merken

								c = new ChangePropertyCommand(n, "seamark:type", null);
								c.executeCommand();
								ds.fireSelectionChanged();
								obuoy = buoy;
							}

						}
					} else {
						cM01IconVisible.setIcon(new ImageIcon(getClass().getResource(
								"/images/Auge.png")));
						PicRebuild();
						obuoy = null;
					}
					buoy.paintSign();
				}
			});
		}
		return cM01IconVisible;
	}

	private JTextField getSM01StatusBar() {
		if (sM01StatusBar == null) {
			sM01StatusBar = new JTextField();
			sM01StatusBar.setBounds(new Rectangle(7, 355, 385, 20));
			sM01StatusBar.setBackground(SystemColor.activeCaptionBorder);
		}
		return sM01StatusBar;
	}

}
