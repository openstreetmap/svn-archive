// License: GPL. For details, see LICENSE file.
package seachart;

import java.awt.event.ActionEvent;
import java.util.Map.Entry;

import javax.swing.SwingUtilities;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.imagery.ImageryInfo;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
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
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

import s57.S57map;

/**
 * @author Malcolm Herring
 */
public class SeachartAction extends JosmAction implements ActiveLayerChangeListener, LayerChangeListener {
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
    public void layerAdded(LayerAddEvent e) {
    }

    @Override
    public void layerRemoving(LayerRemoveEvent e) {
        if ("SeaChart".equals(e.getRemovedLayer().getName())) {
            closeChartLayer();
        }
    }

    @Override
    public void layerOrderChanged(LayerOrderChangeEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (!isOpen)
                    createChartLayer();
                isOpen = true;
            }
        });
    }

    protected void createChartLayer() {
        rendering = new ChartImage(new ImageryInfo("SeaChart"));
        rendering.setBackgroundLayer(true);
        MainApplication.getLayerManager().addLayer(rendering);
        MainApplication.getLayerManager().addAndFireActiveLayerChangeListener(this);
        MainApplication.getLayerManager().addLayerChangeListener(this);
    }

    public void closeChartLayer() {
        if (isOpen) {
        	try {
            MainApplication.getLayerManager().removeActiveLayerChangeListener(this);
            MainApplication.getLayerManager().removeLayerChangeListener(this);
            MainApplication.getLayerManager().removeLayer(rendering);
        	} catch (Exception e) {
        		// Assume that this can't be serious?
        		System.err.println(e);
        	}
        }
        isOpen = false;
    }

    @Override
    public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
        OsmDataLayer oldLayer = e.getPreviousDataLayer();
        if (oldLayer != null) {
            oldLayer.getDataSet().removeDataSetListener(dataSetListener);
        }
        OsmDataLayer newLayer = MainApplication.getLayerManager().getEditLayer();
        if (newLayer != null) {
            newLayer.getDataSet().addDataSetListener(dataSetListener);
            data = newLayer.getDataSet();
            makeChart();
        } else {
            data = null;
            map = null;
        }
    }

    void makeChart() {
        map = new S57map(false);
        if (data != null) {
            double minlat = 90;
            double maxlat = -90;
            double minlon = 180;
            double maxlon = -180;
            for (Bounds bounds : data.getDataSourceBounds()) {
                if (bounds.getMinLat() < minlat) {
                    minlat = bounds.getMinLat();
                }
                if (bounds.getMaxLat() > maxlat) {
                    maxlat = bounds.getMaxLat();
                }
                if (bounds.getMinLon() < minlon) {
                    minlon = bounds.getMinLon();
                }
                if (bounds.getMaxLon() > maxlon) {
                    maxlon = bounds.getMaxLon();
                }
            }
            map.addNode(1, maxlat, minlon);
            map.addNode(2, minlat, minlon);
            map.addNode(3, minlat, maxlon);
            map.addNode(4, maxlat, maxlon);
            map.bounds.minlat = Math.toRadians(minlat);
            map.bounds.maxlat = Math.toRadians(maxlat);
            map.bounds.minlon = Math.toRadians(minlon);
            map.bounds.maxlon = Math.toRadians(maxlon);
            for (Node node : data.getNodes()) {
                LatLon coor = node.getCoor();
                if (coor != null) {
                    map.addNode(node.getUniqueId(), coor.lat(), coor.lon());
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
                        map.addToEdge(node.getUniqueId());
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
                            map.addToArea(mem.getUniqueId(), mem.getRole().equals("outer"));
                    }
                    for (Entry<String, String> entry : rel.getKeys().entrySet()) {
                        map.addTag(entry.getKey(), entry.getValue());
                    }
                    map.tagsDone(rel.getUniqueId());
                }
            }
            map.mapDone();
            if (rendering != null) rendering.zoomChanged();
        }
    }

}
