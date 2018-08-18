/* Copyright 2014 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package scedit;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSelectionListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
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
import org.openstreetmap.josm.data.osm.event.SelectionEventManager;
import org.openstreetmap.josm.data.osm.event.TagsChangedEvent;
import org.openstreetmap.josm.data.osm.event.WayNodesChangedEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

import panels.PanelMain;
import panels.PanelS57;
import panels.ShowFrame;
import s57.S57map;
import s57.S57map.Feature;

public class SCeditAction extends JosmAction implements ActiveLayerChangeListener, DataSelectionListener {
    private static String title = tr("SeaChart Editor");
    public static JFrame editFrame = null;
    public static ShowFrame showFrame = null;
    private boolean isOpen = false;
    public static PanelMain panelMain = null;
    public static PanelS57 panelS57 = null;
    public static S57map map = null;
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

    public SCeditAction() {
        super(title, "SC", title, null, true);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
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
        editFrame = new JFrame(title);
        editFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        editFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                closeDialog();
            }
        });
        editFrame.setSize(new Dimension(480, 480));
        editFrame.setLocation(100, 200);
        editFrame.setResizable(true);
        editFrame.setAlwaysOnTop(true);
        editFrame.setVisible(true);
        panelMain = new PanelMain();
        editFrame.add(panelMain);

        panelS57 = new PanelS57();
        editFrame.add(panelS57);

        showFrame = new ShowFrame(tr("Seamark Inspector"));
        showFrame.setSize(new Dimension(300, 300));
        showFrame.setLocation(50, 400);
        showFrame.setResizable(false);
        showFrame.setAlwaysOnTop(true);
        showFrame.setEnabled(true);
        showFrame.setVisible(false);

        getLayerManager().addAndFireActiveLayerChangeListener(this);
        SelectionEventManager.getInstance().addSelectionListener(this);
    }

    public void closeDialog() {
        if (isOpen) {
                getLayerManager().removeActiveLayerChangeListener(this);
            editFrame.setVisible(false);
            editFrame.dispose();
            data = null;
            map = null;
        }
        isOpen = false;
    }

    @Override
    public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
        if (e.getPreviousDataLayer() != null) {
            e.getPreviousDataLayer().getDataSet().removeDataSetListener(dataSetListener);
        }
        OsmDataLayer newLayer = getLayerManager().getEditLayer();
        if (newLayer != null) {
            newLayer.getDataSet().addDataSetListener(dataSetListener);
            data = newLayer.getDataSet();
            makeMap();
        } else {
            data = null;
            map = null;
        }
    }

    @Override
    public void selectionChanged(SelectionChangeEvent event) {
        OsmPrimitive nextFeature = null;
        OsmPrimitive feature = null;

        showFrame.setVisible(false);
        panelMain.clearMark();
        if (map != null) {
            Set<OsmPrimitive> selection = event.getSelection();
            for (OsmPrimitive osm : selection) {
                nextFeature = osm;
                if (selection.size() == 1) {
                    if (nextFeature.compareTo(feature) != 0) {
                        feature = nextFeature;
                        Feature id = map.index.get(feature.getUniqueId());
                        if (id != null) {
                            panelMain.parseMark(id);
                            showFrame.setVisible(true);
                            showFrame.showFeature(feature, map);
                        }
                    }
                } else {
                    showFrame.setVisible(false);
                    PanelMain.setStatus(tr("Select only one object"), Color.orange);
                }
            }
            if (nextFeature == null) {
                feature = null;
                panelMain.clearMark();
                showFrame.setVisible(false);
                PanelMain.setStatus(tr("Select a map object"), Color.yellow);
            }
        }
    }

    void makeMap() {
        map = new S57map(true);
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
            map.mapDone();
        }
    }
}
