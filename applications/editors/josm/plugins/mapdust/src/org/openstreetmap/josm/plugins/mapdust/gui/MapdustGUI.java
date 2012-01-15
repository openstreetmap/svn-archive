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
package org.openstreetmap.josm.plugins.mapdust.gui;


import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.plugins.mapdust.MapdustPlugin;
import org.openstreetmap.josm.plugins.mapdust.gui.component.panel.MapdustActionPanel;
import org.openstreetmap.josm.plugins.mapdust.gui.component.panel.MapdustBugListPanel;
import org.openstreetmap.josm.plugins.mapdust.gui.component.panel.MapdustBugPropertiesPanel;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustActionObserver;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustBugDetailsObservable;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustBugDetailsObserver;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustUpdateObservable;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustUpdateObserver;
import org.openstreetmap.josm.plugins.mapdust.gui.value.MapdustAction;
import org.openstreetmap.josm.plugins.mapdust.gui.value.MapdustPluginState;
import org.openstreetmap.josm.plugins.mapdust.gui.value.MapdustServiceCommand;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBug;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBugFilter;
import org.openstreetmap.josm.plugins.mapdust.service.value.Status;
import org.openstreetmap.josm.tools.Shortcut;


/**
 * This class is the main graphical user interface class.
 *
 * @author Bea
 */
