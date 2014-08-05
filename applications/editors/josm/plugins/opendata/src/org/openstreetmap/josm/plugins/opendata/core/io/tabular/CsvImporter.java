// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.tabular;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.io.AbstractImporter;

public class CsvImporter extends AbstractImporter {
    
    public static final ExtensionFileFilter CSV_FILE_FILTER = new ExtensionFileFilter(
            OdConstants.CSV_EXT, OdConstants.CSV_EXT, tr("CSV files") + " (*."+OdConstants.CSV_EXT+")");
    
    public static final String COLOMBUS_HEADER = "INDEX,TAG,DATE,TIME,LATITUDE N/S,LONGITUDE E/W,HEIGHT,SPEED,HEADING,FIX MODE,VALID,PDOP,HDOP,VDOP,VOX";
	
    public CsvImporter() {
        super(CSV_FILE_FILTER);
    }

	@Override
	protected DataSet parseDataSet(InputStream in, ProgressMonitor instance)
			throws IllegalDataException {
		try {
			return CsvReader.parseDataSet(in, handler, instance);
		} catch (IOException e) {
			throw new IllegalDataException(e);
		}
	}

    @Override
    public boolean acceptFile(File pathname) {
        return super.acceptFile(pathname) && !isColombusCsv(pathname);
    }

    public static boolean isColombusCsv(File file) {
        boolean result = false;
        if (file != null && file.isFile()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                try {
                    String line = reader.readLine();
                    result = line != null && line.equalsIgnoreCase(COLOMBUS_HEADER);
                } finally {
                    reader.close();
                }
            } catch (IOException e) {
                // Ignore exceptions
            }
        }
        return result;
    }
}
