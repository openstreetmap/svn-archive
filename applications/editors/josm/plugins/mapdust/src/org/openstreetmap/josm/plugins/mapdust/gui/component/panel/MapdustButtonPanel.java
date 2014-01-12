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


import java.awt.GridLayout;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.mapdust.MapdustPlugin;
import org.openstreetmap.josm.plugins.mapdust.gui.action.execute.ExecuteRefresh;
import org.openstreetmap.josm.plugins.mapdust.gui.action.execute.ExecuteWorkOffline;
import org.openstreetmap.josm.plugins.mapdust.gui.action.show.ShowCloseBugAction;
import org.openstreetmap.josm.plugins.mapdust.gui.action.show.ShowCommentBugAction;
import org.openstreetmap.josm.plugins.mapdust.gui.action.show.ShowFilterBugAction;
import org.openstreetmap.josm.plugins.mapdust.gui.action.show.ShowInvalidateBugAction;
import org.openstreetmap.josm.plugins.mapdust.gui.action.show.ShowReOpenBugAction;
import org.openstreetmap.josm.plugins.mapdust.gui.component.util.ComponentUtil;
import org.openstreetmap.josm.plugins.mapdust.gui.value.MapdustPluginState;


/**
 * Defines the panel of buttons for the MapDust bugs list.
 *
 * @author Bea
 * @version $Revision$
 */
public class MapdustButtonPanel extends JPanel {

    /** The serial version UID */
    private static final long serialVersionUID = -4234650664854226973L;

    /** The work off-line button */
    private JToggleButton btnWorkOffline;

    /** The filter button */
    private JToggleButton btnFilter;

    /** The add comment button */
    private JToggleButton btnAddComment;

    /** The fix bug report button */
    private JToggleButton btnFixBugReport;

    /** The invalidate bug report button */
    private JToggleButton btnInvalidateBugReport;

    /** The re-open bug report button */
    private JToggleButton btnReOpenBugReport;

    /** The refresh button */
    private JToggleButton btnRefresh;

    /**
     * Builds a <code>MapdustButtonPanel</code> object
     */
    public MapdustButtonPanel() {}

    /**
     * Builds a <code>MapdustButtonPanel</code> object based on the given
     * parameter
     *
     * @param mapdustPlugin The <code>MapdustPlugin</code> object
     */
    public MapdustButtonPanel(MapdustPlugin mapdustPlugin) {
        setLayout(new GridLayout(1, 7));
        addComponents(mapdustPlugin);
    }

    /**
     * Add the components to the button panel.
     *
     * @param mapdustPlugin The <code>MapdustPlugin</code> object
     */
    private void addComponents(MapdustPlugin mapdustPlugin) {
        /* 'Work off-line' button */
        String text = "";
        String imagePath = "";
        if (btnWorkOffline == null) {
            String pluginState = Main.pref.get("mapdust.pluginState");
            if (pluginState.equals(MapdustPluginState.ONLINE.getValue())) {
                text = "Work offline mode";
                imagePath = "dialogs/workoffline.png";
            } else {
                text = "Work online mode";
                imagePath = "dialogs/online.png";
            }
            AbstractAction action = new ExecuteWorkOffline(
                    mapdustPlugin.getMapdustGUI());
            ((ExecuteWorkOffline) action).addObserver(mapdustPlugin);
            btnWorkOffline = ComponentUtil.createJButton("Work offline", text,
                    imagePath, action);
            btnWorkOffline.setSelected(false);
            btnWorkOffline.setFocusTraversalKeysEnabled(false);
        }
        /* 'Filter' button */
        if (btnFilter == null) {
            text = "Filter bug reports";
            imagePath = "dialogs/mapdust_bug_filter.png";
            AbstractAction action = new ShowFilterBugAction(mapdustPlugin);
            btnFilter = ComponentUtil.createJButton(text, text, imagePath,
                    action);
            btnFilter.setEnabled(true);
            btnFilter.setFocusTraversalKeysEnabled(false);
        }
        /* 'Add Comment' button */
        if (btnAddComment == null) {
            text = "Add comment/additional info";
            imagePath = "dialogs/comment.png";
            AbstractAction action = new ShowCommentBugAction(mapdustPlugin);
            btnAddComment = ComponentUtil.createJButton(text, text, imagePath,
                    action);
            btnAddComment.setEnabled(false);
            btnAddComment.setFocusTraversalKeysEnabled(false);
        }
        /* 'Fix bug report' button */
        if (btnFixBugReport == null) {
            text = "Mark as fixed";
            imagePath = "dialogs/fixed.png";
            AbstractAction action = new ShowCloseBugAction(mapdustPlugin);
            btnFixBugReport = ComponentUtil.createJButton(text, text, imagePath,
                    action);
            btnFixBugReport.setEnabled(false);
            btnFixBugReport.setFocusTraversalKeysEnabled(false);
        }
        /* 'Invalidate bug report' button */
        if (btnInvalidateBugReport == null) {
            text = "Non-reproducible/Software bug";
            imagePath = "dialogs/invalid.png";
            AbstractAction action = new ShowInvalidateBugAction(mapdustPlugin);
            btnInvalidateBugReport = ComponentUtil.createJButton(text, text,
                    imagePath, action);
            btnInvalidateBugReport.setEnabled(false);
            btnInvalidateBugReport.setFocusTraversalKeysEnabled(false);
        }
        /* 'Re-open bug report' button */
        if (btnReOpenBugReport == null) {
            text = "Reopen bug";
            imagePath = "dialogs/reopen.png";
            AbstractAction action = new ShowReOpenBugAction(mapdustPlugin);
            btnReOpenBugReport = ComponentUtil.createJButton(text, text,
                    imagePath, action);
            btnReOpenBugReport.setEnabled(false);
            btnReOpenBugReport.setFocusTraversalKeysEnabled(false);
        }
        /* 'Refresh' button */
        if (btnRefresh == null) {
            text = "Refresh";
            imagePath = "dialogs/mapdust_refresh.png";
            String pluginState = Main.pref.get("mapdust.pluginState");
            AbstractAction action = new ExecuteRefresh();
            ((ExecuteRefresh) action).addObserver(mapdustPlugin);
            btnRefresh = ComponentUtil.createJButton(text, text, imagePath,
                    action);
            if (pluginState.equals(MapdustPluginState.OFFLINE.getValue())) {
                btnRefresh.setEnabled(false);
            }
            btnRefresh.setFocusTraversalKeysEnabled(false);
        }

        /* add components */
        add(btnWorkOffline);
        add(btnFilter);
        add(btnAddComment);
        add(btnFixBugReport);
        add(btnInvalidateBugReport);
        add(btnReOpenBugReport);
        add(btnRefresh);
    }

