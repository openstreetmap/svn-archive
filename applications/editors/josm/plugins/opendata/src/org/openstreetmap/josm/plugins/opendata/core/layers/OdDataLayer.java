// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.layers;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.actions.OpenLinkAction;
import org.openstreetmap.josm.plugins.opendata.core.actions.ViewLicenseAction;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.io.OsmDownloader;
import org.openstreetmap.josm.plugins.opendata.core.licenses.License;
import org.openstreetmap.josm.plugins.opendata.core.util.OdUtils;
import org.openstreetmap.josm.tools.ImageProvider;

public class OdDataLayer extends OsmDataLayer implements OdLayer, LayerChangeListener, ActiveLayerChangeListener {

    public OdDiffLayer diffLayer;
    public OdOsmDataLayer osmLayer;
    
    public final AbstractDataSetHandler handler;
    
    private Bounds bounds;
    
    public OdDataLayer(DataSet data, String name, File associatedFile, AbstractDataSetHandler handler) {
        super(data, name, associatedFile);
        setUploadDiscouraged(true);
        this.handler = handler;
        for (Node node : data.getNodes()) {
            LatLon ll = node.getCoor();
            if (ll != null) {
                if (this.bounds == null) {
                    this.bounds = new Bounds(ll);
                } else {
                    this.bounds.extend(ll);
                }
            }
        }
        Main.getLayerManager().addLayerChangeListener(this);
    }
    
    @Override public ImageProvider getBaseIconProvider() {
        return OdUtils.getImageProvider(handler != null ? handler.getDataLayerIconName() : OdConstants.ICON_CORE_16);
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
            Main.getLayerManager().setActiveLayer(osmLayer);
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
    public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
        if (Main.getLayerManager().getActiveLayer() == this && this.handler != null) {
            this.handler.notifyActive();
        }
    }

    @Override
    public void layerAdded(LayerAddEvent e) {
    }

    @Override
    public void layerRemoving(LayerRemoveEvent e) {
        if (e.getRemovedLayer() == this) {
            removeOsmLayer();
            removeDiffLayer();
        } else if (e.getRemovedLayer() == osmLayer) {
            osmLayer = null;
        } else if (e.getRemovedLayer() == diffLayer) {
            diffLayer = null;
        }
    }

    @Override
    public void layerOrderChanged(LayerOrderChangeEvent e) {
    }

    @Override
    public Action[] getMenuEntries() {
        List<Action> result = new ArrayList<>();
        for (Action entry : super.getMenuEntries()) {
            result.add(entry);
        }
        if (this.handler != null) {
            if (this.handler.getWikiURL() != null || this.handler.getLocalPortalURL() != null || this.handler.getNationalPortalURL() != null) { 
                result.add(SeparatorLayerAction.INSTANCE);
            }
            if (this.handler.getWikiURL() != null) { 
                result.add(new OpenLinkAction(this.handler.getWikiURL(), OdConstants.ICON_OSM_24, 
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
                    result.add(new OpenLinkAction(lic.getURL(), OdConstants.ICON_AGREEMENT_24, 
                            tr("View License"), tr("Launch browser to the license page of the selected data set")));
                }
                if (lic.getSummaryURL() != null && lic.getSummaryURL().getProtocol().startsWith("http")) {
                    result.add(new OpenLinkAction(lic.getSummaryURL(), OdConstants.ICON_AGREEMENT_24, 
                            tr("View License (summary)"), tr("Launch browser to the summary license page of the selected data set")));
                }
                if ((lic.getURL() != null && !lic.getURL().getProtocol().startsWith("http")) || (lic.getSummaryURL() != null && !lic.getSummaryURL().getProtocol().startsWith("http"))) {
                    result.add(new ViewLicenseAction(lic, tr("View License"), tr("View the license of the selected data set")));
                }
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
        Main.getLayerManager().setActiveLayer(diffLayer);
    }
}
