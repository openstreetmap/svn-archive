// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.public_transport.commands;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;
import java.util.Vector;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.public_transport.actions.StopImporterAction;
import org.openstreetmap.josm.plugins.public_transport.models.TrackStoplistTableModel;

public class TrackStoplistDetachCommand extends Command {
    private Vector<Integer> workingLines = null;

    private Vector<Node> nodesForUndo = null;

    private TrackStoplistTableModel stoplistTM = null;

    public TrackStoplistDetachCommand(StopImporterAction controller) {
        super(MainApplication.getLayerManager().getEditDataSet());
        stoplistTM = controller.getCurrentTrack().stoplistTM;
        workingLines = new Vector<>();
        nodesForUndo = new Vector<>();

        // use either selected lines or all lines if no line is selected
        int[] selectedLines = controller.getDialog().getStoplistTable().getSelectedRows();
        Vector<Integer> consideredLines = new Vector<>();
        if (selectedLines.length > 0) {
            for (int i = 0; i < selectedLines.length; ++i) {
                consideredLines.add(selectedLines[i]);
            }
        } else {
            for (int i = 0; i < stoplistTM.getRowCount(); ++i) {
                consideredLines.add(Integer.valueOf(i));
            }
        }

        // keep only lines where a node can be added
        for (int i = 0; i < consideredLines.size(); ++i) {
            if (stoplistTM.nodeAt(consideredLines.elementAt(i)) != null)
                workingLines.add(consideredLines.elementAt(i));
        }
    }

    @Override
    public boolean executeCommand() {
        nodesForUndo.clear();
        for (int i = 0; i < workingLines.size(); ++i) {
            int j = workingLines.elementAt(i).intValue();
            Node node = stoplistTM.nodeAt(j);
            nodesForUndo.add(node);
            stoplistTM.setNodeAt(j, null);
        }
        return true;
    }

    @Override
    public void undoCommand() {
        for (int i = 0; i < workingLines.size(); ++i) {
            int j = workingLines.elementAt(i).intValue();
            Node node = nodesForUndo.elementAt(i);
            stoplistTM.setNodeAt(j, node);
        }
    }

    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified,
            Collection<OsmPrimitive> deleted, Collection<OsmPrimitive> added) {
    }

    @Override
    public String getDescriptionText() {
        return tr("Public Transport: Detach track stop list");
    }
}
