// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.layers;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Pair;

public class OdDiffLayer extends Layer implements OdLayer {

	private final OdDataLayer dataLayer;
	
	public final List<Pair<OsmPrimitive,OsmPrimitive>> differentPrimitives;
	public final List<OsmPrimitive> onlyInTlsPrimitives;
	public final List<OsmPrimitive> onlyInOsmPrimitives;
	
	public OdDiffLayer(OdDataLayer dataLayer, String name) {
		super(name);
		this.dataLayer = dataLayer;
		this.differentPrimitives = new ArrayList<>();
		this.onlyInTlsPrimitives = new ArrayList<>();
		this.onlyInOsmPrimitives = new ArrayList<>();
		initDiff(dataLayer.data, dataLayer.osmLayer.data);
	}
	
	private void initDiff(DataSet tlsData, DataSet osmData) {
		for (OsmPrimitive p1 : tlsData.allPrimitives()) {
			if (dataLayer.handler.isRelevant(p1)) {
				OsmPrimitive p2 = findPrimitiveAt(osmData, p1);
				if (p2 == null) {
					onlyInTlsPrimitives.add(p1);
				} else if (!dataLayer.handler.equals(p1, p2)) {
					differentPrimitives.add(new Pair<>(p1, p2));
				}
			}
		}
		for (OsmPrimitive p1 : osmData.allPrimitives()) {
			if (dataLayer.handler.isRelevant(p1)) {
				if (findPrimitiveAt(tlsData, p1) == null) {
					onlyInOsmPrimitives.add(p1);
				}
			}
		}
	}
	
	private double distance(OsmPrimitive p1, OsmPrimitive p2) {
		return p1.getBBox().getCenter().greatCircleDistance(p2.getBBox().getCenter());
	}
	
	private OsmPrimitive findPrimitiveAt(DataSet dataSet, OsmPrimitive source) {
		double maxDistance = Main.pref.getDouble(OdConstants.PREF_MAXDISTANCE, OdConstants.DEFAULT_MAXDISTANCE);
		//List<OsmPrimitive> samePrimitives = new ArrayList<OsmPrimitive>();
		OsmPrimitive nearestSamePrimitive = null;
		//List<OsmPrimitive> potentialPrimitives = new ArrayList<OsmPrimitive>();
		OsmPrimitive nearestPotentialPrimitive = null;
		for (OsmPrimitive p : dataSet.allPrimitives()) {
			if (dataLayer.handler.isRelevant(p)) {
				double dist = distance(source, p); 
				if (dist <= maxDistance) {
					if (dataLayer.handler.equals(p, source)) {
						//samePrimitives.add(p);
						if (nearestSamePrimitive == null || distance(p, nearestSamePrimitive) > dist) {
							nearestSamePrimitive = p;
						}
					} else {
						//potentialPrimitives.add(p);
						if (nearestPotentialPrimitive == null || distance(p, nearestPotentialPrimitive) > dist) {
							nearestPotentialPrimitive = p;
						}
					}
				}
			}
		}
		return nearestSamePrimitive != null ? nearestSamePrimitive : nearestPotentialPrimitive;
	}

	@Override
	public void paint(Graphics2D g, MapView mv, Bounds box) {
		// TODO Auto-generated method stub
	}

	@Override
	public Icon getIcon() {
		return ImageProvider.get("layer", "diff");
	}

	@Override
	public String getToolTipText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void mergeFrom(Layer from) {
	}

	@Override
	public boolean isMergable(Layer other) {
		return false;
	}

	@Override
	public void visitBoundingBox(BoundingXYVisitor v) {
		// TODO Auto-generated method stub
	}

	@Override
	public Object getInfoComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Action[] getMenuEntries() {
		return null;
	}

	@Override
	public OdDataLayer getDataLayer() {
		return dataLayer;
	}
}
