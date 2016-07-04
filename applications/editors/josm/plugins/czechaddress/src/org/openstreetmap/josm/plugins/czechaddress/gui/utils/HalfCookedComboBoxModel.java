// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.czechaddress.gui.utils;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * Class for shorter and faster implementations of {@link ComboBoxModel}s.
 *
 * <p>This creates a list of {@link ListDataListener}s and implements
 * method for adding and removing them. Moreover it allows to notify all
 * listeners with the generic message {@code CONTENT_CHANGED}.</p>
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public abstract class HalfCookedComboBoxModel<E> implements ComboBoxModel<E> {

    List<ListDataListener> listeners = new ArrayList<>();

    @Override
    public void addListDataListener(ListDataListener l) {
        listeners.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        listeners.remove(l);
    }

    public void notifyAllListeners() {
        ListDataEvent evt = new ListDataEvent(this,
                ListDataEvent.CONTENTS_CHANGED, 0, getSize()-1);

        for (ListDataListener l : listeners)
            l.contentsChanged(evt);
    }
}
