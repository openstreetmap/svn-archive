/*
 * Copyright (c) 2010, skobbler GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Created on Feb 10, 2011 by Bea
 * Modified on $DateTime$ by $Author$
 */
package org.openstreetmap.josm.plugins.mapdust.gui.action.execute;


import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import org.openstreetmap.josm.plugins.mapdust.gui.MapdustGUI;
import org.openstreetmap.josm.plugins.mapdust.gui.component.dialog.FilterBugDialog;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustUpdateObservable;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustUpdateObserver;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBugFilter;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustRelevance;


/**
 * Filters the MapDust bugs from the given area based on the selected filters.
 * The filters can be based on the status, type or description of the bugs.
 *
 * @author Bea
 */
public class ExecuteFilterBug extends MapdustExecuteAction implements
        MapdustUpdateObservable {

    /** The serial version UID */
    private static final long serialVersionUID = -2724396161610512502L;

    /** The list MapDustBug filter observers */
    private final ArrayList<MapdustUpdateObserver> observers =
            new ArrayList<MapdustUpdateObserver>();

    /**
     * Builds a <code>ExecuteFilterBug</code> object.
     */
    public ExecuteFilterBug() {}

    /**
     * Builds a <code>ExecuteFilterBug</code> object based on the given
     * arguments.
     *
     * @param dialog The MapDust filter bug dialog window
     * @param mapdustGUI The <code>MapdustGUI</code> object
     */
    public ExecuteFilterBug(FilterBugDialog dialog, MapdustGUI mapdustGUI) {
        setDialog(dialog);
        setMapdustGUI(mapdustGUI);
    }

    /**
     * Applies the selected filters for the MapDust bugs from the current view.
     *
     * @param event The action event which fires this action
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() instanceof JButton) {
            JButton btn = (JButton) event.getSource();
            if (btn.getText().equals("Apply")) {
                /* pressed apply */
                FilterBugDialog dialog = (FilterBugDialog) getDialog();
                /* get the selected type filters */
                List<String> types = dialog.getCheckedTypes();
                /* get the selected status filters */
                List<Integer> statuses = dialog.getCheckedStatuses();
                /* get the selected description filter */
                boolean descr = dialog.isDescrFilterChecked();
                MapdustRelevance minValue = dialog.getSelectedMinRelevance();
                MapdustRelevance maxValue= dialog.getSelectedMaxRelevance();
                /* notifies the observers about the filters */
                notifyObservers(new MapdustBugFilter(statuses, types, descr,
                        minValue, maxValue), false);
                enableFiredButton(dialog.getFiredButton());
                mapdustGUI.enableBtnPanel(false);
                /* destroy dialog */
                dialog.dispose();
            }
        }
    }

    /**
     * Adds a new MapDust bug filter observer to the list of observers.
     *
     * @param observer The <code>MapdustBugFilterObserver</code> object
     */
    @Override
    public void addObserver(MapdustUpdateObserver observer) {
        if (!this.observers.contains(observer)) {
            this.observers.add(observer);
        }
    }

    /**
     * Removes the MapDust bug filter observer object from the list of
     * observers.
     *
     * @param observer The <code>MapdustBugFilterObserver</code> object
     */
    @Override
    public void removeObserver(MapdustUpdateObserver observer) {
        this.observers.remove(observer);
    }

    /**
     * Notifies the observers observing this action.
     *
     * @param filter The <code>MapdustBugFilter</code> object
     */
    @Override
    public void notifyObservers(MapdustBugFilter filter, boolean first) {
        Iterator<MapdustUpdateObserver> elements = this.observers.iterator();
        while (elements.hasNext()) {
            (elements.next()).update(filter, false);
        }
    }

}
