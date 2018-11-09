// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.public_transport.commands;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.public_transport.actions.StopImporterAction;
import org.openstreetmap.josm.plugins.public_transport.dialogs.StopImporterDialog;
import org.openstreetmap.josm.plugins.public_transport.refs.TrackReference;

public class TrackStoplistRelocateCommand extends Command {
    private StopImporterAction controller = null;

    private TrackReference currentTrack = null;

    private String oldGpsSyncTime = null;

    private String oldStopwatchStart = null;

    private String gpsSyncTime = null;

    private String stopwatchStart = null;

    public TrackStoplistRelocateCommand(StopImporterAction controller) {
        super(MainApplication.getLayerManager().getEditDataSet());
        this.controller = controller;
        this.currentTrack = controller.getCurrentTrack();
        this.gpsSyncTime = controller.getDialog().getGpsTimeStart();
        this.stopwatchStart = controller.getDialog().getStopwatchStart();
        this.oldGpsSyncTime = currentTrack.gpsSyncTime;
        this.oldStopwatchStart = currentTrack.stopwatchStart;
    }

    @Override
    public boolean executeCommand() {
        currentTrack.gpsSyncTime = gpsSyncTime;
        currentTrack.stopwatchStart = stopwatchStart;
        for (int i = 0; i < currentTrack.stoplistTM.getNodes().size(); ++i) {
            Node node = currentTrack.stoplistTM.nodeAt(i);
            if (node == null)
                continue;

            double time = StopImporterDialog
                    .parseTime((String) currentTrack.stoplistTM.getValueAt(i, 0));
            node.setCoor(currentTrack.computeCoor(time));
        }
        if (currentTrack == controller.getCurrentTrack()) {
            controller.inEvent = true;
            controller.getDialog().setGpsTimeStart(gpsSyncTime);
            controller.getDialog().setStopwatchStart(stopwatchStart);
            controller.inEvent = false;
        }

        return true;
    }

    @Override
    public void undoCommand() {
        currentTrack.gpsSyncTime = oldGpsSyncTime;
        currentTrack.stopwatchStart = oldStopwatchStart;
        for (int i = 0; i < currentTrack.stoplistTM.getNodes().size(); ++i) {
            Node node = currentTrack.stoplistTM.nodeAt(i);
            if (node == null)
                continue;

            double time = StopImporterDialog
                    .parseTime((String) currentTrack.stoplistTM.getValueAt(i, 0));
            node.setCoor(currentTrack.computeCoor(time));
        }
        if (currentTrack == controller.getCurrentTrack()) {
            controller.inEvent = true;
            controller.getDialog().setGpsTimeStart(oldGpsSyncTime);
            controller.getDialog().setStopwatchStart(oldStopwatchStart);
            controller.inEvent = false;
        }
    }

    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified,
            Collection<OsmPrimitive> deleted, Collection<OsmPrimitive> added) {
    }

    @Override
    public String getDescriptionText() {
        return tr("Public Transport: Relocate nodes in track stoplist");
    }
}
