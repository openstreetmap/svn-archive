/* Copyright 2014 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package seachart;

import java.awt.event.*;

import javax.swing.*;

import java.util.Map.Entry;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.EditLayerChangeListener;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.*;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.imagery.ImageryInfo;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.data.osm.event.*;
import org.openstreetmap.josm.Main;

import s57.S57map;

public class SeachartAction extends JosmAction implements EditLayerChangeListener, LayerChangeListener {
	private static String title = "SeaChart";
	private boolean isOpen = false;
	public static ChartImage rendering;
	public static S57map map = null;
	public DataSet data = null;

	private final DataSetListener dataSetListener = new DataSetListener() {

		@Override
		public void dataChanged(DataChangedEvent e) {
			makeChart();
		}

		@Override
		public void nodeMoved(NodeMovedEvent e) {
			makeChart();
		}

		@Override
		public void otherDatasetChange(AbstractDatasetChangedEvent e) {
			makeChart();
		}

		@Override
		public void primitivesAdded(PrimitivesAddedEvent e) {
			makeChart();
		}

		@Override
		public void primitivesRemoved(PrimitivesRemovedEvent e) {
			makeChart();
		}

		@Override
		public void relationMembersChanged(RelationMembersChangedEvent e) {
			makeChart();
		}

		@Override
		public void tagsChanged(TagsChangedEvent e) {
			makeChart();
		}

		@Override
		public void wayNodesChanged(WayNodesChangedEvent e) {
			makeChart();
		}
	};

	public SeachartAction() {
		super(title, "SC", title, null, true);
	}

	@Override
	public void activeLayerChange(Layer arg0, Layer arg1) {
	}

	@Override
	public void layerAdded(Layer arg0) {
	}

	@Override
	public void layerRemoved(Layer arg0) {
		if (arg0.getName().equals("SeaChart")) {
			closeChartLayer();
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (!isOpen)
					createChartLayer();
				isOpen = true;
			}
		});
	}

	protected void createChartLayer() {
		// System.out.println("hello");
		rendering = new ChartImage(new ImageryInfo("SeaChart"));
		rendering.setBackgroundLayer(true);
		Main.main.addLayer(rendering);
		MapView.addEditLayerChangeListener(this);
		MapView.addLayerChangeListener(this);
		editLayerChanged(Main.main.getEditLayer(), Main.main.getEditLayer());
	}

	public void closeChartLayer() {
		if (isOpen) {
			MapView.removeEditLayerChangeListener(this);
			MapView.removeLayerChangeListener(this);
			Main.main.removeLayer(rendering);
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
			makeChart();
		} else {
			data = null;
			map = null;
		}
	}

	void makeChart() {
		map = new S57map();
		if (data != null) {
			for (Node node : data.getNodes()) {
				LatLon coor = node.getCoor();
				if (coor != null) {
					map.addNode(node.getUniqueId(), node.getCoor().lat(), node.getCoor().lon());
					for (Entry<String, String> entry : node.getKeys().entrySet()) {
						map.addTag(entry.getKey(), entry.getValue());
					}
					map.tagsDone(node.getUniqueId());
				}
			}
			for (Way way : data.getWays()) {
				if (way.getNodesCount() > 0) {
					map.addEdge(way.getUniqueId());
					for (Node node : way.getNodes()) {
						map.addToEdge((node.getUniqueId()));
					}
					for (Entry<String, String> entry : way.getKeys().entrySet()) {
						map.addTag(entry.getKey(), entry.getValue());
					}
					map.tagsDone(way.getUniqueId());
				}
			}
			for (Relation rel : data.getRelations()) {
				if (rel.isMultipolygon() && (rel.getMembersCount() > 0)) {
					map.addArea(rel.getUniqueId());
					for (RelationMember mem : rel.getMembers()) {
						if (mem.getType() == OsmPrimitiveType.WAY)
							map.addToArea(mem.getUniqueId(), (mem.getRole().equals("outer")));
					}
					for (Entry<String, String> entry : rel.getKeys().entrySet()) {
						map.addTag(entry.getKey(), entry.getValue());
					}
					map.tagsDone(rel.getUniqueId());
				}
			}
			if (rendering != null) rendering.zoomChanged();
		}
	}

}
