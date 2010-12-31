package oseam.dialogs;

import static org.openstreetmap.josm.tools.I18n.tr;

import oseam.panels.*;

import java.awt.Dimension;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

import oseam.Messages;
import oseam.seamarks.SeaMark;
import oseam.seamarks.SeaMark.Obj;
import smed.plug.ifc.SmedPluginManager;

public class OSeaMAction {

	private SmedPluginManager manager;
	public PanelMain panelMain = null;

	public SeaMark mark = null;
	public Node node = null;
	private Collection<? extends OsmPrimitive> Selection = null;

	public SelectionChangedListener SmpListener = new SelectionChangedListener() {
		public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
			Node nextNode = null;
			Selection = newSelection;

			// System.out.println("hello");
			for (OsmPrimitive osm : Selection) {
				if (osm instanceof Node) {
					nextNode = (Node) osm;
					if (Selection.size() == 1) {
						if (nextNode.compareTo(node) != 0) {
							node = nextNode;
							parseNode();
						}
					} else
						manager.showVisualMessage(Messages.getString("OneNode"));
				}
			}
			if (nextNode == null) {
				node = null;
				mark = null;
				panelMain.clearSelections();
				manager.showVisualMessage(Messages.getString("SelectNode"));
			}
		}
	};

	public OSeaMAction(SmedPluginManager mngr) {

		DataSet.addSelectionListener(SmpListener);
		manager = mngr;
		String str = Main.pref.get("mappaint.style.sources");
		if (!str.contains("dev.openseamap.org")) {
			if (!str.isEmpty())
				str += new String(new char[] { 0x1e });
			Main.pref.put("mappaint.style.sources", str + "http://dev.openseamap.org/josm/seamark_styles.xml");
		}
		str = Main.pref.get("color.background");
		if (str.equals("#000000") || str.isEmpty())
			Main.pref.put("color.background", "#606060");
	}

	public JPanel getOSeaMPanel() {
		if (panelMain == null) {
			panelMain = new PanelMain(this);
			panelMain.setLayout(null);
			panelMain.setSize(new Dimension(400, 360));
		}
		return panelMain;
	}

	private void parseNode() {

		Map<String, String> keys;

		manager.showVisualMessage("");
		String type = "";
		String str = "";

		mark = new SeaMark(this);
		keys = node.getKeys();

		if (keys.containsKey("seamark:type"))
			type = keys.get("seamark:type");

		Iterator<Obj> it = mark.objects.keySet().iterator();
		while (it.hasNext()) {
			Obj obj = it.next();
			if (mark.objects.get(obj).equals(type)) {
				mark.setObject(obj);
			}
		}

		if (type.equals("light_float")) {
			if (keys.containsKey("seamark:light_float:colour")) {
				str = keys.get("seamark:light_float:colour");
				if (str.equals("red") || str.equals("green") || str.equals("red;green;red") || str.equals("green;red;green")) {
					mark.setObject(Obj.BOYLAT);
				} else if (str.equals("black;yellow") || str.equals("black;yellow;black") || str.equals("yellow;black")
						|| str.equals("yellow;black;yellow")) {
					mark.setObject(Obj.BOYCAR);
				} else if (str.equals("black;red;black")) {
					mark.setObject(Obj.BOYISD);
				} else if (str.equals("red;white")) {
					mark.setObject(Obj.BOYSAW);
				} else if (str.equals("yellow")) {
					mark.setObject(Obj.BOYSPP);
				}
			} else if (keys.containsKey("seamark:light_float:topmark:shape")) {
				str = keys.get("seamark:light_float:topmark:shape");
				if (str.equals("cylinder") || str.equals("cone, point up")) {
					mark.setObject(Obj.BOYLAT);
				}
			} else if (keys.containsKey("seamark:light_float:topmark:colour")) {
				str = keys.get("seamark:light_float:topmark:colour");
				if (str.equals("red") || str.equals("green")) {
					mark.setObject(Obj.BOYLAT);
				}
			}
		} else if (keys.containsKey("buoy_lateral:category") || keys.containsKey("buoy_lateral:shape") || keys.containsKey("buoy_lateral:colour")) {
			mark.setObject(Obj.BOYLAT);
		} else if (keys.containsKey("beacon_lateral:category") || keys.containsKey("beacon_lateral:shape") || keys.containsKey("beacon_lateral:colour")) {
			mark.setObject(Obj.BCNLAT);
		} else if (keys.containsKey("buoy_cardinal:category") || keys.containsKey("buoy_cardinal:shape") || keys.containsKey("buoy_cardinal:colour")) {
			mark.setObject(Obj.BOYCAR);
		} else if (keys.containsKey("beacon_cardinal:category") || keys.containsKey("beacon_cardinal:shape") || keys.containsKey("beacon_cardinal:colour")) {
			mark.setObject(Obj.BCNCAR);
		} else if (keys.containsKey("buoy_isolated_danger:category") || keys.containsKey("buoy_isolated_danger:shape") || keys.containsKey("buoy_isolated_danger:colour")) {
			mark.setObject(Obj.BOYISD);
		} else if (keys.containsKey("beacon_isolated_danger:category") || keys.containsKey("beacon_isolated_danger:shape") || keys.containsKey("beacon_isolated_danger:colour")) {
			mark.setObject(Obj.BCNISD);
		} else if (keys.containsKey("buoy_safe_water:category") || keys.containsKey("buoy_safe_water:shape") || keys.containsKey("buoy_safe_water:colour")) {
			mark.setObject(Obj.BOYSAW);
		} else if (keys.containsKey("beacon_safe_water:category") || keys.containsKey("beacon_safe_water:shape") || keys.containsKey("beacon_safe_water:colour")) {
			mark.setObject(Obj.BCNSAW);
		} else if (keys.containsKey("buoy_special_purpose:category") || keys.containsKey("buoy_special_purpose:shape") || keys.containsKey("buoy_special_purpose:colour")) {
			mark.setObject(Obj.BOYSPP);
		} else if (keys.containsKey("beacon_special_purpose:category") || keys.containsKey("beacon_special_purpose:shape") || keys.containsKey("beacon_special_purpose:colour")) {
			mark.setObject(Obj.BCNSPP);
		}

		if (mark.getObject() == Obj.UNKNOWN) {
			manager.showVisualMessage(Messages.getString("NoMark"));
			panelMain.clearSelections();
		} else {
			if (keys.containsKey("seamark:" + type + ":name")) {
				panelMain.nameBox.setText(keys.get("seamark:" + type + ":name"));
				panelMain.nameBox.postActionEvent();
			} else if (keys.containsKey("seamark:name")) {
				panelMain.nameBox.setText(keys.get("seamark:name"));
				panelMain.nameBox.postActionEvent();
			} else if (keys.containsKey("name")) {
				panelMain.nameBox.setText(keys.get("name"));
				panelMain.nameBox.postActionEvent();
			} else
				panelMain.nameBox.setText("");
			mark.parseMark();
			mark.paintSign();
		}
	}
}
