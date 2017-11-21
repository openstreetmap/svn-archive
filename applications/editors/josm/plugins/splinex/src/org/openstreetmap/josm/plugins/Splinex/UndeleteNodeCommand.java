// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.Splinex;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.DefaultNameFormatter;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;

class UndeleteNodeCommand extends Command {
    Node n;
    boolean wasModified;

    UndeleteNodeCommand(Node n) {
        super(MainApplication.getLayerManager().getEditDataSet());
        this.n = n;
    }

    @Override
    public boolean executeCommand() {
        if (!n.isDeleted())
            return false;
        n.setDeleted(false);
        wasModified = n.isModified();
        n.setModified(true);
        return true;
    }

    @Override
    public void undoCommand() {
        n.setDeleted(true);
        n.setModified(wasModified);
    }

    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified, Collection<OsmPrimitive> deleted,
            Collection<OsmPrimitive> added) {
        modified.add(n);
    }

    @Override
    public String getDescriptionText() {
        return tr("Undelete node {0}", n.getDisplayName(DefaultNameFormatter.getInstance()));
    }
}
