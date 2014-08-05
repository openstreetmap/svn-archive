// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.datasets;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;

public abstract class DataSetUpdater {

	public static final void updateDataSet(DataSet dataSet, AbstractDataSetHandler handler, File associatedFile) {
		if (dataSet != null && handler != null) {
			if (associatedFile != null) {
				handler.setAssociatedFile(associatedFile);
				long lastmodified = associatedFile.lastModified();
				if (lastmodified > 0) {
					handler.setSourceDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date(lastmodified)));
				}
			}
			if (!Main.pref.getBoolean(OdConstants.PREF_RAWDATA)) {
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
