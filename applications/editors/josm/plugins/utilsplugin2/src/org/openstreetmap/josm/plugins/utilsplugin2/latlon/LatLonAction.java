// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.plugins.utilsplugin2.latlon;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.util.Collection;
import java.util.LinkedList;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.actions.JosmAction;

/**
 * This action displays a dialog where the user can enter a latitude and longitude,
 * and when ok is pressed, a new node is created at the specified position.
 */
public final class LatLonAction extends JosmAction {
    // remember input from last time
    private String textLatLon;

    public LatLonAction() {
        super(tr("Lat Lon tool"), "latlon", tr("Create geometry by entering lat lon coordinates for it."),
                Shortcut.registerShortcut("latlon", tr("Edit: {0}", tr("Lat Lon tool")), KeyEvent.VK_L, Shortcut.CTRL_SHIFT), true);
        putValue("help", ht("/Action/AddNode"));
    }

    public void actionPerformed(ActionEvent e) {
        if (!isEnabled())
            return;

        LatLonDialog dialog = new LatLonDialog(Main.parent, tr("Add Node..."), ht("/Action/AddNode"));

        if (textLatLon != null) {
            dialog.setLatLonText(textLatLon);
        }

        dialog.showDialog();
        
        if (dialog.getValue() != 1)
            return;

        LatLon[] coordinates = dialog.getCoordinates();
        String type = dialog.getGeomType();
        if (coordinates == null)
            return;

        textLatLon = dialog.getLatLonText();

        // we create a list of commands that will modify the map in the way we want.
        Collection<Command> cmds = new LinkedList<Command>();
        // first we create all the nodes, then we do extra stuff based on what geometry type we need.
        LinkedList<Node> nodes = new LinkedList<Node>();

        for (LatLon ll : coordinates) {
            Node nnew = new Node(ll);
            nodes.add(nnew);
            cmds.add(new AddCommand(nnew));
        }

        if (type == "nodes") {
            //we dont need to do anything, we already have all the nodes
        }
        if (type == "way") {
            Way wnew = new Way();
            wnew.setNodes(nodes);
            cmds.add(new AddCommand(wnew));
        }
        if (type == "area") {
            nodes.add(nodes.get(0)); // this is needed to close the way.
            Way wnew = new Way();
            wnew.setNodes(nodes);
            cmds.add(new AddCommand(wnew));
        }
        Main.main.undoRedo.add(new SequenceCommand(tr("Lat Lon tool"), cmds));
        Main.map.mapView.repaint();
    }

    @Override
    protected void updateEnabledState() {
        setEnabled(getEditLayer() != null);
    }
}
