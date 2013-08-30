/* Copyright 2013 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package smed2;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.*;

import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.EditLayerChangeListener;
import org.openstreetmap.josm.gui.layer.*;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.imagery.ImageryInfo;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.data.osm.event.*;
import org.openstreetmap.josm.Main;

import s57.S57dat;
import seamap.SeaMap;

import panels.PanelMain;
import panels.ShowFrame;

public class Smed2Action extends JosmAction implements EditLayerChangeListener, SelectionChangedListener {

	private static String editor = tr("SeaMap Editor");
	public static JFrame editFrame = null;
	public static ShowFrame showFrame = null;
	public static S57dat panelS57;
	private boolean isOpen = false;
	public static PanelMain panelMain = null;
	public MapImage rendering;
	public SeaMap map = null;
	public DataSet data = null;

	private final DataSetListener dataSetListener = new DataSetListener() {

		@Override
		public void dataChanged(DataChangedEvent e) {
			makeMap();
		}

		@Override
		public void nodeMoved(NodeMovedEvent e) {
			makeMap();
		}

		@Override
		public void otherDatasetChange(AbstractDatasetChangedEvent e) {
			makeMap();
		}

		@Override
		public void primitivesAdded(PrimitivesAddedEvent e) {
			makeMap();
		}

		@Override
		public void primitivesRemoved(PrimitivesRemovedEvent e) {
			makeMap();
		}

		@Override
		public void relationMembersChanged(RelationMembersChangedEvent e) {
			makeMap();
		}

		@Override
		public void tagsChanged(TagsChangedEvent e) {
			makeMap();
		}

		@Override
		public void wayNodesChanged(WayNodesChangedEvent e) {
			makeMap();
		}
	};

	public Smed2Action() {
		super(editor, "Smed2", editor, null, true);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (!isOpen)
					createFrame();
				else
					editFrame.toFront();
				isOpen = true;
			}
		});
	}

	protected void createFrame() {
		editFrame = new JFrame(editor);
		editFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		editFrame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				closeDialog();
			}
		});
		editFrame.setSize(new Dimension(480, 480));
		editFrame.setLocation(100, 200);
		editFrame.setResizable(true);
		editFrame.setAlwaysOnTop(true);
		editFrame.setVisible(false);
		panelMain = new PanelMain();
		editFrame.add(panelMain);
		panelS57 = new S57dat();
		panelS57.setVisible(false);
		editFrame.add(panelS57);

		showFrame = new ShowFrame("Seamark Inspector");
		showFrame.setSize(new Dimension(300, 300));
		Rectangle rect = Main.map.mapView.getBounds();
		showFrame.setLocation(50, (rect.y + rect.height - 200));
		showFrame.setResizable(false);
		showFrame.setAlwaysOnTop(true);
		showFrame.setEnabled(false);
		showFrame.setVisible(false);

		// System.out.println("hello");
		rendering = new MapImage(new ImageryInfo("OpenSeaMap"), this);
		rendering.setBackgroundLayer(true);
		Main.main.addLayer(rendering);
		MapView.addEditLayerChangeListener(this);
		DataSet.addSelectionListener(this);
		editLayerChanged(Main.main.getEditLayer(), Main.main.getEditLayer());
	}

	public void closeDialog() {
		if (isOpen) {
			Main.main.removeLayer(rendering);
			MapView.removeEditLayerChangeListener(this);
			editFrame.setVisible(false);
			editFrame.dispose();
			data = null;
			map = null;
		}
		isOpen = false;
	}

	@Override
	public void editLayerChanged(OsmDataLayer oldLayer, OsmDataLayer newLayer) {
		if (oldLayer != null) {
			oldLayer.data.removeDataSetListener(dataSetListener);
		}
		if (newLayer != null) {
			newLayer.data.addDataSetListener(dataSetListener);
			data = newLayer.data;
			makeMap();
		} else {
			data = null;
			map = null;
		}
	}

	@Override
	public void selectionChanged(Collection<? extends OsmPrimitive> selection) {
		OsmPrimitive nextFeature = null;
		OsmPrimitive feature = null;

		if (selection.size() == 0) showFrame.setVisible(false);
		for (OsmPrimitive osm : selection) {
				nextFeature = osm;
				if (selection.size() == 1) {
					if (nextFeature.compareTo(feature) != 0) {
						feature = nextFeature;
//						showFrame.setVisible(true);
						showFrame.showFeature(feature, map);
					}
				} else {
					showFrame.setVisible(false);
				}
			}
		if (nextFeature == null) {
			feature = null;
		}
	}

	void makeMap() {
		map = new SeaMap();
		if (data != null) {
			for (Node node : data.getNodes()) {
				map.addNode(node.getUniqueId(), node.getCoor().lat(), node.getCoor().lon());
				for (Entry<String, String> entry : node.getKeys().entrySet()) {
					map.addTag(entry.getKey(), entry.getValue());
				}
				map.tagsDone(node.getUniqueId());
			}
			for (Way way : data.getWays()) {
				map.addEdge(way.getUniqueId());
				for (Node node : way.getNodes()) {
					map.addToEdge((node.getUniqueId()));
				}
				for (Entry<String, String> entry : way.getKeys().entrySet()) {
					map.addTag(entry.getKey(), entry.getValue());
				}
				map.tagsDone(way.getUniqueId());
			}
			for (Relation rel : data.getRelations()) {
				if (rel.isMultipolygon()) {
					map.addArea(rel.getUniqueId());
					for (RelationMember mem : rel.getMembers()) {
						if (mem.getType() == OsmPrimitiveType.WAY)
							map.addToArea(mem.getUniqueId(), (mem.getRole().equals("outer")));
					}
				}
				for (Entry<String, String> entry : rel.getKeys().entrySet()) {
					map.addTag(entry.getKey(), entry.getValue());
				}
				map.tagsDone(rel.getUniqueId());
			}
			if (rendering != null) rendering.zoomChanged();
		}
	}
}
