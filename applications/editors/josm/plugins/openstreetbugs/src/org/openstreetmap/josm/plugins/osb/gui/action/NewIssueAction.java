/* Copyright (c) 2008, Henrik Niehaus
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
package org.openstreetmap.josm.plugins.osb.gui.action;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JToggleButton;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.gui.widgets.HistoryChangedListener;
import org.openstreetmap.josm.plugins.osb.ConfigKeys;
import org.openstreetmap.josm.plugins.osb.OsbPlugin;
import org.openstreetmap.josm.plugins.osb.api.NewAction;
import org.openstreetmap.josm.plugins.osb.gui.dialogs.TextInputDialog;

public class NewIssueAction extends OsbAction implements MouseListener {

    private static final long serialVersionUID = 1L;

    private NewAction newAction = new NewAction();

    private JToggleButton button;

    private OsbPlugin plugin;

    private Cursor previousCursor;

    public NewIssueAction(JToggleButton button, OsbPlugin plugin) {
        super(tr("New issue"));
        this.button = button;
        this.plugin = plugin;
    }

    @Override
    protected void doActionPerformed(ActionEvent e) throws IOException {
        if(button.isSelected()) {
            previousCursor = Main.map.mapView.getCursor();
            Main.map.mapView.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
            Main.map.mapView.addMouseListener(this);
        } else {
            reset();
        }
    }

    private void reset() {
        Main.map.mapView.setCursor(previousCursor);
        Main.map.mapView.removeMouseListener(this);
        button.setSelected(false);
    }

    public void mouseClicked(MouseEvent e) {
        addNewIssue(e);
    }

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {
        addNewIssue(e);
    }

    private void addNewIssue(MouseEvent e) {
        List<String> history = new LinkedList<String>(Main.pref.getCollection(ConfigKeys.OSB_NEW_HISTORY, new LinkedList<String>()));
        HistoryChangedListener l = new HistoryChangedListener() {
            public void historyChanged(List<String> history) {
                Main.pref.putCollection(ConfigKeys.OSB_NEW_HISTORY, history);
            }
        };
        String result = TextInputDialog.showDialog(
                Main.map,
                tr("Create issue"),
                tr("Describe the problem precisely"),
                OsbPlugin.loadIcon("icon_error_add22.png"),
                history, l);

        if(result != null && result.length() > 0) {
            try {
                result = addMesgInfo(result);
                Node n = newAction.execute(e.getPoint(), result);
                plugin.getDataSet().addPrimitive(n);
                if(Main.pref.getBoolean(ConfigKeys.OSB_API_DISABLED)) {
                    plugin.updateGui();
                } else {
                    plugin.updateData();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(Main.parent,
                        tr("An error occurred: {0}", new Object[] {result}),
                        tr("Error"),
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        reset();
    }

    public void mouseReleased(MouseEvent e) {}
}
