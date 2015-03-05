// License: GPL. For details, see LICENSE file.
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
import org.openstreetmap.josm.plugins.opendata.core.datasets.DataSetCategory;
import org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.layers.OdDataLayer;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.ToulouseConstants;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.ToulouseLicense;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.ToulouseModule;
import org.openstreetmap.josm.plugins.utilsplugin2.selection.NodeWayUtils;

public abstract class ToulouseDataSetHandler extends FrenchDataSetHandler implements ToulouseConstants {

    /**
     * Categories
     */
    public static final DataSetCategory CAT_ASSOCIATIONS = new DataSetCategory("Associations", "styles/standard/people.png");
    public static final DataSetCategory CAT_CITOYENNETE = new DataSetCategory("Citoyenneté", "presets/townhall.png");
    public static final DataSetCategory CAT_CULTURE = new DataSetCategory("Culture", "presets/arts_centre.png");
    public static final DataSetCategory CAT_ENFANCE = new DataSetCategory("Enfance", "presets/kindergarten.png");
    public static final DataSetCategory CAT_ENVIRONNEMENT = new DataSetCategory("Environnement", "presets/recycling.png");
    public static final DataSetCategory CAT_PATRIMOINE = new DataSetCategory("Patrimoine", "presets/ruins.png");
    public static final DataSetCategory CAT_SERVICES = new DataSetCategory("Services", "styles/standard/vehicle/services.png");
    public static final DataSetCategory CAT_SPORT = new DataSetCategory("Sport", "styles/standard/sport/soccer.png");
    public static final DataSetCategory CAT_TOPOGRAPHIE = new DataSetCategory("Topographie", "presets/peak.svg");
    public static final DataSetCategory CAT_TRANSPORT = new DataSetCategory("Transport", "presets/bus.png");
    public static final DataSetCategory CAT_URBANISME = new DataSetCategory("Urbanisme", "styles/standard/place/settlement/city.png");

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
            addTool(new SplitByMunicipality());
            addTool(new SplitBySector());
            addTool(new SplitByNeighbourhood());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getSource() {
        return SOURCE_TOULOUSE_METROPOLE;
    }

    @Override
    public String getLocalPortalIconName() {
        return ICON_CROIX_24;
    }

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
                if (!data.allPrimitives().isEmpty()) {
                    String name = boundary.get("name");
                    if (name == null || name.isEmpty()) {
                        name = boundary.get("ref");
                    }
                    if (name == null || name.isEmpty()) {
                        name = boundary.get("description");
                    }
                    Main.main.addLayer(new OdDataLayer(data, baseName+"/"+name, null, ToulouseDataSetHandler.this));
                }
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

    protected class SplitByMunicipality extends SplitAction {
        public SplitByMunicipality() {
            super(marktr("Split by municipality"), tr("Split this data by municipality (admin_level=8)."));
        }
        @Override
        protected Collection<Relation> getBoundaries() {
            return ToulouseModule.getMunicipalities();
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
