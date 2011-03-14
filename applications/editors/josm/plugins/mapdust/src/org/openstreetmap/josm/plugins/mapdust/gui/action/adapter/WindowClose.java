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
package org.openstreetmap.josm.plugins.mapdust.gui.action.adapter;


import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.mapdust.gui.component.dialog.AbstractDialog;
import org.openstreetmap.josm.plugins.mapdust.gui.component.panel.MapdustButtonPanel;
import org.openstreetmap.josm.plugins.mapdust.gui.value.MapdustPluginState;
import org.openstreetmap.josm.plugins.mapdust.service.value.Status;


/**
 * Closes the given dialog window.
 *
 * @author Bea
 * @version $Revision$
 */
public class WindowClose extends WindowAdapter {

    /** Serial version UID */

    /** A <code>AbstractDialog</code> object */
    private AbstractDialog dialog;

    /** The <code>MapdustButtonPanel</code> object */
    private MapdustButtonPanel btnPanel;

    /**
     * Builds a <code>WindowClose</code> object
     */
    public WindowClose() {}

    /**
     * Builds a <code>WindowClose</code> object based on the given arguments
     *
     * @param dialog The dialog which will be closed
     * @param btnPanel The button panel
     */
    public WindowClose(AbstractDialog dialog, MapdustButtonPanel btnPanel ) {
        this.dialog = dialog;
        this.btnPanel = btnPanel;
    }

    @Override
    public void windowClosing(WindowEvent event) {
        String status = Main.pref.get("selectedBug.status");
        String pluginState = Main.pref.get("mapdust.pluginState");
        /* enable buttons */
        if (btnPanel != null) {
            btnPanel.getBtnWorkOffline().setEnabled(true);
            btnPanel.getBtnWorkOffline().setSelected(false);
            btnPanel.getBtnWorkOffline().setFocusable(false);
            btnPanel.getBtnFilter().setEnabled(true);
            btnPanel.getBtnFilter().setSelected(false);
            btnPanel.getBtnFilter().setFocusable(false);
            if (pluginState.equals(MapdustPluginState.OFFLINE.getValue())) {
                btnPanel.getBtnRefresh().setEnabled(false);
            } else {
                btnPanel.getBtnRefresh().setEnabled(true);
                btnPanel.getBtnRefresh().setSelected(false);
                btnPanel.getBtnRefresh().setFocusable(false);
            }
            if (status.equals(Status.OPEN.getValue())) {
                btnPanel.getBtnFixBugReport().setEnabled(true);
                btnPanel.getBtnInvalidateBugReport().setEnabled(true);
                btnPanel.getBtnAddComment().setEnabled(true);
            } else {
                if (status.equals(Status.FIXED.getValue())) {
                    btnPanel.getBtnReOpenBugReport().setEnabled(true);
                    btnPanel.getBtnAddComment().setEnabled(true);
                } else {
                    if (status.equals(Status.INVALID.getValue())) {
                        btnPanel.getBtnReOpenBugReport().setEnabled(true);
                        btnPanel.getBtnAddComment().setEnabled(true);
                    }
                }
            }
        }

        btnPanel.getBtnAddComment().setSelected(false);
        btnPanel.getBtnAddComment().setFocusable(false);
        btnPanel.getBtnFixBugReport().setSelected(false);
        btnPanel.getBtnFixBugReport().setFocusable(false);
        btnPanel.getBtnInvalidateBugReport().setSelected(false);
        btnPanel.getBtnInvalidateBugReport().setFocusable(false);
        btnPanel.getBtnReOpenBugReport().setSelected(false);
        btnPanel.getBtnReOpenBugReport().setFocusable(false);

        /* dispose dialog */
        dialog.dispose();
    }

}
