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
import javax.swing.JButton;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.mapdust.gui.MapdustGUI;
import org.openstreetmap.josm.plugins.mapdust.gui.component.dialog.ChangeBugStatusDialog;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustActionObservable;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustActionObserver;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustBugObservable;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustBugObserver;
import org.openstreetmap.josm.plugins.mapdust.gui.value.MapdustAction;
import org.openstreetmap.josm.plugins.mapdust.gui.value.MapdustPluginState;
import org.openstreetmap.josm.plugins.mapdust.gui.value.MapdustServiceCommand;
import org.openstreetmap.josm.plugins.mapdust.service.MapdustServiceHandler;
import org.openstreetmap.josm.plugins.mapdust.service.MapdustServiceHandlerException;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBug;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustComment;
import org.openstreetmap.josm.plugins.mapdust.service.value.Status;


/**
 * Re-opens the given <code>MapdustBug</code> object. Validates the input data,
 * and based on the plugin state executes the corresponding action. If the
 * plugin is in the "ONLINE" state then executes the MapDust Service
 * 'changeBugStatus' action with statusId=1 and the given input parameters. If
 * the plugin is in the "OFFLINE" state then adds the given action to the action
 * list.
 *
 * @author Bea
 *
 */
public class ExecuteReOpenBug extends MapdustExecuteAction implements
        MapdustBugObservable, MapdustActionObservable {

    /** The serial version UID */
    private static final long serialVersionUID = 8275121997644127251L;

    /** The list of MapDust bug observers */
    private final ArrayList<MapdustBugObserver> bugObservers =
            new ArrayList<MapdustBugObserver>();

    /** The list of MapDust action observers */
    private final ArrayList<MapdustActionObserver> actionObservers =
            new ArrayList<MapdustActionObserver>();

    /**
     * Builds a <code>ExecuteReOpenBug</code> object
     */
    public ExecuteReOpenBug() {}

    /**
     * Builds a <code>ExecuteReOpenBug</code> object with the given arguments.
     *
     * @param dialog The <code>ChangeIssueStatusDialog</code> object
     * @param mapdustGUI The <code>MapdustGUI</code> object
     */
    public ExecuteReOpenBug(ChangeBugStatusDialog dialog,
            MapdustGUI mapdustGUI) {
        setDialog(dialog);
        setMapdustGUI(mapdustGUI);
    }

    /**
     *
     * @param event The event which fires this action
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() instanceof JButton) {
            JButton btn = (JButton) event.getSource();
            if (btn.getText().equals("OK")) {
                /* pressed ok */
                ChangeBugStatusDialog issueDialog =
                        (ChangeBugStatusDialog) getDialog();
                String nickname = issueDialog.getTxtNickname().getText();
                String commentText = issueDialog.getTxtDescription().getText();
                /* validate */
                String errorMessage = validate(nickname, commentText);
                if (errorMessage != null) {
                    /* invalid data */
                    JOptionPane.showMessageDialog(Main.parent, tr(errorMessage),
                            tr("Missing input data"), JOptionPane.WARNING_MESSAGE);
                    return;
                }
                /* valid */
                Main.pref.put("mapdust.nickname", nickname);
                MapdustBug selectedBug = mapdustGUI.getSelectedBug();
                MapdustComment comment = new MapdustComment(selectedBug.getId(),
                        commentText, nickname);
                String pluginState = Main.pref.get("mapdust.pluginState");
                if (pluginState.equals(MapdustPluginState.OFFLINE.getValue())) {
                    /* offline , save to action list */
                    selectedBug.setStatus(Status.OPEN);
                    String iconPath = getIconPath(selectedBug);
                    MapdustAction mapdustAction = new MapdustAction(
                            MapdustServiceCommand.CHANGE_BUG_STATUS, iconPath,
                            selectedBug, comment, 1);
                    /* destroy dialog */
                    mapdustGUI.enableBtnPanel(true);
                    enableFiredButton(issueDialog.getFiredButton());
                    issueDialog.dispose();
                    if (getMapdustGUI().getActionPanel() != null) {
                        notifyObservers(mapdustAction);
                    }
                } else {
                    /* online, call MapDust service method */
                    MapdustServiceHandler handler = new MapdustServiceHandler();
                    Long id = null;
                    try {
                        id = handler.changeBugStatus(1, comment);
                    } catch (MapdustServiceHandlerException e) {
                        errorMessage = "There was a Mapdust service error.";
                        errorMessage += "Mapdust bug report.";
                        JOptionPane.showMessageDialog(Main.parent,
                                tr(errorMessage), tr("Error"),
                                JOptionPane.ERROR_MESSAGE);
                    }
                    if (id != null) {
                        MapdustBug newMapdustBug = null;
                        try {
                            newMapdustBug = handler.getBug(selectedBug.getId(),
                                    null);
                        } catch (MapdustServiceHandlerException e) {
                            errorMessage = "There was a Mapdust service error.";
                            errorMessage += "Mapdust bug report.";
                            JOptionPane.showMessageDialog(Main.parent,
                                    tr(errorMessage), tr("Error"),
                                    JOptionPane.ERROR_MESSAGE);
                        }
                        mapdustGUI.enableBtnPanel(false);
                        enableFiredButton(issueDialog.getFiredButton());
                        issueDialog.dispose();
                        if (newMapdustBug != null) {
                            notifyObservers(newMapdustBug);
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds a new MapDust bug observer to the list of observers.
     *
     * @param observer The <code>MapdustBugObserver</code> object
     */
    @Override
    public void addObserver(MapdustBugObserver observer) {
        if (!this.bugObservers.contains(observer)) {
            this.bugObservers.add(observer);
        }
    }

    /**
     * Adds a new MapDust action list observer to the list of observers.
     *
     * @param observer The <code>MapdustActionListObserver</code> object
     */
    @Override
    public void addObserver(MapdustActionObserver observer) {
        if (!this.actionObservers.contains(observer)) {
            this.actionObservers.add(observer);
        }
    }

    /**
     * Removes the MapDust bug observer object from the list of observers.
     *
     * @param observer The <code>MapdustBugObserver</code> object
     */
    @Override
    public void removeObserver(MapdustBugObserver observer) {
        this.bugObservers.remove(observer);

    }

    /**
     * Removes the MapDust action list observer object from the list of
     * observers.
     *
     * @param observer The <code>MapdustActionListObserver</code> object
     */
    @Override
    public void removeObserver(MapdustActionObserver observer) {
        this.actionObservers.remove(observer);

    }

    /**
     * Notifies the observers observing this action.
     */
    @Override
    public void notifyObservers(MapdustBug mapdustBug) {
        Iterator<MapdustBugObserver> elements = this.bugObservers.iterator();
        while (elements.hasNext()) {
            (elements.next()).changedData(mapdustBug);
        }
    }

    /**
     * Notifies the observers observing this action.
     */
    @Override
    public void notifyObservers(MapdustAction mapdustAction) {
        Iterator<MapdustActionObserver> elements =
                this.actionObservers.iterator();
        while (elements.hasNext()) {
            (elements.next()).addAction(mapdustAction);
        }
    }

}