public class MapdustGUI extends ToggleDialog implements MapdustActionObserver,
        MapdustBugDetailsObservable, MapdustUpdateObservable {

    /** The serial version UID */
    private static final long serialVersionUID = -1194197412364335190L;

    /** The list of MapDust bug details observers */
    private final ArrayList<MapdustBugDetailsObserver> bugDetailsObservers =
            new ArrayList<MapdustBugDetailsObserver>();

    /** The list of MapDust initial update observers */
    private final ArrayList<MapdustUpdateObserver> initialUpdateObservers =
            new ArrayList<MapdustUpdateObserver>();

    /** The <code>MapdustPlugin</code> plug-in */
    private MapdustPlugin mapdustPlugin;

    /** The <code>MapdustBugPropertiesPanel</code> */
    private MapdustBugPropertiesPanel detailsPanel;

    /** The <code>MapdustBugListPanel</code> object */
    private MapdustBugListPanel panel;

    /** The <code>MapdustActionPanel</code> object */
    private MapdustActionPanel actionPanel;

    /** The <code>JTabbedPanel</code> object */
    private JTabbedPane tabbedPane;

    /** The <code>JPanel</code> */
    private JPanel mainPanel;

    /** Specifies if the MapDust data was or not down-loaded */
    private boolean downloaded = false;

    /**
     * Builds a <code>MapdustGUi</code> based on the given parameters.
     *
     * @param name The name of the GUI
     * @param iconName The name of the icon
     * @param tooltip The tool tip
     * @param shortcut The shortcut
     * @param preferredHeight The height
     * @param mapdustPlugin The <code>MapdustPlugin</code> object
     */
    public MapdustGUI(String name, String iconName, String tooltip,
            Shortcut shortcut, int preferredHeight, MapdustPlugin mapdustPlugin) {
        super(tr(name), iconName, tr(tooltip), shortcut, preferredHeight);
        this.mapdustPlugin = mapdustPlugin;
    }

    /**
     * Displays the <code>MapdustGUI</code> dialog window in the JOSM editor. If
     * the MapDust data was not down-loaded yet or the MapDust layer was added
     * after a previous deletion, then the bug reports data will be deleted.
     */
    @Override
    public void showDialog() {
        if (!downloaded) {
            notifyObservers(null, true);
            downloaded = true;
        }
        super.showDialog();
    }

    /**
     * Destroys the <code>MapdustGUI</code> dialog window.
     */
    @Override
    public void destroy() {
        setVisible(false);
        /* remove panels */
        if (tabbedPane != null) {
            /* from off-line to online */
            remove(mainPanel);
            tabbedPane = null;
            actionPanel = null;
            mainPanel = null;
            panel = null;
            actionPanel = null;
        } else {
            /* from online to off-line */
            if (mainPanel != null) {
                remove(mainPanel);
                mainPanel = null;
                panel = null;
                detailsPanel = null;
            }
        }
        downloaded = false;
        button.setSelected(false);
        super.destroy();
    }

    @Override
    protected void toggleButtonHook() {
        if (isVisible()) {
            setVisible(false);
            button.setSelected(false);
        } else {
            setVisible(true);
        }
    }

    /**
     * Adds the given <code>MapdustAction</code> object to the list of actions.
     *
     * @param action The <code>MapdustAction</code> object
     */
    @Override
    public synchronized void addAction(MapdustAction action) {
        /* add the action */
        List<MapdustAction> actionList = actionPanel.getActionList();
        actionList.add(action);
        List<MapdustBug> mapdustBugs = panel.getMapdustBugsList();
        boolean showBug = shouldDisplay(action.getMapdustBug(),
                mapdustPlugin.getFilter());
        mapdustBugs = modifyBug(mapdustBugs, action.getMapdustBug(), showBug);
        /* update panels */
        updateMapdustPanel(mapdustBugs);
        updateMapdustActionPanel(actionList);
        if (showBug && !action.getCommand().equals(MapdustServiceCommand.ADD_BUG)) {
            panel.resetSelectedBug(0);
        } else {
            mapdustPlugin.getMapdustLayer().setBugSelected(null);
        }
        revalidate();
        Main.map.mapView.revalidate();
        Main.map.repaint();
    }

    /**
     * Verifies if the given <code>MapdustBug</code> should be shown on the
     * MapDust bug list/ map. Any <code>MapdustBug</code> should be shown on the
     * MapDust bug list / map only if it is permitted by the selected filters.
     *
     * @param modifiedBug The <code>MapdustBug</code> object
     * @param filter The <code>MapdustBugFilter</code> object
     * @return true if the given MapDust bug should be shown in the list/map
     * false otherwise
     */
    private boolean shouldDisplay(MapdustBug modifiedBug,
            MapdustBugFilter filter) {
        boolean result = false;
        if (filter != null && filter.getStatuses() != null
                && !filter.getStatuses().isEmpty()) {
            if (filter.getStatuses().contains(modifiedBug.getStatus().getKey())) {
                result = true;
            }
        } else {
            result = true;
        }
        return result;
    }

    /**
     * Updates the MapDust GUI with the given list of <code>MapdustBug</code>
     * objects.
     *
     * @param mapdustBugs The list of <code>MapdustBug</code> objects
     * @param mapdustPlugin The <code>MapdustPlugin</code> object
     */
    public synchronized void update(List<MapdustBug> mapdustBugs,
            MapdustPlugin mapdustPlugin) {
        setMapdustPlugin(mapdustPlugin);
        String pluginState = Main.pref.get("mapdust.pluginState");
        if (pluginState.equals(MapdustPluginState.ONLINE.getValue())) {
            if (tabbedPane != null) {
                /* from off-line to online */
                remove(mainPanel);
                tabbedPane = null;
                actionPanel = null;
                mainPanel = null;
                panel = null;
            }
            updateMapdustPanel(mapdustBugs);
            if (mainPanel == null) {
                createMainPanel();
            }
        } else {
            if (tabbedPane == null) {
                /* from online to off-line */
                remove(mainPanel);
                mainPanel = null;
                panel = null;
            }
            List<MapdustAction> actionList = actionPanel != null ?
                    actionPanel.getActionList() : new ArrayList<MapdustAction>();

            /* update panels */
            List<MapdustBug> bugs = filterMapdustBugList(mapdustBugs,
                    actionList, mapdustPlugin.getFilter());
            updateMapdustPanel(bugs);
            updateMapdustActionPanel(actionList);
            if (mainPanel == null) {
                createMainPanel();
            }
        }
    }

    /**
     * Updates the MapDust bugs panel with the new list of data.
     *
     * @param mapdustBugs The list of <code>MapdustBug</code> objects
     */
    private void updateMapdustPanel(List<MapdustBug> mapdustBugs) {
        MapdustBug selectedBug = (mapdustBugs != null && mapdustBugs.size()
                > 0) ? mapdustBugs.get(0) : null;
        if (detailsPanel == null) {
            detailsPanel = new MapdustBugPropertiesPanel(selectedBug);
            addObserver(detailsPanel);
        }
        if (panel == null) {
            panel = new MapdustBugListPanel(mapdustBugs, "Bug reports",
                    mapdustPlugin);
            panel.addObserver(detailsPanel);
        } else {
            panel.updateComponents(mapdustBugs);
            notifyObservers(selectedBug);
        }
    }

    /**
     * Updates the MapDust action panel with the new list of data.
     *
     * @param actionList The list of <code>MapdustAction</code> objects
     */
    private void updateMapdustActionPanel(List<MapdustAction> actionList) {
        if (actionPanel == null) {
            actionPanel = new MapdustActionPanel(actionList,
                    "Offline Contribution", mapdustPlugin);
        } else {
            actionPanel.updateComponents(actionList);
        }
    }

    /**
     * Creates the main panel of the plug-in and adds to the content pane.
     */
    private void createMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setAutoscrolls(true);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(detailsPanel, BorderLayout.NORTH);
        if (actionPanel == null) {
            mainPanel.add(panel, BorderLayout.CENTER);
        } else {
            tabbedPane = new JTabbedPane();
            tabbedPane.add(panel, 0);
            tabbedPane.add(actionPanel);
            mainPanel.add(tabbedPane, BorderLayout.CENTER);
        }
        createLayout(mainPanel, false, null);
    }


    /**
     * Filters the given list of <code>MapdustBug</code>s based on the given
     * list of <code>MapdustAction</code>s. The filtering is done in order to
     * show the modified bugs ( but not already committed operations) if they
     * also appears in the new list of bugs, according to the latest
     * modifications.
     *
     * @param bugList The list of <code>MapdustBug</code> objects
     * @param actionList The list of <code>MapdustAction</code> objects
     * @param filter The <code>MapdustBugFilter</code> object
     * @return A filtered list of <code>MapdustBug</code>s
     */
    private List<MapdustBug> filterMapdustBugList(List<MapdustBug> bugList,
            List<MapdustAction> actionList, MapdustBugFilter filter) {
        if (bugList != null && actionList != null) {
            for (MapdustAction action : actionList) {
                int index = bugList.indexOf(action.getMapdustBug());
                if (index >= 0) {
                    if (action.getNewStatus() != null) {
                        Status newStatus =
                                Status.getStatus(action.getNewStatus());
                        if (filter != null && filter.getStatuses() != null
                                && !filter.getStatuses().isEmpty()) {
                            if (filter.getStatuses().contains(
                                    newStatus.getKey())) {
                                bugList.get(index).setStatus(newStatus);
                            } else {
                                bugList.remove(index);
                            }
                        } else {
                            bugList.get(index).setStatus(newStatus);
                        }
                    }
                }
            }
        }
        return bugList;
    }

    /**
     * Modifies the given <code>MapdustBug</code> in the given list of
     * <code>MapdustBug</code> objects. Returns the list of bugs containing the
     * modified bug.
     *
     * @param mapdustBugs The list of <code>MapdustBug</code> objects
     * @param modifiedBug The <code>MapdustBug</code> object
     * @param showBug A flag indicating if the given modified bug should be
     * displayed on the map and on the list of bugs
     * @return the modified list
     */
    private List<MapdustBug> modifyBug(List<MapdustBug> mapdustBugs,
            MapdustBug modifiedBug, boolean showBug) {
        int index = -1;
        for (int i = 0; i < mapdustBugs.size(); i++) {
            if (modifiedBug.getId() != null) {
                if (mapdustBugs.get(i).getId().equals(modifiedBug.getId())) {
                    index = i;
                }
            }
        }
        if (index != -1) {
            /* remove, and add to the top of the list */
            mapdustBugs.remove(index);
            if (showBug) {
                mapdustBugs.add(0, modifiedBug);
            }
        }
        return mapdustBugs;
    }

    /**
     * Returns the selected bug from the list of bugs. If there is no bug
     * selected then the returned result is null.
     *
     * @return The selected bug
     */
    public MapdustBug getSelectedBug() {
        MapdustBug selectedBug = null;
        if (panel != null) {
            selectedBug = panel.getSelectedBug();
        }
        return selectedBug;
    }

    /**
     * Sets the given <code>MapdustBug</code> to be selected from the list of
     * bugs.
     *
     * @param mapdustBug The <code>MapdustBug</code> object
     */
    public void setSelectedBug(MapdustBug mapdustBug) {
        if (panel != null) {
            panel.setSelectedBug(mapdustBug);
        }
    }

    /**
     * Disables the buttons from the <code>MapdustPanel</code>.
     *
     */
    public void disableBtnPanel() {
        if (panel != null) {
            panel.disableBtnPanel();
        }
    }

    /**
     * Enables the basic components from the <code>MapdustButtonPanel</code>.
     * Basic components are considered the following buttons: work off-line,
     * filter bug report, and refresh.If the onlyBasic flag is true then the
     * other buttons will be disabled.
     *
     * @param onlyBasic If true then the not basic buttons will be disabled
     */
    public void enableBtnPanel(boolean onlyBasic) {
        if (panel != null) {
            panel.enableBtnPanel(onlyBasic);
        }
    }

    /**
     * Returns the list of <code>MapdustAction</code> objects.
     *
     * @return list of <code>MapdustAction</code>
     */
    public List<MapdustAction> getMapdustActionList() {
        return getActionPanel().getActionList();
    }

    /**
     * Adds a new MapDust bug details observer to the list of observers.
     *
     * @param observer The <code>MapdustBugDetailsObserver</code> object
     */
    @Override
    public void addObserver(MapdustBugDetailsObserver observer) {
        if (!this.bugDetailsObservers.contains(observer)) {
            this.bugDetailsObservers.add(observer);
        }
    }

    /**
     * Adds a new MapDust update observer to the list of observers.
     *
     * @param observer The <code>MapdustUpdateObserver</code> object
     */
    @Override
    public void addObserver(MapdustUpdateObserver observer) {
        if (!this.initialUpdateObservers.contains(observer)) {
            this.initialUpdateObservers.add(observer);
        }
    }

    /**
     * Removes the given MapDust bug details observer from the list of
     * observers.
     *
     * @param observer The <code>MapdustBugDetailsObserver</code> object
     */
    @Override
    public void removeObserver(MapdustBugDetailsObserver observer) {
        this.bugDetailsObservers.remove(observer);

    }

    /**
     * Removes the given MapDust initial update observer from the list of
     * observers.
     *
     * @param observer The <code>MapdustInitialUpdateObserver</code> object
     */
    @Override
    public void removeObserver(MapdustUpdateObserver observer) {
        this.initialUpdateObservers.remove(observer);

    }

    /**
     * Notifies the <code>MapdustBugDetailsObserver</code> objects observing the
     * given <code>MapdustBug</code> object.
     */
    @Override
    public void notifyObservers(MapdustBug mapdustBug) {
        Iterator<MapdustBugDetailsObserver> elements =
                this.bugDetailsObservers.iterator();
        while (elements.hasNext()) {
            (elements.next()).showDetails(mapdustBug);
        }
    }

    /**
     * Notifies the <code>MapdustInitialUpdateObserver</code> objects waiting
     * for the initial down-load, and update of plug-in.
     */
    @Override
    public void notifyObservers(MapdustBugFilter filter, boolean first) {
        Iterator<MapdustUpdateObserver> elements =
                this.initialUpdateObservers.iterator();
        while (elements.hasNext()) {
            (elements.next()).update(filter, first);
        }
    }

    /**
     * Returns the <code>MapdustPanel</code> object
     *
     * @return the panel
     */
    public MapdustBugListPanel getPanel() {
        return panel;
    }

    /**
     * Returns the <code>MapdustActionPanel</code> object
     *
     * @return the queuePanel
     */
    public MapdustActionPanel getActionPanel() {
        return actionPanel;
    }

    /**
     * Returns the <code>MapdustPlugin</code> object
     *
     * @return the mapdustPlugin
     */
    public MapdustPlugin getMapdustPlugin() {
        return mapdustPlugin;
    }

    /**
     * Sets the <code>MapdustPlugin</code> object
     *
     * @param mapdustPlugin the mapdustPlugin to set
     */
    public void setMapdustPlugin(MapdustPlugin mapdustPlugin) {
        this.mapdustPlugin = mapdustPlugin;
    }

    /**
     * Returns the down-loaded flag
     *
     * @return the down-loaded
     */
    public boolean isDownloaded() {
        return downloaded;
    }

}
