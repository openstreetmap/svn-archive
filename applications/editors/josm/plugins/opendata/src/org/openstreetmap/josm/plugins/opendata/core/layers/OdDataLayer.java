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

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.actions.OpenLinkAction;
import org.openstreetmap.josm.plugins.opendata.core.actions.ViewLicenseAction;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.io.OsmDownloader;
import org.openstreetmap.josm.plugins.opendata.core.licenses.License;
import org.openstreetmap.josm.plugins.opendata.core.util.OdUtils;

public class OdDataLayer extends OsmDataLayer implements OdConstants, OdLayer, LayerChangeListener {

	public OdDiffLayer diffLayer;
	public OdOsmDataLayer osmLayer;
	
	public final AbstractDataSetHandler handler;
	
	private Bounds bounds;
	
	public OdDataLayer(DataSet data, String name, File associatedFile, AbstractDataSetHandler handler) {
		super(data, name, associatedFile);
		setUploadDiscouraged(true);
		this.handler = handler;
		for (Node node : data.getNodes()) {
			if (this.bounds == null) {
				this.bounds = new Bounds(node.getCoor());
			} else {
				this.bounds.extend(node.getCoor());
			}
		}
		MapView.addLayerChangeListener(this);
	}
	
    @Override public Icon getBaseIcon() {
    	return OdUtils.getImageIcon(handler != null ? handler.getDataLayerIconName() : ICON_CORE_16);
    }

    public void addOsmLayer(OdOsmDataLayer layer) {
    	removeOsmLayer();
    	osmLayer = layer;
    	Main.main.addLayer(osmLayer);
    }

    public void removeOsmLayer() {
    	if (osmLayer != null) {
	    	Main.main.removeLayer(osmLayer);
	    	osmLayer = null;
    	}
    }
    
    public void addDiffLayer(OdDiffLayer layer) {
    	removeDiffLayer();
    	diffLayer = layer;
    	Main.main.addLayer(diffLayer);
    }
    
    public void removeDiffLayer() {
    	if (diffLayer != null) {
	    	Main.main.removeLayer(diffLayer);
	    	diffLayer = null;
    	}
    }
    
	public final void downloadOsmData() {
		String oapiReq = handler.getOverpassApiRequest(bounds);
		Collection<String> xapiReqs = handler.getOsmXapiRequests(bounds);
		if (oapiReq != null || xapiReqs != null) {
			DataSet dataSet = new DataSet();
			final OdOsmDataLayer layer = new OdOsmDataLayer(this, dataSet, getName()+"/OSM");
			addOsmLayer(layer);
			Main.map.mapView.setActiveLayer(osmLayer);
			if (oapiReq != null) {
				OsmDownloader.downloadOapi(oapiReq);
				// Overpass API does not allow to exclude tags :(
				layer.removeForbiddenTags();
			} else {
				OsmDownloader.downloadXapi(xapiReqs);
			}
		}
	}

	@Override
	public void activeLayerChange(Layer oldLayer, Layer newLayer) {
	}

	@Override
	public void layerAdded(Layer newLayer) {
	}

	@Override
	public void layerRemoved(Layer oldLayer) {
		if (oldLayer == this) {
			removeOsmLayer();
			removeDiffLayer();
		} else if (oldLayer == osmLayer) {
			osmLayer = null;
		} else if (oldLayer == diffLayer) {
			diffLayer = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.layer.OsmDataLayer#getMenuEntries()
	 */
	@Override
	public Action[] getMenuEntries() {
		List<Action> result = new ArrayList<Action>();
		for (Action entry : super.getMenuEntries()) {
			result.add(entry);
		}
		if (this.handler.getWikiURL() != null || this.handler.getLocalPortalURL() != null || this.handler.getNationalPortalURL() != null) { 
			result.add(SeparatorLayerAction.INSTANCE);
		}
		if (this.handler.getWikiURL() != null) { 
			result.add(new OpenLinkAction(this.handler.getWikiURL(), ICON_OSM_24, 
					tr("View OSM Wiki page"), tr("Launch browser to the OSM Wiki page of the selected data set")));
		}
		if (this.handler.getLocalPortalURL() != null) { 
			result.add(new OpenLinkAction(this.handler.getLocalPortalURL(), this.handler.getLocalPortalIconName(), 
					tr("View Local Portal page"), tr("Launch browser to the local portal page of the selected data set")));
		}
		if (this.handler.getNationalPortalURL() != null) { 
			result.add(new OpenLinkAction(this.handler.getNationalPortalURL(), this.handler.getNationalPortalIconName(), 
					tr("View National Portal page"), tr("Launch browser to the national portal page of the selected data set")));
		}
		if (this.handler.getLicense() != null) {
			License lic = this.handler.getLicense();
			if (lic.getURL() != null && lic.getURL().getProtocol().startsWith("http")) {
				result.add(new OpenLinkAction(lic.getURL(), ICON_AGREEMENT_24, 
						tr("View License"), tr("Launch browser to the license page of the selected data set")));
			}
			if (lic.getSummaryURL() != null && lic.getSummaryURL().getProtocol().startsWith("http")) {
				result.add(new OpenLinkAction(lic.getSummaryURL(), ICON_AGREEMENT_24, 
						tr("View License (summary)"), tr("Launch browser to the summary license page of the selected data set")));
			}
			if ((lic.getURL() != null && !lic.getURL().getProtocol().startsWith("http")) || (lic.getSummaryURL() != null && !lic.getSummaryURL().getProtocol().startsWith("http"))) {
				result.add(new ViewLicenseAction(lic, tr("View License"), tr("View the license of the selected data set")));
			}
		}
		return result.toArray(new Action[0]);
	}

	@Override
	public OdDataLayer getDataLayer() {
		return this;
	}

	public void makeDiff() {
		final OdDiffLayer layer = new OdDiffLayer(this, getName()+"/Diff");
		addDiffLayer(layer);
		Main.map.mapView.setActiveLayer(diffLayer);
	}
}
