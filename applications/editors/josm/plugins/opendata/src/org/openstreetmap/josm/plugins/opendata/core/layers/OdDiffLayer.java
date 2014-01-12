//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

public class OdDiffLayer extends Layer implements OdLayer, OdConstants {

	private final OdDataLayer dataLayer;
	
	public final List<Pair<OsmPrimitive,OsmPrimitive>> differentPrimitives;
	public final List<OsmPrimitive> onlyInTlsPrimitives;
	public final List<OsmPrimitive> onlyInOsmPrimitives;
	
	public OdDiffLayer(OdDataLayer dataLayer, String name) {
		super(name);
		this.dataLayer = dataLayer;
		this.differentPrimitives = new ArrayList<Pair<OsmPrimitive,OsmPrimitive>>();
		this.onlyInTlsPrimitives = new ArrayList<OsmPrimitive>();
		this.onlyInOsmPrimitives = new ArrayList<OsmPrimitive>();
		initDiff(dataLayer.data, dataLayer.osmLayer.data);
	}
	
	private void initDiff(DataSet tlsData, DataSet osmData) {
		for (OsmPrimitive p1 : tlsData.allPrimitives()) {
			if (dataLayer.handler.isRelevant(p1)) {
				OsmPrimitive p2 = findPrimitiveAt(osmData, p1);
				if (p2 == null) {
					onlyInTlsPrimitives.add(p1);
				} else if (!dataLayer.handler.equals(p1, p2)) {
					differentPrimitives.add(new Pair<OsmPrimitive, OsmPrimitive>(p1, p2));
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
		double maxDistance = Main.pref.getDouble(PREF_MAXDISTANCE, DEFAULT_MAXDISTANCE);
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
