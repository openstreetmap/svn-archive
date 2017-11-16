// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.public_transport.commands;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;
import java.util.Vector;

import javax.swing.DefaultListModel;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.public_transport.actions.StopImporterAction;
import org.openstreetmap.josm.plugins.public_transport.models.WaypointTableModel;
import org.openstreetmap.josm.plugins.public_transport.refs.TrackReference;

public class SettingsStoptypeCommand extends Command {
    private static class HighwayRailway {
        HighwayRailway(Node node) {
            this.node = node;
            highway = node.get("highway");
            railway = node.get("railway");
        }

        public Node node;

        public String highway;

        public String railway;
    }

    private Vector<HighwayRailway> oldStrings = null;

    private WaypointTableModel waypointTM = null;

    private DefaultListModel<?> tracksListModel = null;

    private String type = null;

    public SettingsStoptypeCommand(StopImporterAction controller) {
        super(MainApplication.getLayerManager().getEditDataSet());
        waypointTM = controller.getWaypointTableModel();
        tracksListModel = controller.getTracksListModel();
        type = controller.getDialog().getStoptype();
        oldStrings = new Vector<>();
    }

    @Override
    public boolean executeCommand() {
        oldStrings.clear();
        for (int i = 0; i < waypointTM.getRowCount(); ++i) {
            if (waypointTM.nodes.elementAt(i) != null) {
                Node node = waypointTM.nodes.elementAt(i);
                oldStrings.add(new HighwayRailway(node));
                StopImporterAction.setTagsWrtType(node, type);
            }
        }
        for (int j = 0; j < tracksListModel.size(); ++j) {
            TrackReference track = (TrackReference) tracksListModel.elementAt(j);
            for (int i = 0; i < track.stoplistTM.getRowCount(); ++i) {
                if (track.stoplistTM.nodeAt(i) != null) {
                    Node node = track.stoplistTM.nodeAt(i);
                    oldStrings.add(new HighwayRailway(node));
                    StopImporterAction.setTagsWrtType(node, type);
                }
            }
        }
        return true;
    }

    @Override
    public void undoCommand() {
        for (int i = 0; i < oldStrings.size(); ++i) {
            HighwayRailway hr = oldStrings.elementAt(i);
            hr.node.put("highway", hr.highway);
            hr.node.put("railway", hr.railway);
        }
    }

    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified,
            Collection<OsmPrimitive> deleted, Collection<OsmPrimitive> added) {
    }

    @Override
    public String getDescriptionText() {
        return tr("Public Transport: Change stop type");
    }
}
