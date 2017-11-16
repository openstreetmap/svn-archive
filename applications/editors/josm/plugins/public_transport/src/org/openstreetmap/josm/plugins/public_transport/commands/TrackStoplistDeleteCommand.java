// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.public_transport.commands;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;
import java.util.Vector;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.public_transport.TransText;
import org.openstreetmap.josm.plugins.public_transport.actions.StopImporterAction;
import org.openstreetmap.josm.plugins.public_transport.models.TrackStoplistTableModel;

public class TrackStoplistDeleteCommand extends Command {
    private static class NodeTimeName {
        NodeTimeName(Node node, String time, String name, TransText shelter) {
            this.node = node;
            this.time = time;
            this.name = name;
            this.shelter = shelter;
        }

        public Node node;

        public String time;

        public String name;

        public TransText shelter;
    }

    private Vector<Integer> workingLines = null;

    private Vector<NodeTimeName> nodesForUndo = null;

    private TrackStoplistTableModel stoplistTM = null;

    public TrackStoplistDeleteCommand(StopImporterAction controller) {
        super(MainApplication.getLayerManager().getEditDataSet());
        stoplistTM = controller.getCurrentTrack().stoplistTM;
        workingLines = new Vector<>();
        nodesForUndo = new Vector<>();

        // use selected lines or all lines if no line is selected
        int[] selectedLines = controller.getDialog().getStoplistTable().getSelectedRows();
        if (selectedLines.length > 0) {
            for (int i = 0; i < selectedLines.length; ++i) {
                workingLines.add(selectedLines[i]);
            }
        } else {
            for (int i = 0; i < stoplistTM.getRowCount(); ++i) {
                workingLines.add(Integer.valueOf(i));
            }
        }
    }

    @Override
    public boolean executeCommand() {
        nodesForUndo.clear();
        for (int i = workingLines.size() - 1; i >= 0; --i) {
            int j = workingLines.elementAt(i).intValue();
            Node node = stoplistTM.nodeAt(j);
            nodesForUndo.add(new NodeTimeName(node, (String) stoplistTM.getValueAt(j, 0),
                    (String) stoplistTM.getValueAt(j, 1), (TransText) stoplistTM.getValueAt(j, 2)));
            stoplistTM.removeRow(j);
            if (node == null)
                continue;
            MainApplication.getLayerManager().getEditDataSet().removePrimitive(node);
            node.setDeleted(true);
        }
        return true;
    }

    @Override
    public void undoCommand() {
        for (int i = 0; i < workingLines.size(); ++i) {
            int j = workingLines.elementAt(i).intValue();
            NodeTimeName ntn = nodesForUndo.elementAt(workingLines.size() - i - 1);
            stoplistTM.insertRow(j, ntn.node, ntn.time, ntn.name, ntn.shelter);
            if (ntn.node == null)
                continue;
            ntn.node.setDeleted(false);
            MainApplication.getLayerManager().getEditDataSet().addPrimitive(ntn.node);
        }
    }

    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified,
            Collection<OsmPrimitive> deleted, Collection<OsmPrimitive> added) {
    }

    @Override
    public String getDescriptionText() {
        return tr("Public Transport: Delete track stop");
    }
}
