package oseam.dialogs;

import oseam.panels.*;

import java.awt.*;
import java.util.*;
import javax.swing.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.*;
import org.openstreetmap.josm.data.osm.*;

import oseam.Messages;
import oseam.seamarks.SeaMark;
import smed.plug.ifc.SmedPluginManager;

public class OSeaMAction {

	private OSeaMAction dlg = null;
	public SmedPluginManager manager = null;;
	public PanelMain panelMain = null;

	public Node node = null;
	private Collection<? extends OsmPrimitive> Selection = null;

	public SelectionChangedListener SmpListener = new SelectionChangedListener() {
		public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
			Node nextNode = null;
			Selection = newSelection;

			for (OsmPrimitive osm : Selection) {
				if (osm instanceof Node) {
					nextNode = (Node) osm;
					if (Selection.size() == 1) {
						if (nextNode.compareTo(node) != 0) {
							node = nextNode;
							panelMain.mark.parseMark(node);
						}
					} else {
						node = null;
						panelMain.mark.clearSign();
						panelMain.syncPanel();
						manager.showVisualMessage(Messages.getString("OneNode"));
					}
				}
			}
			if (nextNode == null) {
				node = null;
				panelMain.mark.clearSign();
				panelMain.syncPanel();
				manager.showVisualMessage(Messages.getString("SelectNode"));
			}
		}
	};

	public OSeaMAction(SmedPluginManager mngr) {

		// System.out.println("hello");
		dlg = this;
		manager = mngr;
		DataSet.addSelectionListener(SmpListener);
		String str = Main.pref.get("mappaint.style.sources");
		if (!str.contains("dev.openseamap.org")) {
			if (!str.isEmpty())
				str += new String(new char[] { 0x1e });
			Main.pref.put("mappaint.style.sources", str + "http://dev.openseamap.org/josm/seamark_styles.xml");
		}
	}

	public JPanel getOSeaMPanel() {
		if (panelMain == null) {
			panelMain = new PanelMain(this);
			panelMain.setLayout(null);
			panelMain.setSize(new Dimension(400, 360));
			node = null;
			panelMain.syncPanel();
		}
		return panelMain;
	}

}
