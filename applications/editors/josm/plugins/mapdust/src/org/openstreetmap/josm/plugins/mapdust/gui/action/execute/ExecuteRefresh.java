/* Copyright (c) 2010, skobbler GmbH
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
 */
package org.openstreetmap.josm.plugins.mapdust.gui.action.execute;


import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.JToggleButton;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustRefreshObservable;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustRefreshObserver;


/**
 * Executes a refresh action. The refresh action returns all the
 * <code>MapdustBug</code> object from the current view.
 *
 * @author Bea
 *
 */
public class ExecuteRefresh extends AbstractAction implements
        MapdustRefreshObservable {

    /** The serial version UID */
    private static final long serialVersionUID = 1L;

    /** The list of MapDust refresh observers */
    private final ArrayList<MapdustRefreshObserver> observers =
            new ArrayList<MapdustRefreshObserver>();

    /**
     * Builds a <code>EexecuteRefresh<code> object
     */
    public ExecuteRefresh() {}

    /**
     * Updates the Mapdust data.
     * 
     * @param event The event which fires this action
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() instanceof JToggleButton) {
            JToggleButton btn = (JToggleButton) event.getSource();
            /* update the bugs */
            Main.pref.put("mapdust.showError", true);
            notifyObservers();
            btn.setSelected(false);
        }
    }

    /**
     * Adds a new MapDust refresh observer to the list of observers.
     *
     * @param observer The <code>MapdustRefreshObserver</code> object
     */
    @Override
    public void addObserver(MapdustRefreshObserver observer) {
        if (!this.observers.contains(observer)) {
            this.observers.add(observer);
        }
    }

    /**
     * Removes the MapDust refresh observer object from the list of observers.
     *
     * @param observer The <code>MapdustRefreshObserver</code> object
     */
    @Override
    public void removeObserver(MapdustRefreshObserver observer) {
        this.observers.remove(observer);
    }

    /**
     * Notifies the observers observing this action.
     */
    @Override
    public void notifyObservers() {
        Iterator<MapdustRefreshObserver> elements = this.observers.iterator();
        while (elements.hasNext()) {
            (elements.next()).refreshData();
        }
    }
    
}
