// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.public_transport.commands;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.public_transport.TransText;
import org.openstreetmap.josm.plugins.public_transport.actions.StopImporterAction;
import org.openstreetmap.josm.plugins.public_transport.dialogs.StopImporterDialog;
import org.openstreetmap.josm.plugins.public_transport.models.TrackStoplistTableModel;

public class TrackStoplistSortCommand extends Command {
    private TrackStoplistTableModel stoplistTM = null;

    private Vector<Vector<Object>> tableDataModel = null;

    private Vector<Node> nodes = null;

    private Vector<String> times = null;

    private Vector<Integer> workingLines = null;

    private int insPos;

    private String stopwatchStart;

    public TrackStoplistSortCommand(StopImporterAction controller) {
        super(MainApplication.getLayerManager().getEditDataSet());
        stoplistTM = controller.getCurrentTrack().stoplistTM;
        workingLines = new Vector<>();
        insPos = controller.getDialog().getStoplistTable().getSelectedRow();
        stopwatchStart = controller.getCurrentTrack().stopwatchStart;

        // use either selected lines or all lines if no line is selected
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
    @SuppressWarnings("unchecked")
    public boolean executeCommand() {
        tableDataModel = (Vector<Vector<Object>>) stoplistTM.getDataVector().clone();
        nodes = (Vector<Node>) stoplistTM.getNodes().clone();
        times = (Vector<String>) stoplistTM.getTimes().clone();

        Vector<NodeSortEntry> nodesToSort = new Vector<>();
        for (int i = workingLines.size() - 1; i >= 0; --i) {
            int j = workingLines.elementAt(i).intValue();
            nodesToSort.add(new NodeSortEntry(stoplistTM.nodeAt(j),
                    (String) stoplistTM.getValueAt(j, 0), (String) stoplistTM.getValueAt(j, 1),
                    (TransText) stoplistTM.getValueAt(j, 2),
                    StopImporterDialog.parseTime(stopwatchStart)));
            stoplistTM.removeRow(j);
        }

        Collections.sort(nodesToSort);

        int insPos = this.insPos;
        Iterator<NodeSortEntry> iter = nodesToSort.iterator();
        while (iter.hasNext()) {
            NodeSortEntry nse = iter.next();
            stoplistTM.insertRow(insPos, nse.node, nse.time, nse.name, nse.shelter);
            if (insPos >= 0)
                ++insPos;
        }
        return true;
    }

    @Override
    public void undoCommand() {
        stoplistTM.setDataVector(tableDataModel);
        stoplistTM.setNodes(nodes);
        stoplistTM.setTimes(times);
    }

    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified,
            Collection<OsmPrimitive> deleted, Collection<OsmPrimitive> added) {
    }

    @Override
    public String getDescriptionText() {
        return tr("Public Transport: sort track stop list");
    }

    private static class NodeSortEntry implements Comparable<NodeSortEntry> {
        public Node node = null;

        public String time = null;

        public String name = null;

        public TransText shelter = null;

        public double startTime = 0;

        NodeSortEntry(Node node, String time, String name, TransText shelter,
                double startTime) {
            this.node = node;
            this.time = time;
            this.name = name;
            this.shelter = shelter;
        }

        @Override
        public int compareTo(NodeSortEntry nse) {
            double time = StopImporterDialog.parseTime(this.time);
            if (time - startTime > 12 * 60 * 60)
                time -= 24 * 60 * 60;

            double nseTime = StopImporterDialog.parseTime(nse.time);
            if (nseTime - startTime > 12 * 60 * 60)
                nseTime -= 24 * 60 * 60;

            if (time < nseTime)
                return -1;
            else if (time > nseTime)
                return 1;
            else
                return 0;
        }
    }
}
