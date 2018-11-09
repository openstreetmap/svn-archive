// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.public_transport.models;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.public_transport.TransText;
import org.openstreetmap.josm.plugins.public_transport.actions.StopImporterAction;
import org.openstreetmap.josm.plugins.public_transport.commands.WaypointsNameCommand;

public class WaypointTableModel extends DefaultTableModel implements TableModelListener {
    private StopImporterAction controller = null;

    public boolean inEvent = false;

    public Vector<Node> nodes = new Vector<>();

    public Vector<LatLon> coors = new Vector<>();

    public WaypointTableModel(StopImporterAction controller) {
        this.controller = controller;
        addColumn(tr("Time"));
        addColumn(tr("Stopname"));
        addColumn(tr("Shelter"));
        addTableModelListener(this);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        if (column >= 1)
            return true;
        return false;
    }

    @Override
    public void addRow(Object[] obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insertRow(int insPos, Object[] obj) {
        throw new UnsupportedOperationException();
    }

    public void addRow(WayPoint wp) {
        insertRow(-1, wp);
    }

    public void insertRow(int insPos, WayPoint wp) {
        String time = wp.getString("time");
        if (time == null)
            time = "";
        String name = wp.getString("name");
        if (name == null)
            name = "";

        Node node = controller.createNode(wp.getCoor(), name);

        Object[] buf = {time, name, new TransText(null)};
        if (insPos == -1) {
            nodes.addElement(node);
            coors.addElement(wp.getCoor());
            super.addRow(buf);
        } else {
            nodes.insertElementAt(node, insPos);
            coors.insertElementAt(wp.getCoor(), insPos);
            super.insertRow(insPos, buf);
        }
    }

    public void clear() {
        nodes.clear();
        super.setRowCount(0);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if (e.getType() == TableModelEvent.UPDATE) {
            if (inEvent)
                return;
            UndoRedoHandler.getInstance().add(new WaypointsNameCommand(this, e.getFirstRow(),
                    (String) getValueAt(e.getFirstRow(), 1),
                    (TransText) getValueAt(e.getFirstRow(), 2)));
        }
    }
}
