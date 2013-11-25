//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
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
package org.openstreetmap.josm.plugins.opendata.core.datasets;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.SimplifyWayAction;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;

public abstract class DataSetUpdater implements OdConstants {

	public static final void updateDataSet(DataSet dataSet, AbstractDataSetHandler handler, File associatedFile) {
		if (dataSet != null && handler != null) {
			if (associatedFile != null) {
				handler.setAssociatedFile(associatedFile);
				long lastmodified = associatedFile.lastModified();
				if (lastmodified > 0) {
					handler.setSourceDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date(lastmodified)));
				}
			}
			if (!Main.pref.getBoolean(PREF_RAWDATA)) {
				handler.updateDataSet(dataSet);
			}
			handler.checkDataSetSource(dataSet);
			handler.checkNames(dataSet);
			// Replace multipolygons with single untagged member by their sole member
			for (Relation r : dataSet.getRelations()) {
			    if (r.isMultipolygon() && r.getMembersCount() == 1) {
			        OsmPrimitive outer = r.getMember(0).getMember();
			        if (!outer.isTagged()) {
			            r.remove("type");
			            r.removeMember(0);
                        outer.setKeys(r.getKeys());
			            dataSet.removePrimitive(r);
			        }
			    }
			}
			// Simplify ways geometries
			for (Way w : dataSet.getWays()) {
			    SequenceCommand command = Main.main.menu.simplifyWay.simplifyWay(w, 0.25);
			    if (command != null) {
			        Main.main.undoRedo.addNoRedraw(command);
			    }
			}
		}
	}
}
