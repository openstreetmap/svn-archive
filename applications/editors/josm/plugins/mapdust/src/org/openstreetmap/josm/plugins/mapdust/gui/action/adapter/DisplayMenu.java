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


import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JList;
import javax.swing.JPopupMenu;


/**
 * Displays the pop-up menu for the given selected element from the list of
 * bugs.
 *
 * @author Bea
 * @version $Revision$
 */
public class DisplayMenu extends MouseAdapter {

    /** The <code>JPopupMenu</code> */
    private JPopupMenu menu;

    /** The list of bugs */
    private JList listBugs;

    /**
     * Builds a <code>DisplayMenu</code> object
     */
    public DisplayMenu() {}

    /**
     * Builds a <code>DisplayMenu</code> object with the given arguments.
     *
     * @param listBugs The <code>JList</code> of <code>MapdustBug</code> objects
     * @param menu The <code>JPopupMenu</code> object
     */
    public DisplayMenu(JList listBugs, JPopupMenu menu) {
        this.listBugs = listBugs;
        this.menu = menu;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        /* show the pop-up menu */
        showMenu(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        /* show the pop-up menu */
        showMenu(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        /* show the pop-up menu */
        showMenu(e);
    }

    /**
     * Shows the menu if the given <code>MouseEvent</code> object has trigger
     * the popup action.
     *
     * @param event The <code>MouseEvent</code> object
     */
    private void showMenu(MouseEvent event) {
        if (event.isPopupTrigger()) {
            int x = event.getX();
            int y = event.getY();
            int locationToIndex = listBugs.locationToIndex(event.getPoint());
            int selectedIndex = listBugs.getSelectedIndex();
            if (!listBugs.isSelectionEmpty()
                    && locationToIndex == selectedIndex) {
                menu.show(listBugs, x, y);

            }
        }
    }

}
