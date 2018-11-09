// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.public_transport.commands;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;
import java.util.Vector;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.public_transport.actions.GTFSImporterAction;
import org.openstreetmap.josm.plugins.public_transport.models.GTFSStopTableModel;

public class GTFSAddCommand extends Command {
    private Vector<Integer> workingLines = null;

    private Vector<String> typesForUndo = null;

    private GTFSStopTableModel gtfsStopTM = null;

    private String type = null;

    public GTFSAddCommand(GTFSImporterAction controller) {
        super(MainApplication.getLayerManager().getEditDataSet());
        gtfsStopTM = controller.getGTFSStopTableModel();
        type = controller.getDialog().getStoptype();
        workingLines = new Vector<>();
        typesForUndo = new Vector<>();

        // use either selected lines or all lines if no line is selected
        int[] selectedLines = controller.getDialog().getGTFSStopTable().getSelectedRows();
        Vector<Integer> consideredLines = new Vector<>();
        if (selectedLines.length > 0) {
            for (int i = 0; i < selectedLines.length; ++i) {
                consideredLines.add(selectedLines[i]);
            }
        } else {
            for (int i = 0; i < gtfsStopTM.getRowCount(); ++i) {
                consideredLines.add(Integer.valueOf(i));
            }
        }

        // keep only lines where a node can be added
        for (int i = 0; i < consideredLines.size(); ++i) {
            if (gtfsStopTM.nodes.elementAt(consideredLines.elementAt(i)) == null)
                workingLines.add(consideredLines.elementAt(i));
        }
    }

    @Override
    public boolean executeCommand() {
        typesForUndo.clear();
        for (int i = 0; i < workingLines.size(); ++i) {
            int j = workingLines.elementAt(i).intValue();
            typesForUndo.add((String) gtfsStopTM.getValueAt(j, 2));
            Node node = GTFSImporterAction.createNode(gtfsStopTM.coors.elementAt(j),
                    (String) gtfsStopTM.getValueAt(j, 0), (String) gtfsStopTM.getValueAt(j, 1));
            gtfsStopTM.nodes.set(j, node);
            gtfsStopTM.setValueAt(tr("added"), j, 2);
        }
        return true;
    }

    @Override
    public void undoCommand() {
        for (int i = 0; i < workingLines.size(); ++i) {
            int j = workingLines.elementAt(i).intValue();
            Node node = gtfsStopTM.nodes.elementAt(j);
            gtfsStopTM.nodes.set(j, null);
            gtfsStopTM.setValueAt(typesForUndo.elementAt(i), j, 2);
            if (node == null)
                continue;
            MainApplication.getLayerManager().getEditDataSet().removePrimitive(node);
            node.setDeleted(true);
        }
    }

    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified,
            Collection<OsmPrimitive> deleted, Collection<OsmPrimitive> added) {
    }

    @Override
    public String getDescriptionText() {
        return tr("Public Transport: Enable GTFSStops");
    }
}
