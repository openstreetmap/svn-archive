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


import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.mapdust.gui.MapdustActionUploader;
import org.openstreetmap.josm.plugins.mapdust.gui.MapdustActionUploaderException;
import org.openstreetmap.josm.plugins.mapdust.gui.MapdustGUI;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustUpdateObservable;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustUpdateObserver;
import org.openstreetmap.josm.plugins.mapdust.gui.value.MapdustPluginState;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBugFilter;


/**
 * Executes the "work offline" action. In the offline mode the user actions will
 * not be uploaded immediately to the MapDust service. The user can perform the
 * following actions in offline mode: add bug, comment bug and change bug
 * status. After de-activating the offline mode the user's modifications will be
 * uploaded to the MapDust service.
 *
 * @author Bea
 *
 */
public class ExecuteWorkOffline extends MapdustExecuteAction implements
        MapdustUpdateObservable {

    /** The serial version UID */
    private static final long serialVersionUID = 8792828131813689548L;

    /** The list of MapDust refresh observers */
    private final ArrayList<MapdustUpdateObserver> observers =
            new ArrayList<MapdustUpdateObserver>();

    /**
     * Builds a <code>ExecuteWorkOffline</code> object.
     */
    public ExecuteWorkOffline() {}

    /**
     * Builds a <code>ExecuteWorkOffline</code> object based on the given
     * arguments.
     *
     * @param mapdustGUI The <code>MapdustGUI</code> object
     */
    public ExecuteWorkOffline(MapdustGUI mapdustGUI) {
        setMapdustGUI(mapdustGUI);
    }

    /**
     * Sets the 'offline' mode for the plugin.
     *
     * @param event The event which fires this action
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() instanceof JToggleButton) {
            JToggleButton btn = (JToggleButton) event.getSource();
            if (getMapdustGUI() != null) {
                String pluginState = Main.pref.get("mapdust.pluginState");
                if (pluginState.equals(MapdustPluginState.ONLINE.getValue())) {
                    Main.pref.put("mapdust.pluginState",
                            MapdustPluginState.OFFLINE.getValue());
                    btn.setSelected(false);
                    btn.setFocusable(false);
                } else {
                    // was offline, becomes online
                    String title = "MapDust";
                    String message = "Do you want to submit your changes ";
                    message += "to Mapdust?";
                    int result = JOptionPane.showConfirmDialog(Main.parent,
                            tr(message), tr(title), JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        try {
                            MapdustActionUploader.getInstance().uploadData(
                                    getMapdustGUI().getMapdustActionList());
                        } catch (MapdustActionUploaderException e) {
                            String errorMessage = "There was a Mapdust service";
                            errorMessage+=" error.";
                            JOptionPane.showMessageDialog(Main.parent,
                                    tr(errorMessage), tr("Error"),
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    Main.pref.put("mapdust.pluginState",
                            MapdustPluginState.ONLINE.getValue());
                    btn.setSelected(false);
                    btn.setFocusable(false);
                }
                notifyObservers(null, false);
            }
        }
    }

    /**
     * Adds a new MapDust refresh observer to the list of observers.
     *
     * @param observer The <code>MapdustRefreshObserver</code> object
     */
    @Override
    public void addObserver(MapdustUpdateObserver observer) {
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
    public void removeObserver(MapdustUpdateObserver observer) {
        this.observers.remove(observer);
    }

    /**
     * Notifies the observers observing this action.
     */
    @Override
    public void notifyObservers(MapdustBugFilter filter, boolean first) {
        Iterator<MapdustUpdateObserver> elements = this.observers.iterator();
        while (elements.hasNext()) {
            (elements.next()).update(filter, false);
        }
    }

}
