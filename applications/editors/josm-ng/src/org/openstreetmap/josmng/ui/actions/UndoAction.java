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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openstreetmap.josmng.ui.Main;
import org.openstreetmap.josmng.utils.UndoHelper;
import org.openstreetmap.josmng.view.EditableLayer;
import org.openstreetmap.josmng.view.MapView;

/**
 *
 * @author nenik
 */
public class UndoAction extends AbstractAction {

    UndoHelper current;
    
    private PropL listener = new PropL();
    
    public UndoAction() {
        super("Undo");
        Main.main.getMapView().addPropertyChangeListener(MapView.PROP_LAYER, listener);
        updateManager();
    }

    public void actionPerformed(ActionEvent e) {
        if (current != null && current.canUndo()) {
            current.undo();
            updateState();
        }
    }

    private void updateManager() {
        if (current != null) current.removeChangeListener(listener);
        EditableLayer layer = Main.main.getMapView().getCurrentEditLayer();
        current = layer == null ? null : layer.getUndoManager();
        if (current != null) current.addChangeListener(listener);
        updateState();
    }
    
    private void updateState() {
        if (current == null) {
            setEnabled(false);
            putValue(Action.NAME, "Undo");
        } else {
            setEnabled(current.canUndo());
            putValue(Action.NAME, current.getUndoPresentationName());
        }       
    }
    
    private class PropL implements PropertyChangeListener, ChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (MapView.PROP_LAYER.equals(evt.getPropertyName())) {
                updateManager();
            }
        }

        public void stateChanged(ChangeEvent e) {
            updateState();
        }
        
    }
    
}
