/*
 *  JOSMng - a Java Open Street Map editor, the next generation.
 * 
 *  Copyright (C) 2008 Petr Nejedly <P.Nejedly@sh.cvut.cz>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.openstreetmap.josmng.view;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.AbstractAction;

/**
 * An abstracion of an editation mode.
 * 
 * @author nenik
 */
public abstract class EditMode extends AbstractAction implements MouseListener, MouseMotionListener {

    private MapView parent;
    
    public EditMode(String name, MapView view) {
        super(name);
        parent = view;
    }

    public final void actionPerformed(ActionEvent e) {
        parent.setEditMode(this);
    }

    public final void enter() {
        parent.addMouseListener(this);
        parent.addMouseMotionListener(this);
        entered();
    }
    
    public final void exit() {
        exited();
        parent.removeMouseMotionListener(this);
        parent.removeMouseListener(this);
    }

    protected abstract void exited();
    protected abstract void entered();

    protected MapView getMapView() {
        return parent;
    }
    
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
}
