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
import org.openstreetmap.josm.plugins.mapdust.gui.action.show.ShowInvalidateBugAction;
import org.openstreetmap.josm.plugins.mapdust.gui.action.show.ShowReOpenBugAction;
import org.openstreetmap.josm.plugins.mapdust.gui.component.util.ComponentUtil;
import org.openstreetmap.josm.plugins.mapdust.gui.value.MapdustPluginState;


/**
 * Defines the panel of the Mapdust bug list action buttons.
 *
 * @author Bea
 * @version $Revision$
 */
public class MapdustButtonPanel extends JPanel {

    /** The serial version UID */
    private static final long serialVersionUID = 1L;

    /** The work offline button */
    private JToggleButton btnWorkOffline;

    /** The refresh button */
    private JToggleButton btnRefresh;

    /** The add comment button */
    private JToggleButton btnAddComment;

    /** The fix bug report button */
    private JToggleButton btnFixBugReport;

    /** The invalidate bug report button */
    private JToggleButton btnInvalidateBugReport;

    /** The re-open bug report button */
    private JToggleButton btnReOpenBugReport;

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
     * Add the components to the button panel
     *
     * @param mapdustPlugin The <code>MapdustPlugin</code> object
     */
    private void addComponents(MapdustPlugin mapdustPlugin) {
        /* create components */
        /* 'Work offline' button */
        if (btnWorkOffline == null) {
            String pluginState = Main.pref.get("mapdust.pluginState");
            String tooltipText = "";
            String imagePath = "";
            if (pluginState.equals(MapdustPluginState.ONLINE.getValue())) {
                tooltipText = "Work offline mode";
                imagePath = "dialogs/workoffline.png";
            } else {
                tooltipText = "Work online mode";
                imagePath = "dialogs/online.png";
            }
            AbstractAction action = new ExecuteWorkOffline(mapdustPlugin.getMapdustGUI());
            ((ExecuteWorkOffline) action).addObserver(mapdustPlugin);
            btnWorkOffline = ComponentUtil.createJButton("Work offline",
                    tooltipText, imagePath, action);
        }
        /* 'Refresh' button */
        if (btnRefresh == null) {
            String pluginState = Main.pref.get("mapdust.pluginState");
            AbstractAction action = new ExecuteRefresh();
            ((ExecuteRefresh) action).addObserver(mapdustPlugin);
            btnRefresh = ComponentUtil.createJButton("Refresh", "Refresh",
                    "dialogs/refresh.png", action);
            if (pluginState.equals(MapdustPluginState.OFFLINE.getValue())) {
                btnRefresh.setEnabled(false);
            }
        }
        /* 'Add Comment' button */
        if (btnAddComment == null) {
            AbstractAction action = new ShowCommentBugAction(mapdustPlugin);
            btnAddComment = ComponentUtil.createJButton("Comment bug report",
                    "Comment bug report", "dialogs/comment.png", action);
            btnAddComment.setEnabled(false);
        }
        /* 'Fix bug report' button */
        if (btnFixBugReport == null) {
            AbstractAction action = new ShowCloseBugAction(mapdustPlugin);
            btnFixBugReport = ComponentUtil.createJButton("Close bug report",
                    "Close bug report", "dialogs/fixed.png", action);
            btnFixBugReport.setEnabled(false);
        }
        /* 'Invalidate bug report' button */
        if (btnInvalidateBugReport == null) {
            AbstractAction action = new ShowInvalidateBugAction(mapdustPlugin);
            btnInvalidateBugReport = ComponentUtil.createJButton("Invalidate bug report",
                    "Invalidate bug report", "dialogs/invalid.png", action);
            btnInvalidateBugReport.setEnabled(false);
        }
        /* 'Re-open bug report' button */
        if (btnReOpenBugReport == null) {
            AbstractAction action = new ShowReOpenBugAction(mapdustPlugin);
            btnReOpenBugReport = ComponentUtil.createJButton("Re-open bug report",
                    "Re-open bug report", "dialogs/reopen.png", action);
            btnReOpenBugReport.setEnabled(false);
        }

        /* add components */
        add(btnWorkOffline);
        add(btnRefresh);
        add(btnAddComment);
        add(btnFixBugReport);
        add(btnInvalidateBugReport);
        add(btnReOpenBugReport);
    }

    /**
     * Returns the work offline button
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
