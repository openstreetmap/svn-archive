// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io;

import java.io.File;
import java.io.IOException;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.OsmImporter;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.datasets.DataSetUpdater;
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
			for (AbstractDataSetHandler dsh : module.getNewlyInstanciatedHandlers()) {
				if (dsh.acceptsFile(file)) {
					return dsh;
				}
			}
		}
    	return null;
    }

	@Override
	public void importData(File file, ProgressMonitor progressMonitor)
			throws IOException, IllegalDataException {
		if (file != null) {
			this.file = file;
			this.handler = findDataSetHandler(file);
		}
		super.importData(file, progressMonitor);
	}

	@Override
	protected OsmDataLayer createLayer(DataSet dataSet, File associatedFile, String layerName) {
		DataSetUpdater.updateDataSet(dataSet, handler, associatedFile);
		return new OdDataLayer(dataSet, layerName, associatedFile, handler);
	}
}
