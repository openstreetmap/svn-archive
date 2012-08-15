//    JOSM tag2link plugin.
//    Copyright (C) 2011-2012 Don-vip & FrViPofm
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package org.openstreetmap.josm.plugins.tag2link.listeners;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.tag2link.action.OpenLinkAction;
import org.openstreetmap.josm.plugins.tag2link.action.OpenMailAction;
import org.openstreetmap.josm.plugins.tag2link.data.Link;

public abstract class AbstractPopupListener implements PopupMenuListener {
    
    protected final MapFrame frame;
    protected final List<JMenuItem> itemList;
    
    protected AbstractPopupListener(MapFrame frame) {
        this.frame = frame;
        this.itemList = new ArrayList<JMenuItem>();
    }
    
    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        JPopupMenu popup =  (JPopupMenu) e.getSource();
        for (JMenuItem item : itemList) {
            popup.remove(item);
        }
        itemList.clear();
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
    }
    
    protected void addLink(JPopupMenu popup, Link link) {
        JosmAction action = null;
        if (link.url.matches("mailto:.*")) {
            action = new OpenMailAction(link);
        } else {
            action = new OpenLinkAction(link);
        }

        itemList.add(popup.add(action));
    }
}
