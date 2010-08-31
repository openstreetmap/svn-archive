//License: GPL. For details, see LICENSE file.
// Copyright (c) 2009 / 2010 by Werner Koenig & Malcolm Herring

package toms.dialogs;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
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
	/**
	 * 
	 */
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
	private JLabel lM01TimeUnit = null;
	private JLabel lM01RepeatTime = null;
	private JLabel lM01Kennung = null;
	private JLabel lM01Name = null;
	private JLabel lM01Props02 = null;
	private JLabel lM01StyleOfMark = null;
	private JLabel lM01TypeOfMark = null;
	private JLabel lM01Region = null;
	private JLabel lM01Head = null;
	public JLabel lM01Icon01 = null;
	public JLabel lM01Icon02 = null;
	public JToggleButton tbM01Region = null;
	public JComboBox cbM01TypeOfMark = null;
	public JComboBox cbM01StyleOfMark = null;
	public JButton bM01Save = null;
	public JCheckBox cM01TopMark = null;
	private JCheckBox cM01Radar = null;
	public JCheckBox cM01Fired = null;
	private JCheckBox cM01Fog = null;
	public JTextField sM01StatusBar = null;
	public JTextField tfM01Name = null;
	private JButton bM01Close = null;
	public JTextField tfM01RepeatTime = null;
	public JComboBox cbM01Kennung = null;
	public JTextField tfM01FireMark = null;
	private JCheckBox cM01IconVisible = null;

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
			if (!str.equals("")) str += new String(new char[] {0x1e});
			Main.pref.put("mappaint.style.sources", str + "http://dev.openseamap.org/josm/seamark_styles.xml");
		}
		str = Main.pref.get("color.background");
		if (str.equals("#000000") || str.equals(""))
			Main.pref.put("color.background", "#606060");
	}

	public void CloseDialog() {
		onode = null;
		DataSet.selListeners.remove(SmpListener);
//		DataSet.removeSelectionListener(SmpListener);
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
//		DataSet.addSelectionListener(SmpListener);
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

		tbM01Region.setEnabled(true);
		cbM01TypeOfMark.setEnabled(true);
		cbM01StyleOfMark.setEnabled(true);

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
			dM01SeaMap.setSize(new Dimension(353, 373));
			// dM01SeaMap.setSize(new Dimension(400, 400));
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
//				DataSet.removeSelectionListener(SmpListener);
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
			lM01TimeUnit = new JLabel();
			lM01TimeUnit.setBounds(new Rectangle(325, 230, 26, 20));
			lM01TimeUnit.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01TimeUnit.setText("s");

			lM01RepeatTime = new JLabel();
			lM01RepeatTime.setBounds(new Rectangle(245, 230, 70, 20));
			lM01RepeatTime.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01RepeatTime.setText("Period:");

			lM01Kennung = new JLabel();
			lM01Kennung.setBounds(new Rectangle(95, 230, 60, 20));
			lM01Kennung.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01Kennung.setText("Character:");

			lM01Name = new JLabel();
			lM01Name.setBounds(new Rectangle(5, 120, 82, 16));
			lM01Name.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01Name.setText("Name:");

			lM01Props02 = new JLabel();
			lM01Props02.setBounds(new Rectangle(5, 170, 90, 16));
			lM01Props02.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01Props02.setText("Other Features:");

			lM01StyleOfMark = new JLabel();
			lM01StyleOfMark.setBounds(new Rectangle(5, 90, 148, 16));
			lM01StyleOfMark.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01StyleOfMark.setText("Shape:");

			lM01TypeOfMark = new JLabel();
			lM01TypeOfMark.setBounds(new Rectangle(5, 30, 120, 16));
			lM01TypeOfMark.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01TypeOfMark.setText("Type:");

			lM01Region = new JLabel();
			lM01Region.setBounds(new Rectangle(5, 60, 120, 16));
			lM01Region.setFont(new Font("Dialog", Font.PLAIN, 12));
			lM01Region.setText("Region:");

			lM01Head = new JLabel();
			lM01Head.setBounds(new Rectangle(5, 3, 316, 16));
			lM01Head.setText("Seamark Properties");

			lM01Icon01 = new JLabel();
			lM01Icon01.setBounds(new Rectangle(225, 0, 160, 200));
			lM01Icon01.setIcon(null);
			lM01Icon01.setText("");

			lM01Icon02 = new JLabel();
			lM01Icon02.setBounds(new Rectangle(225, 0, 160, 200));
			lM01Icon02.setIcon(null);
			lM01Icon02.setText("");

			pM01SeaMap = new JPanel();
			pM01SeaMap.setLayout(null);
			pM01SeaMap.add(lM01TimeUnit, null);
			pM01SeaMap.add(lM01RepeatTime, null);
			pM01SeaMap.add(lM01Kennung, null);
			pM01SeaMap.add(lM01Name, null);
			pM01SeaMap.add(lM01Props02, null);
			pM01SeaMap.add(lM01StyleOfMark, null);
			pM01SeaMap.add(lM01TypeOfMark, null);
			pM01SeaMap.add(lM01Region, null);
			pM01SeaMap.add(lM01Head, null);
			pM01SeaMap.add(lM01Icon01, null);
			pM01SeaMap.add(lM01Icon02, null);
			pM01SeaMap.add(getTbM01Region(), null);
			pM01SeaMap.add(getCbM01TypeOfMark(), null);
			pM01SeaMap.add(getCbM01StyleOfMark(), null);
			pM01SeaMap.add(getBM01Save(), null);
			pM01SeaMap.add(getCM01TopMark(), null);
			pM01SeaMap.add(getCM01Radar(), null);
			pM01SeaMap.add(getCM01Fired(), null);
			pM01SeaMap.add(getCM01Fog(), null);
			pM01SeaMap.add(getSM01StatusBar(), null);
			pM01SeaMap.add(getTfM01Name(), null);
			pM01SeaMap.add(getBM01Close(), null);
			pM01SeaMap.add(getTfM01RepeatTime(), null);
			pM01SeaMap.add(getCbM01Kennung(), null);
			pM01SeaMap.add(getTfM01FireMark(), null);
			pM01SeaMap.add(getCM01IconVisible(), null);
		}

		return pM01SeaMap;
	}

	private JComboBox getCbM01TypeOfMark() {

		if (cbM01TypeOfMark == null) {

			cbM01TypeOfMark = new JComboBox();

			// Inhalt der ComboBox
			cbM01TypeOfMark.addItem("Not set");
			cbM01TypeOfMark.addItem("Port");
			cbM01TypeOfMark.addItem("Starboard");
			cbM01TypeOfMark.addItem("Preferred Port");
			cbM01TypeOfMark.addItem("Preferred Starboard");
			cbM01TypeOfMark.addItem("Safe Water");
			cbM01TypeOfMark.addItem("Cardinal North");
			cbM01TypeOfMark.addItem("Cardinal East");
			cbM01TypeOfMark.addItem("Cardinal South");
			cbM01TypeOfMark.addItem("Cardinal West");
			cbM01TypeOfMark.addItem("Isolated Danger");
			cbM01TypeOfMark.addItem("Special Purpose");
			// cbM01TypeOfMark.addItem("Light");

			cbM01TypeOfMark.setBounds(new Rectangle(50, 25, 150, 25));
			cbM01TypeOfMark.setEditable(false);
			cbM01TypeOfMark.setFont(new Font("Dialog", Font.PLAIN, 12));
			cbM01TypeOfMark.setEnabled(true);

			cbM01TypeOfMark.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Node n;
					int type = cbM01TypeOfMark.getSelectedIndex();

					if (buoy == null) {
						buoy = new BuoyUkn(dia, "Seamark not set");
						return;
					}
					if ((type == 0) || (type == buoy.getBuoyIndex()))
						return;

					n = buoy.getNode();
					if (n == null)
						return;
					switch (type) {

					case SeaMark.UNKNOWN_CAT:
						if (!(buoy instanceof BuoyUkn))
							buoy = new BuoyUkn(dia, "Seamark not set");
						buoy.setBuoyIndex(type);
						break;

					case SeaMark.PORT_HAND:
					case SeaMark.STARBOARD_HAND:
					case SeaMark.PREF_PORT_HAND:
					case SeaMark.PREF_STARBOARD_HAND:
						if (!(buoy instanceof BuoyLat))
							buoy = new BuoyLat(dia, n);
						break;

					case SeaMark.SAFE_WATER:
						if (!(buoy instanceof BuoySaw))
							buoy = new BuoySaw(dia, n);
						break;

					case SeaMark.CARD_NORTH:
					case SeaMark.CARD_EAST:
					case SeaMark.CARD_SOUTH:
					case SeaMark.CARD_WEST:
						if (!(buoy instanceof BuoyCard))
							buoy = new BuoyCard(dia, n);
						break;

					case SeaMark.ISOLATED_DANGER:
						if (!(buoy instanceof BuoyIsol))
							buoy = new BuoyIsol(dia, n);
						break;

					case SeaMark.SPECIAL_PURPOSE:
						if (!(buoy instanceof BuoySpec))
							buoy = new BuoySpec(dia, n);
						break;
					default:
						if (!(buoy instanceof BuoyUkn))
							buoy = new BuoyUkn(dia, "Not Implemented");
					}

					buoy.setBuoyIndex(type);
					buoy.refreshStyles();
					buoy.refreshLights();
					buoy.paintSign();
				}
			});

		}

		return cbM01TypeOfMark;
	}

	private JToggleButton getTbM01Region() {
		if (tbM01Region == null) {
			tbM01Region = new JToggleButton();

			tbM01Region.setBounds(new Rectangle(60, 55, 80, 25));
			tbM01Region.setFont(new Font("Dialog", Font.PLAIN, 12));
			tbM01Region.setEnabled(false);
			if (Main.pref.get("tomsplugin.IALA").equals("B")) {
				tbM01Region.setSelected(true);
				tbM01Region.setText("IALA-B");
			} else {
				tbM01Region.setSelected(false);
				tbM01Region.setText("IALA-A");
			}

			tbM01Region.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (tbM01Region.isSelected()) {
						buoy.setRegion(true);
						tbM01Region.setText("IALA-B");
					} else {
						buoy.setRegion(false);
						tbM01Region.setText("IALA-A");
					}
					buoy.paintSign();
				}
			});
		}

		return tbM01Region;
	}

	private JComboBox getCbM01StyleOfMark() {
		if (cbM01StyleOfMark == null) {
			cbM01StyleOfMark = new JComboBox();
			cbM01StyleOfMark.setBounds(new Rectangle(50, 85, 150, 25));
			cbM01StyleOfMark.setFont(new Font("Dialog", Font.PLAIN, 12));
			cbM01StyleOfMark.setEnabled(true);
			cbM01StyleOfMark.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
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

	private JButton getBM01Save() {
		if (bM01Save == null) {
			bM01Save = new JButton();
			bM01Save.setBounds(new Rectangle(120, 290, 80, 20));
			bM01Save.setText("Save");

			bM01Save.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					cM01IconVisible.setIcon(new ImageIcon(getClass().getResource(
							"/images/Auge.png")));
					cM01IconVisible.setSelected(true);

					buoy.saveSign();
				}
			});
		}

		return bM01Save;
	}

	private JCheckBox getCM01TopMark() {
		if (cM01TopMark == null) {
			cM01TopMark = new JCheckBox();
			cM01TopMark.setBounds(new Rectangle(20, 190, 90, 20));
			cM01TopMark.setFont(new Font("Dialog", Font.PLAIN, 12));
			cM01TopMark.setText("Topmark");
			cM01TopMark.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
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

	private JCheckBox getCM01Radar() {
		if (cM01Radar == null) {
			cM01Radar = new JCheckBox();
			cM01Radar.setBounds(new Rectangle(140, 190, 70, 20));
			cM01Radar.setFont(new Font("Dialog", Font.PLAIN, 12));
			cM01Radar.setEnabled(false);
			cM01Radar.setText("Radar");
		}

		return cM01Radar;
	}

	private JCheckBox getCM01Fired() {
		if (cM01Fired == null) {
			cM01Fired = new JCheckBox();
			cM01Fired.setBounds(new Rectangle(20, 230, 70, 20));
			cM01Fired.setFont(new Font("Dialog", Font.PLAIN, 12));
			cM01Fired.setText("Lighted");
			cM01Fired.setEnabled(false);
			cM01Fired.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					if (buoy == null) {
						return;
					}

					buoy.setFired(cM01Fired.isSelected());

					buoy.paintSign();
				}
			});
		}

		return cM01Fired;
	}

	private JCheckBox getCM01Fog() {
		if (cM01Fog == null) {
			cM01Fog = new JCheckBox();
			cM01Fog.setBounds(new Rectangle(20, 210, 90, 20));
			cM01Fog.setFont(new Font("Dialog", Font.PLAIN, 12));
			cM01Fog.setEnabled(false);
			cM01Fog.setText("Foghorn");
		}

		return cM01Fog;
	}

	private JTextField getSM01StatusBar() {
		if (sM01StatusBar == null) {
			sM01StatusBar = new JTextField();
			sM01StatusBar.setBounds(new Rectangle(7, 319, 340, 20));
			sM01StatusBar.setBackground(SystemColor.activeCaptionBorder);
		}

		return sM01StatusBar;
	}

	private JTextField getTfM01Name() {
		if (tfM01Name == null) {
			tfM01Name = new JTextField();
			tfM01Name.setBounds(new Rectangle(50, 120, 175, 20));
			tfM01Name.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusLost(java.awt.event.FocusEvent e) {
					buoy.setName(tfM01Name.getText());
				}
			});
		}

		return tfM01Name;
	}

	private JButton getBM01Close() {
		if (bM01Close == null) {
			bM01Close = new JButton();
			bM01Close.setBounds(new Rectangle(20, 290, 80, 20));
			bM01Close.setText("Close");
			bM01Close.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// aufraeumen
					if (obuoy != null)
						PicRebuild();
					// Deaktivierung des Listeners
					DataSet.selListeners.remove(SmpListener);
//				DataSet.removeSelectionListener(SmpListener);
					Selection = null;
					SmpItem.setEnabled(true);
					onode = null;

					dM01SeaMap.dispose();
				}
			});
		}

		return bM01Close;
	}

	private JTextField getTfM01RepeatTime() {
		if (tfM01RepeatTime == null) {
			tfM01RepeatTime = new JTextField();
			tfM01RepeatTime.setBounds(new Rectangle(290, 230, 30, 20));
			tfM01RepeatTime.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String s = tfM01RepeatTime.getText();

					buoy.setLightPeriod(s);
					buoy.paintSign();
				}
			});

			tfM01RepeatTime.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusLost(java.awt.event.FocusEvent e) {
					String s = tfM01RepeatTime.getText();

					buoy.setLightPeriod(s);
					buoy.paintSign();
				}
			});
		}

		return tfM01RepeatTime;
	}

	private JComboBox getCbM01Kennung() {
		if (cbM01Kennung == null) {
			cbM01Kennung = new JComboBox();
			cbM01Kennung.setBounds(new Rectangle(160, 230, 70, 20));
			cbM01Kennung.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int i1, i2;
					String g = "", c = "";
					String it = (String) cbM01Kennung.getSelectedItem();

					if (it == null)
						return;

					if (it.compareTo("Not set") == 0)
						return;

					if (buoy == null) {
						return;
					}

					i1 = it.indexOf("(");
					i2 = it.indexOf(")", i1);

					if (i1 >= 0) {
						c = it.substring(0, i1);
						if (i1 >= 0)
							g = it.substring(i1 + 1, i2);
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
					// buoy.setLightColour();

					buoy.paintSign();
				}
			});
		}

		return cbM01Kennung;
	}

	private JTextField getTfM01FireMark() {
		if (tfM01FireMark == null) {
			tfM01FireMark = new JTextField();
			tfM01FireMark.setBounds(new Rectangle(240, 160, 100, 20));
			tfM01FireMark.setEditable(false);
		}

		return tfM01FireMark;
	}

	private JCheckBox getCM01IconVisible() {
		if (cM01IconVisible == null) {
			cM01IconVisible = new JCheckBox();
			cM01IconVisible.setBounds(new Rectangle(310, 290, 30, 21));
			cM01IconVisible.setIcon(new ImageIcon(getClass().getResource(
					"/images/AugeN.png")));
			cM01IconVisible.setSelected(false);
			cM01IconVisible.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
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
}
