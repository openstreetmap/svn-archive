// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.public_transport.commands;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.public_transport.TransText;
import org.openstreetmap.josm.plugins.public_transport.dialogs.StopImporterDialog;
import org.openstreetmap.josm.plugins.public_transport.refs.TrackReference;

public class TrackStoplistNameCommand extends Command {
    private int workingLine = 0;

    private TrackReference trackref = null;

    private String oldName = null;

    private String name = null;

    private String oldTime = null;

    private String time = null;

    private String oldShelter = null;

    private TransText shelter = null;

    private LatLon oldLatLon = null;

    public TrackStoplistNameCommand(TrackReference trackref, int workingLine) {
        super(MainApplication.getLayerManager().getEditDataSet());
        this.trackref = trackref;
        this.workingLine = workingLine;
        Node node = trackref.stoplistTM.nodeAt(workingLine);
        if (node != null) {
            oldName = node.get("name");
            oldTime = trackref.stoplistTM.timeAt(workingLine);
            oldShelter = node.get("shelter");
            oldLatLon = node.getCoor();
        }
        this.time = (String) trackref.stoplistTM.getValueAt(workingLine, 0);
        this.name = (String) trackref.stoplistTM.getValueAt(workingLine, 1);
        this.shelter = (TransText) trackref.stoplistTM.getValueAt(workingLine, 2);
        if ("".equals(this.shelter.text))
            this.shelter = null;
    }

    @Override
    public boolean executeCommand() {
        Node node = trackref.stoplistTM.nodeAt(workingLine);
        if (node != null) {
            node.put("name", name);
            node.put("shelter", shelter.text);
            double dTime = StopImporterDialog.parseTime(time);
            node.setCoor(trackref.computeCoor(dTime));
        }
        trackref.inEvent = true;
        if (time == null)
            trackref.stoplistTM.setValueAt("", workingLine, 0);
        else
            trackref.stoplistTM.setValueAt(time, workingLine, 0);
        if (name == null)
            trackref.stoplistTM.setValueAt("", workingLine, 1);
        else
            trackref.stoplistTM.setValueAt(name, workingLine, 1);
        trackref.stoplistTM.setValueAt(shelter, workingLine, 2);
        trackref.inEvent = false;
        return true;
    }

    @Override
    public void undoCommand() {
        Node node = trackref.stoplistTM.nodeAt(workingLine);
        if (node != null) {
            node.put("name", oldName);
            node.put("shelter", oldShelter);
            node.setCoor(oldLatLon);
        }
        trackref.inEvent = true;
        if (oldTime == null)
            trackref.stoplistTM.setValueAt("", workingLine, 0);
        else
            trackref.stoplistTM.setValueAt(oldTime, workingLine, 0);
        if (oldName == null)
            trackref.stoplistTM.setValueAt("", workingLine, 1);
        else
            trackref.stoplistTM.setValueAt(oldName, workingLine, 1);
        trackref.stoplistTM.setValueAt(new TransText(oldShelter), workingLine, 2);
        trackref.inEvent = false;
    }

    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified,
            Collection<OsmPrimitive> deleted, Collection<OsmPrimitive> added) {
    }

    @Override
    public String getDescriptionText() {
        return tr("Public Transport: Edit track stop list");
    }
}
