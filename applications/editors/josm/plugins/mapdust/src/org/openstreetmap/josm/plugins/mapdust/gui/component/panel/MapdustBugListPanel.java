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
package org.openstreetmap.josm.plugins.mapdust.gui.component.panel;


import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.mapdust.MapdustPlugin;
import org.openstreetmap.josm.plugins.mapdust.gui.MapdustGUI;
import org.openstreetmap.josm.plugins.mapdust.gui.action.adapter.DisplayMenu;
import org.openstreetmap.josm.plugins.mapdust.gui.action.show.MapdustShowAction;
import org.openstreetmap.josm.plugins.mapdust.gui.action.show.ShowCloseBugAction;
import org.openstreetmap.josm.plugins.mapdust.gui.action.show.ShowCommentBugAction;
import org.openstreetmap.josm.plugins.mapdust.gui.action.show.ShowInvalidateBugAction;
import org.openstreetmap.josm.plugins.mapdust.gui.action.show.ShowReOpenBugAction;
import org.openstreetmap.josm.plugins.mapdust.gui.component.model.BugsListModel;
import org.openstreetmap.josm.plugins.mapdust.gui.component.renderer.BugListCellRenderer;
import org.openstreetmap.josm.plugins.mapdust.gui.component.util.ComponentUtil;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustBugDetailsObservable;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustBugDetailsObserver;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBug;


/**
 * Defines the components of the MapDust bug list panel.
 *
 * @author Bea
 * @version $Revision$
 */
