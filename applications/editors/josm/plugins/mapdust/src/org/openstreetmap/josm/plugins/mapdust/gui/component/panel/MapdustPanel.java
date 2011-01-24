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
import org.openstreetmap.josm.plugins.mapdust.gui.component.renderer.BugListCellRenderer;
import org.openstreetmap.josm.plugins.mapdust.gui.component.util.ComponentUtil;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustBugDetailsObservable;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustBugDetailsObserver;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBug;


/**
 * Defines the components of the MapDust panel
 *
 * @author Bea
 * @version $Revision$
 */
public class MapdustPanel extends JPanel implements ListSelectionListener,
        MapdustBugDetailsObservable {

    /** The list of observers */
    private final ArrayList<MapdustBugDetailsObserver> observers =
            new ArrayList<MapdustBugDetailsObserver>();

    /** The serial version UID */
    private static final long serialVersionUID = 1L;

    /** The list of <code>MapdustBug</code> objects */
    private List<MapdustBug> mapdustBugsList;

    /** The list of bugs */
    private JList listBugs;

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
    public MapdustPanel() {}

    /**
     * Builds a <code>MapdustPlugin</code> object with the given parameters.
     *
     * @param mapdustBugsList The list of <code>MapdustBug</code> objects
     * @param name The name of the panel
     * @param mapdustPlugin The <code>MapdustPlugin</code> object
     */
    public MapdustPanel(List<MapdustBug> mapdustBugsList, String name,
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
        btnPanel = new MapdustButtonPanel(mapdustPlugin);
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
        JScrollPane cmpBugs;
        if (mapdustBugsList != null && mapdustBugsList.size() == 0) {
            String text = " No bugs in the current view!";
            JList l=new JList(new String[]{text});
            l.setBorder(new LineBorder(Color.black, 1, false));
            l.setCellRenderer(new BugListCellRenderer());
            cmpBugs=ComponentUtil.createJScrollPane(l);
            add(cmpBugs, BorderLayout.CENTER);
        } else {
            listBugs = ComponentUtil.createJList(mapdustBugsList, menu);
            listBugs.addListSelectionListener(this);
            DisplayMenu adapter = new DisplayMenu(listBugs, menu);
            listBugs.addMouseListener(adapter);
            cmpBugs = ComponentUtil.createJScrollPane(getListBugs());
            add(cmpBugs, BorderLayout.CENTER);
        }
        /* add button panel */
        add(btnPanel, BorderLayout.SOUTH);
    }

    @Override
    public void valueChanged(ListSelectionEvent event) {
        MapdustBug selectedBug = (MapdustBug) listBugs.getSelectedValue();
        Main.pref.put("selectedBug.status", selectedBug.getStatus().getValue());
        notifyObservers(selectedBug);
        if (selectedBug.getStatus().getKey().equals(1)) {
            /* status fixed */
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
        /* re-paint */
        Main.map.mapView.repaint();
        mapdustGUI.repaint();
    }

    /**
     * Returns the selected bug from the list of MapDust bugs.
     *
     * @return a <code>MapdustBug</code> object
     */
    public MapdustBug getSelectedBug() {
        MapdustBug selectedBug=null;
        if (getListBugs()!=null) {
            selectedBug = (MapdustBug) getListBugs().getSelectedValue();
        }
        return selectedBug;
    }

    /**
     * Returns the index of the selected MapDust bug.
     *
     * @return index
     */
    public int getSelectedIndex() {
        return getListBugs().getSelectedIndex();
    }

    /**
     * Sets the <code>MapdustBug</code> which will be selected from the list of
     * MapDust bug.
     *
     * @param mapdustBug The <code>MapdustBug</code> object
     */
    public void setSelectedBug(MapdustBug mapdustBug) {
        getListBugs().setSelectedValue(mapdustBug, false);
        int index = getSelectedIndex();
        getListBugs().ensureIndexIsVisible(index);
    }

    /**
     * Returns the list of bugs
     *
     * @return the listBugs
     */
    public JList getListBugs() {
        return listBugs;
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
