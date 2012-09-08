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
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Collections;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListener;
import org.openstreetmap.josm.data.osm.event.NodeMovedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesAddedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesRemovedEvent;
import org.openstreetmap.josm.data.osm.event.RelationMembersChangedEvent;
import org.openstreetmap.josm.data.osm.event.TagsChangedEvent;
import org.openstreetmap.josm.data.osm.event.WayNodesChangedEvent;
import org.openstreetmap.josm.plugins.opendata.OdPlugin;
import org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.layers.OdDataLayer;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.ToulouseConstants;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.ToulouseLicense;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.ToulouseModule;
import org.openstreetmap.josm.plugins.utilsplugin2.selection.NodeWayUtils;

public abstract class ToulouseDataSetHandler extends FrenchDataSetHandler implements ToulouseConstants {
    
    public ToulouseDataSetHandler(int portalId) {
        init(portalId);
    }
    
    public ToulouseDataSetHandler(int portalId, String relevantTag) {
        super(relevantTag);
        init(portalId);
    }
    
    public ToulouseDataSetHandler(int portalId, boolean relevantUnion, String ... relevantTags) {
        super(relevantUnion, relevantTags);
        init(portalId);
    }

    public ToulouseDataSetHandler(int portalId, String ... relevantTags) {
        this(portalId, false, relevantTags);
    }

    /*public ToulouseDataSetHandler(int portalId, Tag relevantTag) {
        super(relevantTag);
        init(portalId);
    }*/
    
    public ToulouseDataSetHandler(int portalId, boolean relevantUnion, Tag ... relevantTags) {
        super(relevantUnion, relevantTags);
        init(portalId);
    }

    /*public ToulouseDataSetHandler(int portalId, Tag ... relevantTags) {
        this(portalId, false, relevantTags);
    }*/
    
    private final void init(int portalId) {
        try {
            setLicense(new ToulouseLicense());
            if (portalId > 0) {
                String url = PORTAL + "/les-donnees/-/opendata/card/" + portalId + "--";
                setLocalPortalURL(url);
                //setLicenseURL(url+"/license");
                setDataURL(url+"/resource/document");
            }
            addTool(new SplitBySector());
            addTool(new SplitByNeighbourhood());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getSource()
     */
    @Override
    public String getSource() {
        return SOURCE_GRAND_TOULOUSE;
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getLocalPortalIconName()
     */
    @Override
    public String getLocalPortalIconName() {
        return ICON_CROIX_24;
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getDataLayerIconName()
     */
    @Override
    public String getDataLayerIconName() {
        return ICON_CROIX_16;
    }
    
    protected final void setWikiPage(String wikiPage) {
        if (wikiPage != null && !wikiPage.isEmpty()) {
            setName(wikiPage.replace("_", " "));
            try {
                setWikiURL(WIKI + "/" + wikiPage.replace(" ", "_"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
    
    protected abstract class SplitAction extends JosmAction implements DataSetListener {
        
        public SplitAction(String name, String desc) {
            super(name, null, desc, null, false);
            setEnabled(false);
            ToulouseModule.data.addDataSetListener(this);
        }
        
        protected abstract Collection<Relation> getBoundaries();
        
        @Override
        public void actionPerformed(ActionEvent e) {
            final String baseName = OdPlugin.getInstance().getDialog().getDataLayer().getName();
            final DataSet baseDs = getCurrentDataSet();
            for (OsmPrimitive boundary : getBoundaries()) {
                DataSet data = new DataSet();
                for (OsmPrimitive p : NodeWayUtils.selectAllInside(Collections.singleton(boundary), baseDs)) {
                    if (p instanceof Node) {
                        data.addPrimitive(new Node((Node)p));
                    } else if (p instanceof Way) {
                        data.addPrimitive(new Way((Way)p));
                    } else if (p instanceof Relation) {
                        data.addPrimitive(new Relation((Relation)p));
                    }
                }
                Main.main.addLayer(new OdDataLayer(data, baseName+"/"+boundary.get("ref"), null, ToulouseDataSetHandler.this));
            }
        }
        @Override
        public void dataChanged(DataChangedEvent event) {
            setEnabled(!getBoundaries().isEmpty());
        }
        @Override
        public void primitivesAdded(PrimitivesAddedEvent event) {
        }
        @Override
        public void primitivesRemoved(PrimitivesRemovedEvent event) {
        }
        @Override
        public void tagsChanged(TagsChangedEvent event) {
        }
        @Override
        public void nodeMoved(NodeMovedEvent event) {
        }
        @Override
        public void wayNodesChanged(WayNodesChangedEvent event) {
        }
        @Override
        public void relationMembersChanged(RelationMembersChangedEvent event) {
        }
        @Override
        public void otherDatasetChange(AbstractDatasetChangedEvent event) {
        }
    }
    
    protected class SplitBySector extends SplitAction {
        public SplitBySector() {
            super(marktr("Split by sector"), tr("Split this data by sector (admin_level=10)."));
        }
        @Override
        protected Collection<Relation> getBoundaries() {
            return ToulouseModule.getSectors();
        }
    }

    protected class SplitByNeighbourhood extends SplitAction {
        public SplitByNeighbourhood() {
            super(marktr("Split by neighbourhood"), tr("Split this data by neighbourhood (admin_level=11)."));
        }
        @Override
        protected Collection<Relation> getBoundaries() {
            return ToulouseModule.getNeighbourhoods();
        }
    }

    @Override
    public void notifyActive() {
        ToulouseModule.downloadData();
    }
}
