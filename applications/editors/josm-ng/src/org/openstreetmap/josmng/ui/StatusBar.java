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

package org.openstreetmap.josmng.ui;


import java.awt.Dimension;
import javax.swing.JLabel;

/** A global status bar implementation. 
 */
public class StatusBar extends JLabel {
    private static final StatusBar INSTANCE = new StatusBar();
    private StatusBar() {
        setText(" ");
    }
    
    public static StatusBar getDefault() {
        return INSTANCE;
    }

    public @Override Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        dim.width = Integer.MAX_VALUE;
        return dim;
    }    
}
