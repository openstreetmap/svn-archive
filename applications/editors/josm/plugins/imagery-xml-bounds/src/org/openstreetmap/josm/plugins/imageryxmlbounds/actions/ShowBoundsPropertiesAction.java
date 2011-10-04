//    JOSM Imagery XML Bounds plugin.
//    Copyright (C) 2011 Don-vip
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
package org.openstreetmap.josm.plugins.imageryxmlbounds.actions;

import java.util.Collections;

import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.gui.dialogs.properties.PropertiesDialog.RelationRelated;

public class ShowBoundsPropertiesAction extends ShowBoundsAction implements RelationRelated {

    protected Relation relation;
    
    @Override
    public Relation getRelation() {
        return relation;
    }

    @Override
    public void setRelation(Relation relation) {
        this.relation = relation;
        updateOsmPrimitives(Collections.singleton(relation));
    }
}
