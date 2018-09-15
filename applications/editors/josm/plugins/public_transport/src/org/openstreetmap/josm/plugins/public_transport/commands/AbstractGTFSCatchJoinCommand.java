// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.public_transport.commands;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.public_transport.actions.GTFSImporterAction;
import org.openstreetmap.josm.plugins.public_transport.models.GTFSStopTableModel;

public abstract class AbstractGTFSCatchJoinCommand extends Command {
    private List<Integer> workingLines = null;

    private Node undoMapNode = null;

    private Node undoTableNode = null;

    private GTFSStopTableModel gtfsStopTM = null;

    private String type = null;

    private final boolean isCatch;

    public AbstractGTFSCatchJoinCommand(GTFSImporterAction controller, boolean isCatch) {
        super(MainApplication.getLayerManager().getEditDataSet());
        gtfsStopTM = controller.getGTFSStopTableModel();
        workingLines = new ArrayList<>();
        this.isCatch = isCatch;

        // use either selected lines or all lines if no line is selected
        int[] selectedLines = controller.getDialog().getGTFSStopTable().getSelectedRows();
        if (selectedLines.length != 1)
            return;
        workingLines.add(selectedLines[0]);
    }

    @Override
    public boolean executeCommand() {
        if (workingLines.size() != 1)
            return false;
        Node dest = null;
        DataSet ds = MainApplication.getLayerManager().getEditDataSet();
        Iterator<Node> iter = ds.getSelectedNodes().iterator();
        int j = workingLines.get(0);
        while (iter.hasNext()) {
            Node n = iter.next();
            if (n != null && n.equals(gtfsStopTM.nodes.elementAt(j)))
                continue;
            if (dest != null)
                return false;
            dest = n;
        }
        if (dest == null)
            return false;
        undoMapNode = new Node(dest);

        Node node = gtfsStopTM.nodes.elementAt(j);
        undoTableNode = node;
        if (node != null) {
            ds.removePrimitive(node);
            node.setDeleted(true);
        }

        if (isCatch)
            dest.setCoor(gtfsStopTM.coors.elementAt(j));
        dest.put("highway", "bus_stop");
        dest.put("stop_id", (String) gtfsStopTM.getValueAt(j, 0));
        if (dest.get("name") == null)
            dest.put("name", (String) gtfsStopTM.getValueAt(j, 1));
        if (isCatch)
            dest.put("note", "moved by gtfs import");
        gtfsStopTM.nodes.set(j, dest);
        type = (String) gtfsStopTM.getValueAt(j, 2);
        gtfsStopTM.setValueAt(isCatch ? "fed" : tr("moved"), j, 2);

        return true;
    }

    @Override
    public void undoCommand() {
        if (workingLines.size() != 1)
            return;
        int j = workingLines.get(0);

        DataSet ds = MainApplication.getLayerManager().getEditDataSet();
        Node node = gtfsStopTM.nodes.elementAt(j);
        if (node != null) {
            ds.removePrimitive(node);
            node.setDeleted(true);
        }

        if (undoMapNode != null) {
            undoMapNode.setDeleted(false);
            ds.addPrimitive(undoMapNode);
        }
        if (undoTableNode != null) {
            undoTableNode.setDeleted(false);
            ds.addPrimitive(undoTableNode);
        }
        gtfsStopTM.nodes.set(j, undoTableNode);
        gtfsStopTM.setValueAt(type, j, 2);
    }

    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified,
            Collection<OsmPrimitive> deleted, Collection<OsmPrimitive> added) {
        // Do nothing
    }
}
