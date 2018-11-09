// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.public_transport.commands;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.public_transport.actions.StopImporterAction;
import org.openstreetmap.josm.plugins.public_transport.models.TrackStoplistTableModel;

public class TrackStoplistAddCommand extends Command {
    private int workingLine;

    private TrackStoplistTableModel stoplistTM = null;

    public TrackStoplistAddCommand(StopImporterAction controller) {
        super(MainApplication.getLayerManager().getEditDataSet());
        stoplistTM = controller.getCurrentTrack().stoplistTM;
        workingLine = controller.getDialog().getStoplistTable().getSelectedRow();
    }

    @Override
    public boolean executeCommand() {
        stoplistTM.insertRow(workingLine, "00:00:00");
        return true;
    }

    @Override
    public void undoCommand() {
        int workingLine = this.workingLine;
        if (workingLine < 0)
            workingLine = stoplistTM.getRowCount() - 1;
        stoplistTM.removeRow(workingLine);
    }

    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified,
            Collection<OsmPrimitive> deleted, Collection<OsmPrimitive> added) {
    }

    @Override
    public String getDescriptionText() {
        return tr("Public Transport: Add track stop");
    }
}
