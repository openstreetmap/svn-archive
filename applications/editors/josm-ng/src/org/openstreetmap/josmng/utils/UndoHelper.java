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

package org.openstreetmap.josmng.utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoManager;

/**
 * An UndoManager that notifies interested parties (typically Undo/Redo action)
 * whenever the status and UI methods potentially change the result.
 * This usually happens if new UndoableEdit is added, or undone/redone.
 * 
 * @author nenik
 */
public class UndoHelper extends UndoManager {
    private List<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();
    
    public UndoHelper() {
    }

    public void addChangeListener(ChangeListener listener) {
        assert(listener != null);
        listeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        assert(listener != null);
        listeners.remove(listeners);
    }
    
    private void fireChange() {
        ChangeEvent evt = new ChangeEvent(this);
        for (ChangeListener l : listeners) {
            l.stateChanged(evt);
        }
    }

    public @Override void undoableEditHappened(UndoableEditEvent e) {
        super.undoableEditHappened(e);
        fireChange();
    }

    public @Override synchronized void discardAllEdits() {
        super.discardAllEdits();
        fireChange();
    }    
}
