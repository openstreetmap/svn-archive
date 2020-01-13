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

import java.util.Optional;

import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;

import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tags;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.tag2link.Tag2LinkRuleChecker;
import org.openstreetmap.josm.plugins.tag2link.data.Link;

public class PropertyPopupListener extends AbstractPopupListener {

    public PropertyPopupListener(MapFrame frame) {
        super(frame);
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        Tags tags = frame.propertiesDialog.getSelectedProperties();
        if (tags != null) {
            JPopupMenu popup = (JPopupMenu) e.getSource();
            Optional<BBox> bbox = MainApplication.getLayerManager().getEditDataSet().getSelected()
                .stream().filter(x -> x.hasTag(tags.getKey(), tags.getValues())).map(OsmPrimitive::getBBox).findAny();
            for (Link link : Tag2LinkRuleChecker.getLinks(tags, bbox.isPresent() ? bbox.get().getCenter() : null)) {
                addLink(popup, link);
            }
        }
    }
}