public class MapdustBugListPanel extends JPanel implements ListSelectionListener,
        MapdustBugDetailsObservable {

    /** The serial version UID */
    private static final long serialVersionUID = -675120506597637085L;

    /** The list of observers */
    private final ArrayList<MapdustBugDetailsObserver> observers =
            new ArrayList<MapdustBugDetailsObserver>();

    /** The list of <code>MapdustBug</code> objects */
    private List<MapdustBug> mapdustBugsList;

    /** The list of bugs */
    private JList mapdustBugsJList;

    /** The scroll pane for the <code>MapdustBug</code>s */
    private JScrollPane cmpMapdustBugs;

    /** The button panel */
    private MapdustButtonPanel btnPanel;

    /** The <code>MapdustGUI</code> */
    private MapdustGUI mapdustGUI;

    /** The pop-up menu */
    private JPopupMenu menu;

    /** The add comment menu item */
    private JMenuItem menuAddComment;

    /** The fixed menu item */
    private JMenuItem menuFixed;

    /** The invalidate menu item */
    private JMenuItem menuInvalidate;

    /** The re-open menu item */
    private JMenuItem menuReopen;

    /**
     * Builds a <code>MapdustPlugin</code> object
     */
    public MapdustBugListPanel() {}

    /**
     * Builds a <code>MapdustPlugin</code> object with the given parameters.
     *
     * @param mapdustBugsList The list of <code>MapdustBug</code> objects
     * @param name The name of the panel
     * @param mapdustPlugin The <code>MapdustPlugin</code> object
     */
    public MapdustBugListPanel(List<MapdustBug> mapdustBugsList, String name,
            MapdustPlugin mapdustPlugin) {
        this.mapdustGUI = mapdustPlugin.getMapdustGUI();
        this.mapdustBugsList = mapdustBugsList;
        setLayout(new BorderLayout(5, 10));
        addComponents(mapdustPlugin);
        setName(name);
    }

    /**
     * Adds the components to the MapDust panel.
     *
     * @param mapdustPlugin The <code>MapdustPlugin</code> object
     */
    private void addComponents(MapdustPlugin mapdustPlugin) {
        /* create components */
        if (btnPanel == null) {
            btnPanel = new MapdustButtonPanel(mapdustPlugin);
        }
        if (menu == null) {
            menu = new JPopupMenu();
            /* add comment item */
            MapdustShowAction action = new ShowCommentBugAction(mapdustPlugin);
            menuAddComment = ComponentUtil.createJMenuItem(action, "Add comment",
                    "dialogs/comment.png");
            menu.add(menuAddComment);
            /* fix bug item */
            action = new ShowCloseBugAction(mapdustPlugin);
            menuFixed = ComponentUtil.createJMenuItem(action, "Close bug",
                    "dialogs/fixed.png");
            menu.add(menuFixed);
            /* invalidate bug item */
            action = new ShowInvalidateBugAction(mapdustPlugin);
            menuInvalidate = ComponentUtil.createJMenuItem(action,
                    "Invalidate bug", "dialogs/invalid.png");
            menu.add(menuInvalidate);
            /* re-open bug item */
            action = new ShowReOpenBugAction(mapdustPlugin);
            menuReopen = ComponentUtil.createJMenuItem(action, "Re-open bug",
                    "dialogs/reopen.png");
            menu.add(menuReopen);
        }
        /* create bugs list */
        if (mapdustBugsList == null || mapdustBugsList.isEmpty()) {
            String text = " No bugs in the current view for the selected";
            text += " filters!";
            JList textJList = new JList(new String[] { text });
            textJList.setBorder(new LineBorder(Color.black, 1, false));
            textJList.setCellRenderer(new BugListCellRenderer());
            cmpMapdustBugs = ComponentUtil.createJScrollPane(textJList);
            add(cmpMapdustBugs, BorderLayout.CENTER);
        } else {
            mapdustBugsJList = ComponentUtil.createJList(mapdustBugsList, menu);
            mapdustBugsJList.addListSelectionListener(this);
            DisplayMenu adapter = new DisplayMenu(mapdustBugsJList, menu);
            mapdustBugsJList.addMouseListener(adapter);
            cmpMapdustBugs = ComponentUtil.createJScrollPane(mapdustBugsJList);
            add(cmpMapdustBugs, BorderLayout.CENTER);
        }
        /* add button panel */
        add(btnPanel, BorderLayout.SOUTH);
    }

    /**
     * Updates the <code>MapdustPanel</code> with the new list of
     * <code>MapdustBug</code>s. If the list is null or empty an appropriate
     * message will be displayed on the list.
     *
     * @param mapdustBugsList The list of <code>MapdustBug</code>s
     */
    public void updateComponents(List<MapdustBug> mapdustBugsList) {
        this.mapdustBugsList = mapdustBugsList;
        if (mapdustBugsList == null || mapdustBugsList.isEmpty()) {
            String text = " No bugs in the current view for the selected";
            text += " filters!";
            JList textJList = new JList(new String[] { text });
            textJList.setBorder(new LineBorder(Color.black, 1, false));
            textJList.setCellRenderer(new BugListCellRenderer());
            cmpMapdustBugs.getViewport().setView(textJList);
        } else {
            if (mapdustBugsJList == null) {
                mapdustBugsJList = ComponentUtil.createJList(mapdustBugsList,
                        menu);
                mapdustBugsJList.addListSelectionListener(this);
                DisplayMenu adapter = new DisplayMenu(mapdustBugsJList, menu);
                mapdustBugsJList.addMouseListener(adapter);
            } else {
                mapdustBugsJList.setModel(new BugsListModel(mapdustBugsList));
            }
            cmpMapdustBugs.getViewport().setView(mapdustBugsJList);
        }
    }

    /**
     *
     */
    @Override
    public void valueChanged(ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()) {
            MapdustBug selectedBug = (MapdustBug) mapdustBugsJList.getSelectedValue();
            if (selectedBug != null) {
                Main.pref.put("selectedBug.status", selectedBug.getStatus()
                        .getValue());
                if (selectedBug.getStatus().getKey().equals(1)) {
                    /* status open */
                    btnPanel.getBtnAddComment().setEnabled(true);
                    btnPanel.getBtnReOpenBugReport().setEnabled(false);
                    btnPanel.getBtnInvalidateBugReport().setEnabled(true);
                    btnPanel.getBtnFixBugReport().setEnabled(true);
                    getMenuReopen().setEnabled(false);
                    getMenuInvalidate().setEnabled(true);
                    getMenuFixed().setEnabled(true);
                }
                if (selectedBug.getStatus().getKey().equals(2)) {
                    /* status fixed */
                    btnPanel.getBtnAddComment().setEnabled(true);
                    btnPanel.getBtnReOpenBugReport().setEnabled(true);
                    btnPanel.getBtnInvalidateBugReport().setEnabled(false);
                    btnPanel.getBtnFixBugReport().setEnabled(false);
                    getMenuReopen().setEnabled(true);
                    getMenuInvalidate().setEnabled(false);
                    getMenuFixed().setEnabled(false);
                }
                if (selectedBug.getStatus().getKey().equals(3)) {
                    /* status invalid */
                    btnPanel.getBtnAddComment().setEnabled(true);
                    btnPanel.getBtnReOpenBugReport().setEnabled(true);
                    btnPanel.getBtnInvalidateBugReport().setEnabled(false);
                    btnPanel.getBtnFixBugReport().setEnabled(false);
                    getMenuReopen().setEnabled(true);
                    getMenuInvalidate().setEnabled(false);
                    getMenuFixed().setEnabled(false);
                }

                btnPanel.getBtnAddComment().setSelected(false);
                btnPanel.getBtnReOpenBugReport().setSelected(false);
                btnPanel.getBtnFixBugReport().setSelected(false);
                btnPanel.getBtnInvalidateBugReport().setSelected(false);
                notifyObservers(selectedBug);
            } else {
                btnPanel.getBtnWorkOffline().setSelected(false);
                btnPanel.getBtnWorkOffline().setFocusable(false);
                btnPanel.getBtnRefresh().setSelected(false);
                btnPanel.getBtnRefresh().setFocusable(false);
                btnPanel.getBtnFilter().setSelected(false);
                btnPanel.getBtnFilter().setFocusable(false);
                btnPanel.getBtnAddComment().setEnabled(false);
                btnPanel.getBtnFixBugReport().setEnabled(false);
                btnPanel.getBtnInvalidateBugReport().setEnabled(false);
                btnPanel.getBtnReOpenBugReport().setEnabled(false);
            }
            /* re-paint */
            Main.map.mapView.repaint();
            mapdustGUI.repaint();
        }
    }

    /**
     * Returns the selected bug from the list of MapDust bugs.
     *
     * @return a <code>MapdustBug</code> object
     */
    public MapdustBug getSelectedBug() {
        MapdustBug selectedBug = null;
        if (getMapdustBugsJList() != null) {
            selectedBug = (MapdustBug) getMapdustBugsJList().getSelectedValue();
        }
        return selectedBug;
    }

    /**
     * Sets the <code>MapdustBug</code> which will be selected from the list of
     * MapDust bug.
     *
     * @param mapdustBug The <code>MapdustBug</code> object
     */
    public void setSelectedBug(MapdustBug mapdustBug) {
        mapdustBugsJList.setSelectedValue(mapdustBug, false);
        int index = mapdustBugsJList.getSelectedIndex();
        mapdustBugsJList.ensureIndexIsVisible(index);
    }

    /**
     * Selects the <code>MapdustBug</code> at the given index.
     *
     * @param index The index of the <code>MapdustBug</code>
     */
    public void resetSelectedBug(int index) {
        if (mapdustBugsJList != null) {
            mapdustBugsJList.setSelectedIndex(index);
        }
    }

    /**
     * Returns the selected <code>MapdustBug</code> object index.
     *
     * @return index
     */
    public int getSelectedBugIndex() {
        return mapdustBugsJList.getSelectedIndex();
    }

    /**
     * Disables the buttons from the <code>MapdustButtonPanel</code>.
     */
    public void disableBtnPanel() {
        btnPanel.disableComponents();
    }

    /**
     * Enables the basic components from the <code>MapdustButtonPanel</code>.
     * Basic components are considered the following buttons: work offline,
     * filter bug report, and refresh.If the onlyBasic flag is true then the
     * other buttons will be disabled.
     *
     * @param onlyBasic If true then the not basic buttons will be disabled
     */
    public void enableBtnPanel(boolean onlyBasic) {
        btnPanel.enableBasicComponents(onlyBasic);
    }

    /**
     * Returns the list of bugs
     *
     * @return the listBugs
     */
    public JList getMapdustBugsJList() {
        return mapdustBugsJList;
    }

    /**
     * Return the menu
     *
     * @return the menu
     */
    public JPopupMenu getMenu() {
        return menu;
    }

    /**
     * Returns the button panel
     *
     * @return the btnPanel
     */
    public MapdustButtonPanel getBtnPanel() {
        return btnPanel;
    }

    /**
     * Returns the 'add comment' menu item
     *
     * @return the menuAddComment
     */
    public JMenuItem getMenuAddComment() {
        return menuAddComment;
    }

    /**
     * Returns the 'fixed' menu item
     *
     * @return the menuFixed
     */
    public JMenuItem getMenuFixed() {
        return menuFixed;
    }

    /**
     * Returns the 'invalidate' menu item
     *
     * @return the menuInvalidate
     */
    public JMenuItem getMenuInvalidate() {
        return menuInvalidate;
    }

    /**
     * Returns the 're-open' menu item
     *
     * @return the menuReopen
     */
    public JMenuItem getMenuReopen() {
        return menuReopen;
    }

    /**
     * Returns the list of <code>MapdustBug</code> objects
     *
     * @return the mapdustBugsList
     */
    public List<MapdustBug> getMapdustBugsList() {
        return mapdustBugsList;
    }

    /**
     * Adds a new MapDust bug details observer to the list of observers.
     *
     * @param observer The <code>MapdustBugDetailsObserver</code> object
     */
    @Override
    public void addObserver(MapdustBugDetailsObserver observer) {
        if (!this.observers.contains(observer)) {
            this.observers.add(observer);
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
        this.observers.remove(observer);

    }

    /**
     * Notifies the MapDust bug details observers observing the given
     * <code>MapdustBug</code> object.
     *
     * @param mapdustBug The <code>MapdustBug</code> object
     */
    @Override
    public void notifyObservers(MapdustBug mapdustBug) {
        Iterator<MapdustBugDetailsObserver> elements =
                this.observers.iterator();
        while (elements.hasNext()) {
            (elements.next()).showDetails(mapdustBug);
        }
    }

}
