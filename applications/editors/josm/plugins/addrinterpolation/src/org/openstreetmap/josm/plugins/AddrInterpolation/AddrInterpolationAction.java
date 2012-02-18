// License: GPL. Copyright 2009 by Mike Nice and others
// Connects from JOSM menu action to Plugin
package org.openstreetmap.josm.plugins.AddrInterpolation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.Shortcut;


@SuppressWarnings("serial")
public  class AddrInterpolationAction extends JosmAction implements
SelectionChangedListener {

    public AddrInterpolationAction(){
        super(tr("Address Interpolation"), "AddrInterpolation", tr("Handy Address Interpolation Functions"),
                Shortcut.registerShortcut("tools:AddressInterpolation", tr("Tool: {0}", tr("Address Interpolation")),
                        KeyEvent.VK_Z, Shortcut.ALT_CTRL), false);
        setEnabled(false);
        DataSet.addSelectionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        AddrInterpolationDialog addrDialog = new AddrInterpolationDialog(tr("Define Address Interpolation"));


    }

    public void selectionChanged(
            Collection<? extends OsmPrimitive> newSelection) {

        for (OsmPrimitive osm : newSelection) {
            if (osm instanceof Way) {
                setEnabled(true);
                return;
            }
        }
        setEnabled(false);

    }

}


