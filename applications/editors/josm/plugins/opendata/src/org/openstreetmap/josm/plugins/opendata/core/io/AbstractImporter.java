// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.OsmImporter;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.datasets.DataSetUpdater;
import org.openstreetmap.josm.plugins.opendata.core.layers.OdDataLayer;
import org.openstreetmap.josm.plugins.opendata.core.modules.Module;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleHandler;

public abstract class AbstractImporter extends OsmImporter {
	
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
		// Do not call super.importData because Compression.getUncompressedFileInputStream skips the first entry
        try (InputStream in = new FileInputStream(file)) {
            importData(in, file, progressMonitor);
        } catch (FileNotFoundException e) {
            Main.error(e);
            throw new IOException(tr("File ''{0}'' does not exist.", file.getName()), e);
        }
	}

	@Override
	protected OsmDataLayer createLayer(DataSet dataSet, File associatedFile, String layerName) {
		DataSetUpdater.updateDataSet(dataSet, handler, associatedFile);
		return new OdDataLayer(dataSet, layerName, associatedFile, handler);
	}
}
