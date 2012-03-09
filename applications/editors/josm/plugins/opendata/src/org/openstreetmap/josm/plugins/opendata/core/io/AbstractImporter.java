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
package org.openstreetmap.josm.plugins.opendata.core.io;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.OsmImporter;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.layers.OdDataLayer;
import org.openstreetmap.josm.plugins.opendata.core.modules.Module;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleHandler;

public abstract class AbstractImporter extends OsmImporter implements OdConstants {
	
	protected AbstractDataSetHandler handler;
	
	protected File file;
	
    public AbstractImporter(ExtensionFileFilter filter) {
        super(filter);
    }
    
    protected final AbstractDataSetHandler findDataSetHandler(File file) {
    	for (Module module : ModuleHandler.moduleList) {
			for (AbstractDataSetHandler dsh : module.getHandlers()) {
				if (dsh.acceptsFile(file)) {
					return dsh;
				}
			}
		}
    	return null;
    }

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.io.OsmImporter#importData(java.io.File, org.openstreetmap.josm.gui.progress.ProgressMonitor)
	 */
	@Override
	public void importData(File file, ProgressMonitor progressMonitor)
			throws IOException, IllegalDataException {
		if (file != null) {
			this.file = file;
			this.handler = findDataSetHandler(file);
		}
		super.importData(file, progressMonitor);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.io.OsmImporter#createLayer(org.openstreetmap.josm.data.osm.DataSet, java.io.File, java.lang.String)
	 */
	@Override
	protected OsmDataLayer createLayer(DataSet dataSet, File associatedFile, String layerName) {
		if (handler != null) {
			handler.setAssociatedFile(associatedFile);
			handler.setSourceDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date(associatedFile.lastModified())));
			if (!Main.pref.getBoolean(PREF_RAWDATA)) {
				handler.updateDataSet(dataSet);
			}
			handler.checkDataSetSource(dataSet);
			handler.checkNames(dataSet);
		}
		return new OdDataLayer(dataSet, layerName, associatedFile, handler);
	}
}
