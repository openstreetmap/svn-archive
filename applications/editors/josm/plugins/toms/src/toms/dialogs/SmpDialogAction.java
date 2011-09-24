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
import java.io.IOException;
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

import toms.Messages;
import toms.plug.PluginApp;
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
	private String smt = ""; // value vom key "seamark:type" // @jve:decl-index=0: //$NON-NLS-1$
	private String smb = ""; // value vom key "seamark" // @jve:decl-index=0: //$NON-NLS-1$
	private Collection<? extends OsmPrimitive> Selection = null; // @jve:decl-index=0:
	private OsmPrimitive SelNode = null;
	private String Os = ""; // @jve:decl-index=0: //$NON-NLS-1$
	private String UserHome = ""; // @jve:decl-index=0: //$NON-NLS-1$

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
	public JLabel lM01Icon = null; // Shape
	public JLabel lM02Icon = null; // Light
	public JLabel lM03Icon = null; // Reflector
	public JLabel lM04Icon = null; // Racon
	public JLabel lM05Icon = null; // Fog
	public JLabel lM06Icon = null; // Topmark
	public JLabel lM01NameMark = null;
	public JLabel lM01FireMark = null;
	public JLabel lM01FogMark = null;
	public JLabel lM01RadarMark = null;
	private JLabel lM01TypeOfMark = null;
	public JComboBox cbM01TypeOfMark = null;
	public JLabel lM01CatOfMark = null;
	public JComboBox cbM01CatOfMark = null;
	public JLabel lM01StyleOfMark = null;
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
	public JLabel lM01Racon = null;
	public JCheckBox cM01Fog = null;
	public JComboBox cbM01Fog = null;
	public JLabel lM01FogGroup = null;
	public JTextField tfM01FogGroup = null;
	public JLabel lM01FogPeriod = null;
	public JTextField tfM01FogPeriod = null;
	public JCheckBox cM01Fired = null;
	public ButtonGroup bgM01Fired = null;
	public JRadioButton rbM01Fired1 = null;
	public JRadioButton rbM01FiredN = null;
	public JLabel lM01Kennung = null;
	public JComboBox cbM01Kennung = null;
	public JLabel lM01Height = null;
	public JTextField tfM01Height = null;
	public JLabel lM01Range = null;
	public JTextField tfM01Range = null;
	public JLabel lM01Group = null;
	public JTextField tfM01Group = null;
	public JLabel lM01RepeatTime = null;
	public JTextField tfM01RepeatTime = null;
	public JLabel lM01Sector = null;
	public JComboBox cbM01Sector = null;
	public JLabel lM01Colour = null;
	public JComboBox cbM01Colour = null;
	public JLabel lM01Bearing = null;
	public JTextField tfM01Bearing = null;
	public JTextField tfM02Bearing = null;
	public JTextField tfM01Radius = null;
	public JButton bM01Save = null;
	public JButton bM01Close = null;
	public JCheckBox cM01IconVisible = null;
	public JTextField sM01StatusBar = null;

	public boolean paintlock = false;

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
		super(
				Messages.getString("SmpDialogAction.4"), "Smp", Messages.getString("SmpDialogAction.0"), Shortcut //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						.registerShortcut(
								"tools:Semarks", //$NON-NLS-1$
								tr("Tool: {0}", Messages.getString("SmpDialogAction.9")), KeyEvent.VK_S, //$NON-NLS-1$ //$NON-NLS-2$
								Shortcut.GROUP_EDIT, Shortcut.SHIFT_DEFAULT), true);

		dia = this;
		for (int l = 0; ; l++) {
			String entry = new String("mappaint.style.sources-list." + l);
			String str = Main.pref.get(entry);
			if (str == null || str.isEmpty()) {
				String sep = new String(new char[] { 0x1e });
				Main.pref.put(entry, "http://dev.openseamap.org/josm/seamark_styles.xml" + sep + sep + "OpenSeaMap" + sep + "true");
				break;
			}
			if (str.contains("http://dev.openseamap.org/josm/seamark_styles.xml")) {
				break;
			}
		}
	}

	public void CloseDialog() {
		onode = null;
		DataSet.removeSelectionListener(SmpListener);
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
		DataSet.addSelectionListener(SmpListener);
	}

	private void PicRebuild() {

		DataSet ds = Main.main.getCurrentDataSet();

		if (obuoy == null) {
			return;
		}

		Node n = obuoy.getNode();

		if (n != null) {
			Command c;

			if (smb != "") { //$NON-NLS-1$

				c = new ChangePropertyCommand(n, "seamark", smb); //$NON-NLS-1$
				c.executeCommand();
				ds.fireSelectionChanged();

				smb = ""; //$NON-NLS-1$
			}

			if (smt != "") { //$NON-NLS-1$

				c = new ChangePropertyCommand(n, "seamark:type", smt); //$NON-NLS-1$
				c.executeCommand();
				ds.fireSelectionChanged();

				smt = ""; //$NON-NLS-1$
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
			buoy = new BuoyUkn(this, Messages.getString("SmpDialogAction.26")); //$NON-NLS-1$
			buoy.setNode(null);
			return;
		}

		selection = ds.getSelectedNodes();
		nodes = selection.size();

		if (nodes == 0) {
			buoy = new BuoyUkn(this, Messages.getString("SmpDialogAction.27")); //$NON-NLS-1$
			buoy.setNode(null);
			return;
		}

		if (nodes > 1) {
			buoy = new BuoyUkn(this, Messages.getString("SmpDialogAction.28")); //$NON-NLS-1$
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
				"/images/Auge.png"))); //$NON-NLS-1$

		cbM01TypeOfMark.setEnabled(true);

		// Soweit das Vorspiel. Ab hier beginnt das Parsen
		String type = ""; //$NON-NLS-1$
		String str = ""; //$NON-NLS-1$

		keys = node.getKeys();

		// vorsorglich den Namen holen und verwenden, wenn es ein
		// Seezeichen ist. Name kann durch die weiteren Tags ueber-
		// schrieben werden

		if (keys.containsKey("seamark:type")) //$NON-NLS-1$
			type = keys.get("seamark:type"); //$NON-NLS-1$

		if (type.equals("buoy_lateral") || type.equals("beacon_lateral")) { //$NON-NLS-1$ //$NON-NLS-2$
			buoy = new BuoyLat(this, node);
			return;

		} else if (type.equals("buoy_cardinal") || type.equals("beacon_cardinal")) { //$NON-NLS-1$ //$NON-NLS-2$
			buoy = new BuoyCard(this, node);
			return;

		} else if (type.equals("buoy_safe_water") || type.equals("beacon_safe_water")) { //$NON-NLS-1$ //$NON-NLS-2$
			buoy = new BuoySaw(this, node);
			return;

		} else if (type.equals("buoy_special_purpose") || type.equals("beacon_special_purpose")) { //$NON-NLS-1$ //$NON-NLS-2$
			buoy = new BuoySpec(this, node);
			return;

		} else if (type.equals("buoy_isolated_danger") || type.equals("beacon_isolated_danger")) { //$NON-NLS-1$ //$NON-NLS-2$
			buoy = new BuoyIsol(this, node);
			return;

		} else if (type.equals("landmark") || type.equals("light_vessel") //$NON-NLS-1$ //$NON-NLS-2$
				|| type.equals("light_major") || type.equals("light_minor")) { //$NON-NLS-1$ //$NON-NLS-2$
			buoy = new BuoyNota(this, node);
			return;

		} else if (type.equals("light_float")) { //$NON-NLS-1$
			if (keys.containsKey("seamark:light_float:colour")) { //$NON-NLS-1$
				str = keys.get("seamark:light_float:colour"); //$NON-NLS-1$
				if (str.equals("red") || str.equals("green") //$NON-NLS-1$ //$NON-NLS-2$
						|| str.equals("red;green;red") || str.equals("green;red;green")) { //$NON-NLS-1$ //$NON-NLS-2$
					buoy = new BuoyLat(this, node);
					return;
				} else if (str.equals("black;yellow") //$NON-NLS-1$
						|| str.equals("black;yellow;black") || str.equals("yellow;black") //$NON-NLS-1$ //$NON-NLS-2$
						|| str.equals("yellow;black;yellow")) { //$NON-NLS-1$
					buoy = new BuoyCard(this, node);
					return;
				} else if (str.equals("black;red;black")) { //$NON-NLS-1$
					buoy = new BuoyIsol(this, node);
					return;
				} else if (str.equals("red;white")) { //$NON-NLS-1$
					buoy = new BuoySaw(this, node);
					return;
				} else if (str.equals("yellow")) { //$NON-NLS-1$
					buoy = new BuoySpec(this, node);
					return;
				}
			} else if (keys.containsKey("seamark:light_float:topmark:shape")) { //$NON-NLS-1$
				str = keys.get("seamark:light_float:topmark:shape"); //$NON-NLS-1$
				if (str.equals("cylinder") || str.equals("cone, point up")) { //$NON-NLS-1$ //$NON-NLS-2$
					buoy = new BuoyLat(this, node);
					return;
				}
			} else if (keys.containsKey("seamark:light_float:topmark:colour")) { //$NON-NLS-1$
				str = keys.get("seamark:light_float:topmark:colour"); //$NON-NLS-1$
				if (str.equals("red") || str.equals("green")) { //$NON-NLS-1$ //$NON-NLS-2$
					buoy = new BuoyLat(this, node);
					return;
				}
			}
		}

		if (keys.containsKey("buoy_lateral:category") || keys.containsKey("beacon_lateral:category")) { //$NON-NLS-1$ //$NON-NLS-2$
			buoy = new BuoyLat(this, node);
			return;
		} else if (keys.containsKey("buoy_cardinal:category") || keys.containsKey("beacon_cardinal:category")) { //$NON-NLS-1$ //$NON-NLS-2$
			buoy = new BuoyCard(this, node);
			return;
		} else if (keys.containsKey("buoy_isolated_danger:category") || keys.containsKey("beacon_isolated_danger:category")) { //$NON-NLS-1$ //$NON-NLS-2$
			buoy = new BuoyIsol(this, node);
			return;
		} else if (keys.containsKey("buoy_safe_water:category") || keys.containsKey("beacon_safe_water:category")) { //$NON-NLS-1$ //$NON-NLS-2$
			buoy = new BuoySaw(this, node);
			return;
		} else if (keys.containsKey("buoy_special_purpose:category") || keys.containsKey("beacon_special_purpose:category")) { //$NON-NLS-1$ //$NON-NLS-2$
			buoy = new BuoySpec(this, node);
			return;
		}

		if (keys.containsKey("buoy_lateral:shape") || keys.containsKey("beacon_lateral:shape")) { //$NON-NLS-1$ //$NON-NLS-2$
			buoy = new BuoyLat(this, node);
			return;
		} else if (keys.containsKey("buoy_cardinal:shape") || keys.containsKey("beacon_cardinal:shape")) { //$NON-NLS-1$ //$NON-NLS-2$
			buoy = new BuoyCard(this, node);
			return;
		} else if (keys.containsKey("buoy_isolated_danger:shape") || keys.containsKey("beacon_isolated_danger:shape")) { //$NON-NLS-1$ //$NON-NLS-2$
			buoy = new BuoyIsol(this, node);
			return;
		} else if (keys.containsKey("buoy_safe_water:shape") || keys.containsKey("beacon_safe_water:shape")) { //$NON-NLS-1$ //$NON-NLS-2$
			buoy = new BuoySaw(this, node);
			return;
		} else if (keys.containsKey("buoy_special_purpose:shape") || keys.containsKey("beacon_special_purpose:shape")) { //$NON-NLS-1$ //$NON-NLS-2$
			buoy = new BuoySpec(this, node);
			return;
		}

		if (keys.containsKey("buoy_lateral:colour") || keys.containsKey("beacon_lateral:colour")) { //$NON-NLS-1$ //$NON-NLS-2$
			buoy = new BuoyLat(this, node);
			return;
		} else if (keys.containsKey("buoy_cardinal:colour") || keys.containsKey("beacon_cardinal:colour")) { //$NON-NLS-1$ //$NON-NLS-2$
			buoy = new BuoyCard(this, node);
			return;
		} else if (keys.containsKey("buoy_isolated_danger:colour") || keys.containsKey("beacon_isolated_danger:colour")) { //$NON-NLS-1$ //$NON-NLS-2$
			buoy = new BuoyIsol(this, node);
			return;
		} else if (keys.containsKey("buoy_safe_water:colour") || keys.containsKey("beacon_safe_water:colour")) { //$NON-NLS-1$ //$NON-NLS-2$
			buoy = new BuoySaw(this, node);
			return;
		} else if (keys.containsKey("buoy_special_purpose:colour") || keys.containsKey("beacon_special_purpose:colour")) { //$NON-NLS-1$ //$NON-NLS-2$
			buoy = new BuoySpec(this, node);
			return;
		}

		buoy = new BuoyUkn(this, Messages.getString("SmpDialogAction.91")); //$NON-NLS-1$
		buoy.setNode(node);
		buoy.paintSign();
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
			dM01SeaMap.setTitle(Messages.getString("SmpDialogAction.9")); //$NON-NLS-1$
			dM01SeaMap.setVisible(false);
			dM01SeaMap.setAlwaysOnTop(true);
			dM01SeaMap.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {

					// Pictogramme wiederherstellen und aufraeumen
					if (obuoy != null)
						PicRebuild();
					// Deaktivierung des Listeners
					DataSet.removeSelectionListener(SmpListener);
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

			lM01Icon = new JLabel();
			lM01Icon.setBounds(new Rectangle(220, 20, 150, 200));
			lM01Icon.setIcon(null);
			lM01Icon.setText(""); //$NON-NLS-1$

			lM02Icon = new JLabel();
			lM02Icon.setBounds(new Rectangle(220, 20, 150, 200));
			lM02Icon.setIcon(null);
			lM02Icon.setText(""); //$NON-NLS-1$

			lM03Icon = new JLabel();
			lM03Icon.setBounds(new Rectangle(220, 20, 150, 200));
			lM03Icon.setIcon(null);
			lM03Icon.setText(""); //$NON-NLS-1$

			lM04Icon = new JLabel();
			lM04Icon.setBounds(new Rectangle(220, 20, 150, 200));
			lM04Icon.setIcon(null);
			lM04Icon.setText(""); //$NON-NLS-1$

			lM05Icon = new JLabel();
			lM05Icon.setBounds(new Rectangle(220, 20, 150, 200));
			lM05Icon.setIcon(null);
			lM05Icon.setText(""); //$NON-NLS-1$

			lM06Icon = new JLabel();
			lM06Icon.setBounds(new Rectangle(220, 20, 150, 200));
			lM06Icon.setIcon(null);
			lM06Icon.setText(""); //$NON-NLS-1$

			lM01FireMark = new JLabel();
			lM01FireMark.setBounds(new Rectangle(315, 85, 80, 20));
			lM01FireMark.setFont(new Font("Dialog", Font.PLAIN, 10)); //$NON-NLS-1$
			lM01FireMark.setText(""); //$NON-NLS-1$

			lM01NameMark = new JLabel();
			lM01NameMark.setBounds(new Rectangle(315, 65, 80, 20));
			lM01NameMark.setFont(new Font("Dialog", Font.PLAIN, 10)); //$NON-NLS-1$
			lM01NameMark.setText(""); //$NON-NLS-1$

			lM01FogMark = new JLabel();
			lM01FogMark.setBounds(new Rectangle(220, 85, 70, 20));
			lM01FogMark.setFont(new Font("Dialog", Font.PLAIN, 10)); //$NON-NLS-1$
			lM01FogMark.setText(""); //$NON-NLS-1$

			lM01RadarMark = new JLabel();
			lM01RadarMark.setBounds(new Rectangle(230, 65, 70, 20));
			lM01RadarMark.setFont(new Font("Dialog", Font.PLAIN, 10)); //$NON-NLS-1$
			lM01RadarMark.setText(""); //$NON-NLS-1$

			lM01Head = new JLabel();
			lM01Head.setBounds(new Rectangle(5, 3, 316, 16));
			lM01Head.setText(Messages.getString("SmpDialogAction.97")); //$NON-NLS-1$

			lM01Region = new JLabel();
			lM01Region.setBounds(new Rectangle(220, 7, 120, 16));
			lM01Region.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			lM01Region.setText(Messages.getString("SmpDialogAction.99")); //$NON-NLS-1$

			lM02Region = new JLabel();
			lM02Region.setBounds(new Rectangle(270, 7, 120, 16));
			lM02Region.setFont(new Font("Dialog", Font.BOLD, 12)); //$NON-NLS-1$
			lM02Region.setText(Messages.getString("SmpDialogAction.101")); //$NON-NLS-1$

			lM01TypeOfMark = new JLabel();
			lM01TypeOfMark.setBounds(new Rectangle(5, 28, 120, 16));
			lM01TypeOfMark.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			lM01TypeOfMark.setText(Messages.getString("SmpDialogAction.103")); //$NON-NLS-1$

			lM01CatOfMark = new JLabel();
			lM01CatOfMark.setBounds(new Rectangle(5, 58, 120, 16));
			lM01CatOfMark.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			lM01CatOfMark.setText(Messages.getString("SmpDialogAction.1")); //$NON-NLS-1$

			lM01StyleOfMark = new JLabel();
			lM01StyleOfMark.setBounds(new Rectangle(5, 88, 148, 16));
			lM01StyleOfMark.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			lM01StyleOfMark.setText(Messages.getString("SmpDialogAction.107")); //$NON-NLS-1$

			lM01Name = new JLabel();
			lM01Name.setBounds(new Rectangle(5, 120, 82, 16));
			lM01Name.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			lM01Name.setText(Messages.getString("SmpDialogAction.109")); //$NON-NLS-1$

			lM01Props02 = new JLabel();
			lM01Props02.setBounds(new Rectangle(5, 150, 172, 16));
			lM01Props02.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			lM01Props02.setText(Messages.getString("SmpDialogAction.111")); //$NON-NLS-1$

			lM01Racon = new JLabel();
			lM01Racon.setBounds(new Rectangle(335, 195, 65, 20));
			lM01Racon.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			lM01Racon.setText(Messages.getString("SmpDialogAction.113")); //$NON-NLS-1$

			lM01FogGroup = new JLabel();
			lM01FogGroup.setBounds(new Rectangle(190, 220, 100, 20));
			lM01FogGroup.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			lM01FogGroup.setText(Messages.getString("SmpDialogAction.115")); //$NON-NLS-1$

			lM01FogPeriod = new JLabel();
			lM01FogPeriod.setBounds(new Rectangle(300, 220, 100, 20));
			lM01FogPeriod.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			lM01FogPeriod.setText(Messages.getString("SmpDialogAction.117")); //$NON-NLS-1$

			lM01Kennung = new JLabel();
			lM01Kennung.setBounds(new Rectangle(235, 245, 70, 20));
			lM01Kennung.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			lM01Kennung.setText(Messages.getString("SmpDialogAction.119")); //$NON-NLS-1$

			lM01Height = new JLabel();
			lM01Height.setBounds(new Rectangle(10, 270, 100, 20));
			lM01Height.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			lM01Height.setText(Messages.getString("SmpDialogAction.121")); //$NON-NLS-1$

			lM01Range = new JLabel();
			lM01Range.setBounds(new Rectangle(108, 270, 100, 20));
			lM01Range.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			lM01Range.setText(Messages.getString("SmpDialogAction.123")); //$NON-NLS-1$

			lM01Group = new JLabel();
			lM01Group.setBounds(new Rectangle(204, 270, 100, 20));
			lM01Group.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			lM01Group.setText(Messages.getString("SmpDialogAction.125")); //$NON-NLS-1$

			lM01RepeatTime = new JLabel();
			lM01RepeatTime.setBounds(new Rectangle(300, 270, 100, 20));
			lM01RepeatTime.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			lM01RepeatTime.setText(Messages.getString("SmpDialogAction.127")); //$NON-NLS-1$

			lM01Sector = new JLabel();
			lM01Sector.setBounds(new Rectangle(10, 295, 180, 20));
			lM01Sector.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			lM01Sector.setText(Messages.getString("SmpDialogAction.129")); //$NON-NLS-1$

			lM01Colour = new JLabel();
			lM01Colour.setBounds(new Rectangle(120, 295, 180, 20));
			lM01Colour.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			lM01Colour.setText(Messages.getString("SmpDialogAction.131")); //$NON-NLS-1$

			lM01Bearing = new JLabel();
			lM01Bearing.setBounds(new Rectangle(228, 295, 180, 20));
			lM01Bearing.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			lM01Bearing.setText(Messages.getString("SmpDialogAction.133")); //$NON-NLS-1$

			rbM01RegionA = new JRadioButton(
					Messages.getString("SmpDialogAction.134"), Main.pref.get("tomsplugin.IALA") //$NON-NLS-1$ //$NON-NLS-2$
							.equals("A")); //$NON-NLS-1$
			rbM01RegionA.setBounds(new Rectangle(305, 0, 50, 30));
			rbM01RegionB = new JRadioButton("-B", Main.pref.get("tomsplugin.IALA") //$NON-NLS-1$ //$NON-NLS-2$
					.equals("B")); //$NON-NLS-1$
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

			rbM01Fired1 = new JRadioButton(
					Messages.getString("SmpDialogAction.140"), true); //$NON-NLS-1$
			rbM01Fired1.setBounds(new Rectangle(85, 240, 70, 30));
			rbM01FiredN = new JRadioButton(
					Messages.getString("SmpDialogAction.141"), false); //$NON-NLS-1$
			rbM01FiredN.setBounds(new Rectangle(155, 240, 80, 30));
			bgM01Fired = new ButtonGroup();
			bgM01Fired.add(rbM01Fired1);
			bgM01Fired.add(rbM01FiredN);

			ActionListener alM01Fired = new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					buoy.setSectored(rbM01FiredN.isSelected());
					cbM01Sector.setSelectedIndex(0);
					buoy.setSectorIndex(0);
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
			pM01SeaMap.add(lM01Icon, null);
			pM01SeaMap.add(lM02Icon, null);
			pM01SeaMap.add(lM03Icon, null);
			pM01SeaMap.add(lM04Icon, null);
			pM01SeaMap.add(lM05Icon, null);
			pM01SeaMap.add(lM06Icon, null);
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
			pM01SeaMap.add(lM01Group, null);
			pM01SeaMap.add(getTfM01Group(), null);
			pM01SeaMap.add(lM01Sector, null);
			pM01SeaMap.add(getCbM01Sector(), null);
			pM01SeaMap.add(lM01Colour, null);
			pM01SeaMap.add(getCbM01Colour(), null);
			pM01SeaMap.add(lM01Bearing, null);
			pM01SeaMap.add(getTfM01Bearing(), null);
			pM01SeaMap.add(getTfM02Bearing(), null);
			pM01SeaMap.add(getTfM01Radius(), null);
			pM01SeaMap.add(lM01Height, null);
			pM01SeaMap.add(getTfM01Height(), null);
			pM01SeaMap.add(lM01Range, null);
			pM01SeaMap.add(getTfM01Range(), null);
			pM01SeaMap.add(lM01FireMark, null);
			pM01SeaMap.add(lM01NameMark, null);
			pM01SeaMap.add(lM01FogMark, null);
			pM01SeaMap.add(lM01RadarMark, null);
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
			cbM01TypeOfMark.addItem(Messages.getString("SmpDialogAction.142")); //$NON-NLS-1$
			cbM01TypeOfMark.addItem(Messages.getString("SmpDialogAction.143")); //$NON-NLS-1$
			cbM01TypeOfMark.addItem(Messages.getString("SmpDialogAction.144")); //$NON-NLS-1$
			cbM01TypeOfMark.addItem(Messages.getString("SmpDialogAction.145")); //$NON-NLS-1$
			cbM01TypeOfMark.addItem(Messages.getString("SmpDialogAction.146")); //$NON-NLS-1$
			cbM01TypeOfMark.addItem(Messages.getString("SmpDialogAction.147")); //$NON-NLS-1$
			cbM01TypeOfMark.addItem(Messages.getString("SmpDialogAction.148")); //$NON-NLS-1$

			cbM01TypeOfMark.setBounds(new Rectangle(50, 25, 170, 25));
			// cbM01TypeOfMark.setEditable(false);
			cbM01TypeOfMark.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			cbM01TypeOfMark.setEnabled(true);

			cbM01TypeOfMark.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int type = cbM01TypeOfMark.getSelectedIndex();

					if (buoy == null) {
						buoy = new BuoyUkn(dia, Messages.getString("SmpDialogAction.150")); //$NON-NLS-1$
						buoy.paintSign();
						return;
					}

					Node n = buoy.getNode();
					if (n == null)
						return;

					paintlock = true;
					switch (type) {

					case SeaMark.UNKNOWN_TYPE:
						if (!(buoy instanceof BuoyUkn)) {
							buoy = null;
							buoy = new BuoyUkn(dia, Messages.getString("SmpDialogAction.150")); //$NON-NLS-1$
						}
						buoy.setBuoyIndex(0);
						break;

					case SeaMark.LATERAL:
						if (!(buoy instanceof BuoyLat)) {
							buoy = null;
							buoy = new BuoyLat(dia, n);
							buoy.setBuoyIndex(0);
						}
						break;

					case SeaMark.CARDINAL:
						if (!(buoy instanceof BuoyCard)) {
							buoy = null;
							buoy = new BuoyCard(dia, n);
							buoy.setBuoyIndex(0);
						}
						break;

					case SeaMark.SAFE_WATER:
						if (!(buoy instanceof BuoySaw)) {
							buoy = null;
							buoy = new BuoySaw(dia, n);
						}
						buoy.setBuoyIndex(type);
						break;

					case SeaMark.ISOLATED_DANGER:
						if (!(buoy instanceof BuoyIsol)) {
							buoy = null;
							buoy = new BuoyIsol(dia, n);
						}
						buoy.setBuoyIndex(type);
						break;

					case SeaMark.SPECIAL_PURPOSE:
						if (!(buoy instanceof BuoySpec)) {
							buoy = null;
							buoy = new BuoySpec(dia, n);
						}
						buoy.setBuoyIndex(type);
						break;

					case SeaMark.LIGHT:
						if (!(buoy instanceof BuoyNota)) {
							buoy = null;
							buoy = new BuoyNota(dia, n);
							buoy.setBuoyIndex(0);
						}
						break;
					}

					buoy.refreshLights();
					buoy.setLightColour();
					paintlock = false;
					buoy.paintSign();
				}
			});
		}
		return cbM01TypeOfMark;
	}

	private JComboBox getCbM01CatOfMark() {
		if (cbM01CatOfMark == null) {
			cbM01CatOfMark = new JComboBox();
			cbM01CatOfMark.setBounds(new Rectangle(65, 55, 155, 25));
			cbM01CatOfMark.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			cbM01CatOfMark.setEnabled(true);

			cbM01CatOfMark.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (buoy == null)
						return;
					buoy.setBuoyIndex(cbM01CatOfMark.getSelectedIndex());
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
			cbM01StyleOfMark.setBounds(new Rectangle(50, 85, 170, 25));
			cbM01StyleOfMark.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			cbM01StyleOfMark.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (buoy == null)
						return;
					buoy.setStyleIndex(cbM01StyleOfMark.getSelectedIndex());
					buoy.refreshLights();
					buoy.setLightColour();
					buoy.paintSign();
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
					if (buoy == null)
						return;
					buoy.setName(tfM01Name.getText());
					buoy.paintSign();
				}
			});
		}
		return tfM01Name;
	}

	private JCheckBox getCM01TopMark() {
		if (cM01TopMark == null) {
			cM01TopMark = new JCheckBox();
			cM01TopMark.setBounds(new Rectangle(10, 170, 100, 20));
			cM01TopMark.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			cM01TopMark.setText(Messages.getString("SmpDialogAction.166")); //$NON-NLS-1$
			cM01TopMark.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (buoy == null)
						return;
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
			cbM01TopMark.setBounds(new Rectangle(110, 170, 80, 20));
			cbM01TopMark.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			cbM01TopMark.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (buoy == null)
						return;
					buoy.setTopMarkIndex(cbM01TopMark.getSelectedIndex());
					buoy.paintSign();
				}
			});
		}
		return cbM01TopMark;
	}

	private JCheckBox getCM01Radar() {
		if (cM01Radar == null) {
			cM01Radar = new JCheckBox();
			cM01Radar.setBounds(new Rectangle(10, 195, 120, 20));
			cM01Radar.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			cM01Radar.setText(Messages.getString("SmpDialogAction.169")); //$NON-NLS-1$
			cM01Radar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (buoy == null)
						return;
					if (cM01Radar.isSelected()) {
						buoy.setRadar(true);
						buoy.setRacon(false);
						cM01Racon.setSelected(false);
					} else {
						buoy.setRadar(false);
					}
					buoy.paintSign();
				}
			});
		}
		return cM01Radar;
	}

	private JCheckBox getCM01Racon() {
		if (cM01Racon == null) {
			cM01Racon = new JCheckBox();
			cM01Racon.setBounds(new Rectangle(130, 195, 110, 20));
			cM01Racon.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			cM01Racon.setText(Messages.getString("SmpDialogAction.171")); //$NON-NLS-1$
			cM01Racon.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (buoy == null)
						return;
					if (cM01Racon.isSelected()) {
						buoy.setRacon(true);
						buoy.setRadar(false);
						cM01Radar.setSelected(false);
					} else {
						buoy.setRacon(false);
					}
					buoy.paintSign();
				}
			});
		}
		return cM01Racon;
	}

	private JComboBox getCbM01Racon() {
		if (cbM01Racon == null) {
			cbM01Racon = new JComboBox();
			cbM01Racon.setBounds(new Rectangle(240, 195, 80, 20));
			cbM01Racon.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			cbM01Racon.removeAllItems();
			cbM01Racon.addItem(Messages.getString("SmpDialogAction.8")); //$NON-NLS-1$
			cbM01Racon.addItem(Messages.getString("SmpDialogAction.10")); //$NON-NLS-1$
			cbM01Racon.addItem(Messages.getString("SmpDialogAction.11")); //$NON-NLS-1$
			cbM01Racon.addItem(Messages.getString("SmpDialogAction.12")); //$NON-NLS-1$
			cbM01Racon.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (buoy == null)
						return;
					int rac = cbM01Racon.getSelectedIndex();
					buoy.setRaType(rac);
					buoy.paintSign();
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
					if (buoy == null)
						return;
					buoy.setRaconGroup(tfM01Racon.getText().trim());
					buoy.paintSign();
				}
			});
		}
		return tfM01Racon;
	}

	private JCheckBox getCM01Fog() {
		if (cM01Fog == null) {
			cM01Fog = new JCheckBox();
			cM01Fog.setBounds(new Rectangle(10, 220, 90, 20));
			cM01Fog.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			cM01Fog.setText(Messages.getString("SmpDialogAction.174")); //$NON-NLS-1$
			cM01Fog.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (buoy == null)
						return;
					buoy.setFog(cM01Fog.isSelected());
					buoy.paintSign();
				}
			});
		}
		return cM01Fog;
	}

	private JComboBox getCbM01Fog() {
		if (cbM01Fog == null) {
			cbM01Fog = new JComboBox();
			cbM01Fog.setBounds(new Rectangle(100, 220, 75, 20));
			cbM01Fog.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			cbM01Fog.removeAllItems();
			cbM01Fog.addItem(Messages.getString("SmpDialogAction.8")); //$NON-NLS-1$
			cbM01Fog.addItem(Messages.getString("SmpDialogAction.14")); //$NON-NLS-1$
			cbM01Fog.addItem(Messages.getString("SmpDialogAction.15")); //$NON-NLS-1$
			cbM01Fog.addItem(Messages.getString("SmpDialogAction.16")); //$NON-NLS-1$
			cbM01Fog.addItem(Messages.getString("SmpDialogAction.17")); //$NON-NLS-1$
			cbM01Fog.addItem(Messages.getString("SmpDialogAction.18")); //$NON-NLS-1$
			cbM01Fog.addItem(Messages.getString("SmpDialogAction.19")); //$NON-NLS-1$
			cbM01Fog.addItem(Messages.getString("SmpDialogAction.20")); //$NON-NLS-1$
			cbM01Fog.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (buoy == null)
						return;
					if (cbM01Fog.getSelectedIndex() > 0)
						buoy.setFogSound(cbM01Fog.getSelectedIndex());
					else
						buoy.setFogSound(0);
					buoy.paintSign();
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
					if (buoy == null)
						return;
					buoy.setFogGroup(tfM01FogGroup.getText().trim());
					buoy.paintSign();
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
					if (buoy == null)
						return;
					buoy.setFogPeriod(tfM01FogPeriod.getText().trim());
					buoy.paintSign();
				}
			});
		}
		return tfM01FogPeriod;
	}

	private JCheckBox getCM01Fired() {
		if (cM01Fired == null) {
			cM01Fired = new JCheckBox();
			cM01Fired.setBounds(new Rectangle(10, 245, 75, 20));
			cM01Fired.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			cM01Fired.setText(Messages.getString("SmpDialogAction.177")); //$NON-NLS-1$
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
			cbM01Kennung.setBounds(new Rectangle(300, 245, 75, 20));
			cbM01Kennung.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int i1, i2;
					String c = ""; //$NON-NLS-1$ //$NON-NLS-2$
					String it = (String) cbM01Kennung.getSelectedItem();

					if (it == null)
						return;
					if (it.equals(Messages.getString("SmpDialogAction.212"))) //$NON-NLS-1$
						return;
					if (buoy == null)
						return;

					if (it.contains("(")) { //$NON-NLS-1$
						i1 = it.indexOf("("); //$NON-NLS-1$
						i2 = it.indexOf(")"); //$NON-NLS-1$
						c = it.substring(i1 + 1, i2);
						it = it.substring(0, i1) + it.substring(i2 + 1);
					}
					if (!c.isEmpty())
						buoy.setLightGroup(c);
					;
					buoy.setLightChar(it);
					buoy.paintSign();
				}
			});
		}
		return cbM01Kennung;
	}

	private JTextField getTfM01Height() {
		if (tfM01Height == null) {
			tfM01Height = new JTextField();
			tfM01Height.setBounds(new Rectangle(54, 270, 30, 20));
			tfM01Height.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					if (buoy == null)
						return;
					buoy.setHeight(tfM01Height.getText().trim());
					buoy.paintSign();
				}
			});
		}
		return tfM01Height;
	}

	private JTextField getTfM01Range() {
		if (tfM01Range == null) {
			tfM01Range = new JTextField();
			tfM01Range.setBounds(new Rectangle(151, 270, 30, 20));
			tfM01Range.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					if (buoy == null)
						return;
					buoy.setRange(tfM01Range.getText().trim());
					buoy.paintSign();
				}
			});
		}
		return tfM01Range;
	}

	private JTextField getTfM01Group() {
		if (tfM01Group == null) {
			tfM01Group = new JTextField();
			tfM01Group.setBounds(new Rectangle(255, 270, 30, 20));
			tfM01Group.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					if (buoy == null)
						return;
					buoy.setLightGroup(tfM01Group.getText().trim());
					buoy.paintSign();
				}
			});
		}
		return tfM01Group;
	}

	private JTextField getTfM01RepeatTime() {
		if (tfM01RepeatTime == null) {
			tfM01RepeatTime = new JTextField();
			tfM01RepeatTime.setBounds(new Rectangle(345, 270, 30, 20));
			tfM01RepeatTime.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (buoy == null)
						return;
					buoy.setLightPeriod(tfM01RepeatTime.getText().trim());
					buoy.paintSign();
				}
			});

			tfM01RepeatTime.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					if (buoy == null)
						return;
					buoy.setLightPeriod(tfM01RepeatTime.getText().trim());
					buoy.paintSign();
				}
			});
		}
		return tfM01RepeatTime;
	}

	private JComboBox getCbM01Colour() {
		if (cbM01Colour == null) {
			cbM01Colour = new JComboBox();
			cbM01Colour.setBounds(new Rectangle(165, 295, 40, 20));
			cbM01Colour.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			cbM01Colour.addItem(""); //$NON-NLS-1$
			cbM01Colour.addItem(Messages.getString("SmpDialogAction.190")); //$NON-NLS-1$
			cbM01Colour.addItem(Messages.getString("SmpDialogAction.191")); //$NON-NLS-1$
			cbM01Colour.addItem(Messages.getString("SmpDialogAction.192")); //$NON-NLS-1$
			cbM01Colour.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (buoy == null)
						return;
					buoy.setLightColour((String) cbM01Colour.getSelectedItem());
					buoy.paintSign();
				}
			});
		}
		return cbM01Colour;
	}

	private JComboBox getCbM01Sector() {
		if (cbM01Sector == null) {
			cbM01Sector = new JComboBox();
			cbM01Sector.setBounds(new Rectangle(55, 295, 50, 20));
			cbM01Sector.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			cbM01Sector.addItem(Messages.getString("SmpDialogAction.194")); //$NON-NLS-1$
			cbM01Sector.addItem(Messages.getString("SmpDialogAction.195")); //$NON-NLS-1$
			cbM01Sector.addItem(Messages.getString("SmpDialogAction.196")); //$NON-NLS-1$
			cbM01Sector.addItem(Messages.getString("SmpDialogAction.197")); //$NON-NLS-1$
			cbM01Sector.addItem(Messages.getString("SmpDialogAction.198")); //$NON-NLS-1$
			cbM01Sector.addItem(Messages.getString("SmpDialogAction.199")); //$NON-NLS-1$
			cbM01Sector.addItem(Messages.getString("SmpDialogAction.200")); //$NON-NLS-1$
			cbM01Sector.addItem(Messages.getString("SmpDialogAction.201")); //$NON-NLS-1$
			cbM01Sector.addItem(Messages.getString("SmpDialogAction.202")); //$NON-NLS-1$
			cbM01Sector.addItem(Messages.getString("SmpDialogAction.203")); //$NON-NLS-1$
			cbM01Sector.addItem("10");
			cbM01Sector.addItem("11");
			cbM01Sector.addItem("12");
			cbM01Sector.addItem("13");
			cbM01Sector.addItem("14");
			cbM01Sector.addItem("15");
			cbM01Sector.addItem("16");
			cbM01Sector.addItem("17");
			cbM01Sector.addItem("18");
			cbM01Sector.addItem("19");
			cbM01Sector.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (buoy == null)
						return;
					buoy.setSectorIndex(cbM01Sector.getSelectedIndex());
					buoy.paintSign();
				}
			});
		}
		return cbM01Sector;
	}

	private JTextField getTfM01Bearing() {
		if (tfM01Bearing == null) {
			tfM01Bearing = new JTextField();
			tfM01Bearing.setBounds(new Rectangle(255, 295, 30, 20));
			tfM01Bearing.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					if (buoy == null)
						return;
					buoy.setBearing1(tfM01Bearing.getText().trim());
				}
			});
		}
		return tfM01Bearing;
	}

	private JTextField getTfM02Bearing() {
		if (tfM02Bearing == null) {
			tfM02Bearing = new JTextField();
			tfM02Bearing.setBounds(new Rectangle(300, 295, 30, 20));
			tfM02Bearing.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					buoy.setBearing2(tfM02Bearing.getText().trim());
				}
			});
		}
		return tfM02Bearing;
	}

	private JTextField getTfM01Radius() {
		if (tfM01Radius == null) {
			tfM01Radius = new JTextField();
			tfM01Radius.setBounds(new Rectangle(355, 295, 30, 20));
			tfM01Radius.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					if (buoy == null)
						return;
					buoy.setRadius(tfM01Radius.getText().trim());
				}
			});
		}
		return tfM01Radius;
	}

	private JButton getBM01Close() {
		if (bM01Close == null) {
			bM01Close = new JButton();
			bM01Close.setBounds(new Rectangle(20, 325, 110, 20));
			bM01Close.setText(tr("Close")); //$NON-NLS-1$
			bM01Close.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// aufraeumen
					if (obuoy != null)
						PicRebuild();
					// Deaktivierung des Listeners
					DataSet.removeSelectionListener(SmpListener);
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
			bM01Save.setBounds(new Rectangle(150, 325, 110, 20));
			bM01Save.setText(tr("Save")); //$NON-NLS-1$
			bM01Save.setEnabled(false);

			bM01Save.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cM01IconVisible.setIcon(new ImageIcon(getClass().getResource(
							"/images/Auge.png"))); //$NON-NLS-1$
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
					"/images/AugeN.png"))); //$NON-NLS-1$
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
								"/images/AugeN.png"))); //$NON-NLS-1$
						if (n != null) {
							// seamark loeschen, wenn notwendig
							if (n.getKeys().containsKey("seamark")) { //$NON-NLS-1$
								smb = n.getKeys().get("seamark"); // smb merken //$NON-NLS-1$

								c = new ChangePropertyCommand(n, "seamark", null); //$NON-NLS-1$
								c.executeCommand();
								ds.fireSelectionChanged();
								obuoy = buoy;
							}

							// seamark:type loeschen, wenn notwendig
							if (n.getKeys().containsKey("seamark:type")) { //$NON-NLS-1$
								smt = n.getKeys().get("seamark:type"); // smt merken //$NON-NLS-1$

								c = new ChangePropertyCommand(n, "seamark:type", null); //$NON-NLS-1$
								c.executeCommand();
								ds.fireSelectionChanged();
								obuoy = buoy;
							}

						}
					} else {
						cM01IconVisible.setIcon(new ImageIcon(getClass().getResource(
								"/images/Auge.png"))); //$NON-NLS-1$
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
