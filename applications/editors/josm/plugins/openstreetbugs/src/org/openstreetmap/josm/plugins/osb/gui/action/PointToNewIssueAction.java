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

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.JToggleButton;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.osb.OsbPlugin;

public class PointToNewIssueAction extends AbstractAction implements MouseListener {

    private static final long serialVersionUID = 1L;

    private JToggleButton button;
    
    private OsbPlugin plugin;

    private Cursor previousCursor;
    
    public PointToNewIssueAction(JToggleButton button, OsbPlugin plugin) {
        this.button = button;
        this.plugin = plugin;
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
        NewIssueAction nia = new NewIssueAction(plugin, e.getPoint());
        nia.actionPerformed(new ActionEvent(this, 0, ""));
        reset();
    }

    public void mouseReleased(MouseEvent e) {}

    public void actionPerformed(ActionEvent e) {
        if(button.isSelected()) {
            previousCursor = Main.map.mapView.getCursor();
            Main.map.mapView.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
            Main.map.mapView.addMouseListener(this);
        } else {
            reset();
        }
    }
}