    /**
     * Disables the buttons from the <code>MapdustButtonPanel</code>.
     */
    public void disableComponents() {
        if (btnWorkOffline != null) {
            btnWorkOffline.setEnabled(false);
            btnWorkOffline.setSelected(false);
            btnWorkOffline.setFocusable(false);
        }
        if (btnFilter != null) {
            btnFilter.setEnabled(false);
            btnFilter.setEnabled(false);
            btnFilter.setEnabled(false);
        }
        if (btnRefresh != null) {
            btnRefresh.setEnabled(false);
            btnRefresh.setSelected(false);
            btnRefresh.setFocusable(false);
        }
        if (btnAddComment != null) {
            btnAddComment.setEnabled(false);
            btnAddComment.setSelected(false);
            btnAddComment.setFocusable(false);
        }
        if (btnFixBugReport != null) {
            btnFixBugReport.setEnabled(false);
            btnFixBugReport.setSelected(false);
            btnFixBugReport.setFocusable(false);
        }
        if (btnInvalidateBugReport != null) {
            btnInvalidateBugReport.setEnabled(false);
            btnInvalidateBugReport.setEnabled(false);
            btnInvalidateBugReport.setEnabled(false);
        }
        if (btnReOpenBugReport != null) {
            btnReOpenBugReport.setEnabled(false);
            btnReOpenBugReport.setEnabled(false);
            btnReOpenBugReport.setEnabled(false);
        }
    }

    /**
     * Enables the basic components from the <code>MapdustButtonPanel</code>.
     * Basic components are considered the following buttons: work offline,
     * filter bug report, and refresh.If the onlyBasic flag is true then the
     * other buttons will be disabled.
     *
     * @param onlyBasic If true then the not basic buttons will be enabled
     */
    public void enableBasicComponents(boolean onlyBasic) {
        btnWorkOffline.setEnabled(true);
        btnWorkOffline.setSelected(false);
        btnWorkOffline.setFocusable(false);
        btnFilter.setEnabled(true);
        btnFilter.setSelected(false);
        btnFilter.setFocusable(false);
        String pluginState = Main.pref.get("mapdust.pluginState");
        if (pluginState.equals(MapdustPluginState.ONLINE.getValue())) {
            btnRefresh.setEnabled(true);
        } else {
            btnRefresh.setEnabled(false);
        }

        btnRefresh.setSelected(false);
        btnRefresh.setFocusable(false);
        if (onlyBasic) {
            btnAddComment.setEnabled(false);
            btnAddComment.setSelected(false);
            btnAddComment.setFocusable(false);
            btnFixBugReport.setEnabled(false);
            btnFixBugReport.setSelected(false);
            btnFixBugReport.setFocusable(false);
            btnInvalidateBugReport.setEnabled(false);
            btnInvalidateBugReport.setEnabled(false);
            btnInvalidateBugReport.setEnabled(false);
            btnReOpenBugReport.setEnabled(false);
            btnReOpenBugReport.setEnabled(false);
            btnReOpenBugReport.setEnabled(false);
        }
    }

    /**
     * Returns the work off-line button
     *
     * @return the btnWorkOffline
     */
    public JToggleButton getBtnWorkOffline() {
        return btnWorkOffline;
    }

    /**
     * Returns the refresh button
     *
     * @return the btnRefresh
     */
    public JToggleButton getBtnRefresh() {
        return btnRefresh;
    }

    /**
     * @return the btnFilter
     */
    public JToggleButton getBtnFilter() {
        return btnFilter;
    }

    /**
     * Returns the add comment button
     *
     * @return the btnAddComment
     */
    public JToggleButton getBtnAddComment() {
        return btnAddComment;
    }

    /**
     * Returns the fix bug report button
     *
     * @return the btnFixBugReport
     */
    public JToggleButton getBtnFixBugReport() {
        return btnFixBugReport;
    }

    /**
     * Returns the invalidate button
     *
     * @return the btnInvalidateBugReport
     */
    public JToggleButton getBtnInvalidateBugReport() {
        return btnInvalidateBugReport;
    }

    /**
     * Returns the re-open bug report button
     *
     * @return the btnReOpenBugReport
     */
    public JToggleButton getBtnReOpenBugReport() {
        return btnReOpenBugReport;
    }

}
