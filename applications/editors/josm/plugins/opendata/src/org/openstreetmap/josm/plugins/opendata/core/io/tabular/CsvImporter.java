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
package org.openstreetmap.josm.plugins.opendata.core.io.tabular;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.plugins.opendata.core.io.AbstractImporter;

public class CsvImporter extends AbstractImporter {
    
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
