/*
 *  JOSMng - a Java Open Street Map editor, the next generation.
 * 
 *  Copyright (C) 2008 Petr Nejedly <P.Nejedly@sh.cvut.cz>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.openstreetmap.josmng.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import javax.swing.Icon;
import org.openstreetmap.josmng.osm.DataSet;
import org.openstreetmap.josmng.ui.Main;
import org.openstreetmap.josmng.view.EditableLayer;
import org.openstreetmap.josmng.view.osm.OsmLayer;

/**
 * An action wrapper that encapsulate DataSet related action so they
 * automatically run inside DataSet.atomicEdit. This, togehter with providing
 * current layer and its DataSet allows removing most of the boilerplate code.
 * 
 * @author nenik
 */
public abstract class AtomicDataSetAction extends AbstractAction {
    protected AtomicDataSetAction(String label) {
        super(label);
    }
    
    protected AtomicDataSetAction(String label, Icon icon) {
        super(label, icon);
    }

    public void actionPerformed(final ActionEvent e) {
        final EditableLayer layer = Main.main.getMapView().getCurrentEditLayer();
        if (layer instanceof OsmLayer) {
            final DataSet ds = ((OsmLayer)layer).getDataSet();
            ds.atomicEdit(new Runnable() {
                public void run() {
                    perform((OsmLayer)layer, ds, e);
                }
            }, null);
        }
    }
    
    public abstract void perform(OsmLayer layer, DataSet ds, ActionEvent ae);
}
