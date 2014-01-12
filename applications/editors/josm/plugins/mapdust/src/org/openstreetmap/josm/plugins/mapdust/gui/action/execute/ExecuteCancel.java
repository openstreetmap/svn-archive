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
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.mapdust.gui.MapdustGUI;
import org.openstreetmap.josm.plugins.mapdust.gui.component.dialog.AbstractDialog;
import org.openstreetmap.josm.plugins.mapdust.gui.component.panel.MapdustButtonPanel;
import org.openstreetmap.josm.plugins.mapdust.gui.value.MapdustPluginState;
import org.openstreetmap.josm.plugins.mapdust.service.value.Status;


/**
 * This class is invoked whenever a "Cancel" button is pressed.
 *
 * @author Bea
 *
 */
public class ExecuteCancel extends MapdustExecuteAction {

    /** The serial version UID */
    private static final long serialVersionUID = 5357125707042485489L;

    /**
     * Builds a <code>CancelAction</code> object
     */
    public ExecuteCancel() {}

    /**
     * Builds a <code>CancelAction</code> object
     *
     * @param dialog The <code>AbstractDialog</code> object
     * @param mapdustGUI The <code>MapdustGUI</code> object
     */
    public ExecuteCancel(AbstractDialog dialog, MapdustGUI mapdustGUI) {
        setDialog(dialog);
        setMapdustGUI(mapdustGUI);
    }

    /**
     * Cancels the executed action, and closes the visible dialog window.
     *
     * @param event The action event which fires this action
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (event != null) {
            String pluginState = Main.pref.get("mapdust.pluginState");
            String status = Main.pref.get("selectedBug.status");
            /* enable buttons */
            MapdustButtonPanel btnPanel =
                    getMapdustGUI().getPanel().getBtnPanel();
            if (btnPanel != null) {
                btnPanel.getBtnWorkOffline().setEnabled(true);
                btnPanel.getBtnWorkOffline().setFocusable(false);
                btnPanel.getBtnWorkOffline().setSelected(false);
                if (pluginState.equals(MapdustPluginState.OFFLINE.getValue())) {
                    btnPanel.getBtnRefresh().setEnabled(false);
                } else {
                    btnPanel.getBtnRefresh().setEnabled(true);
                }
                if (status.equals(Status.OPEN.getValue())) {
                    btnPanel.getBtnFixBugReport().setEnabled(true);
                    btnPanel.getBtnInvalidateBugReport().setEnabled(true);
                    btnPanel.getBtnReOpenBugReport().setEnabled(false);
                    btnPanel.getBtnAddComment().setEnabled(true);
                } else {
                    if (status.equals(Status.FIXED.getValue())) {
                        btnPanel.getBtnFixBugReport().setEnabled(false);
                        btnPanel.getBtnInvalidateBugReport().setEnabled(false);
                        btnPanel.getBtnReOpenBugReport().setEnabled(true);
                        btnPanel.getBtnAddComment().setEnabled(true);
                    } else {
                        if (status.equals(Status.INVALID.getValue())) {
                            btnPanel.getBtnFixBugReport().setEnabled(false);
                            btnPanel.getBtnInvalidateBugReport().setEnabled(
                                    false);
                            btnPanel.getBtnReOpenBugReport().setEnabled(true);
                            btnPanel.getBtnAddComment().setEnabled(true);
                        } else {
                            if (!status.isEmpty()) {
                                btnPanel.getBtnFixBugReport().setEnabled(false);
                                btnPanel.getBtnInvalidateBugReport()
                                        .setEnabled(false);
                                btnPanel.getBtnReOpenBugReport().setEnabled(
                                        false);
                                btnPanel.getBtnAddComment().setEnabled(false);
                            }
                        }
                    }
                }
                btnPanel.getBtnFilter().setEnabled(true);
                btnPanel.getBtnFilter().setSelected(false);
                btnPanel.getBtnFilter().setFocusable(false);
                btnPanel.getBtnAddComment().setSelected(false);
                btnPanel.getBtnAddComment().setFocusable(false);
                btnPanel.getBtnFixBugReport().setSelected(false);
                btnPanel.getBtnFixBugReport().setFocusable(false);
                btnPanel.getBtnInvalidateBugReport().setSelected(false);
                btnPanel.getBtnInvalidateBugReport().setFocusable(false);
                btnPanel.getBtnReOpenBugReport().setSelected(false);
                btnPanel.getBtnReOpenBugReport().setFocusable(false);
            }
        }

        /* dispose dialog */
        getDialog().dispose();
    }

}
