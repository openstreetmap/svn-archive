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
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.DataUtilities;
import org.geotools.data.shapefile.ShpFileType;
import org.geotools.data.shapefile.ShpFiles;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.dbf.DbaseFileReader.Row;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.SpreadSheetReader;

/**
 * MapInfo TAB reader
 *
 */
public class TabReader extends AbstractMapInfoReader {

	private Charset datCharset;
	private final AbstractDataSetHandler handler;
	
	public TabReader(AbstractDataSetHandler handler) {
		this.handler = handler;
	}

	public static DataSet parseDataSet(InputStream in, File file,
			AbstractDataSetHandler handler, ProgressMonitor instance) throws IOException {
		return new TabReader(handler).parse(in, file, instance, Charset.forName(ISO8859_15));
	}
	
	private class TabFiles extends ShpFiles {
		public TabFiles(File headerFile, File dataFile) throws IllegalArgumentException {
			super(DataUtilities.fileToURL(headerFile));
			urls.put(ShpFileType.DBF, DataUtilities.fileToURL(dataFile));
		}
		
		@Override
	    protected String baseName(Object obj) {
            if (obj instanceof URL) {
                return toBase(((URL) obj).toExternalForm());
            }
            return null;
	    }
	    
	    private String toBase(String path) {
	        return path.substring(0, path.toLowerCase().lastIndexOf(".tab"));
	    }
	}
	
	private class TabOsmReader extends SpreadSheetReader {

		private final DbaseFileReader dbfReader;
		public TabOsmReader(AbstractDataSetHandler handler, TabFiles tabFiles) throws IOException {
			super(handler);
			this.dbfReader = new DbaseFileReader(tabFiles, false, datCharset, null);
		}

		@Override
		protected void initResources(InputStream in,
				ProgressMonitor progressMonitor) throws IOException {
		}

		@Override
		protected String[] readLine(ProgressMonitor progressMonitor)
				throws IOException {
			if (!dbfReader.hasNext()) {
				return null;
			}
        	List<String> result = new ArrayList<String>();
			Row row = dbfReader.readRow();
        	for (int i=0; i<columns.size(); i++) {
        		Object o = row.read(i);
        		if (o != null) {
        			result.add(o.toString());
        		} else {
        			result.add("");
        		}
        	}
        	return result.toArray(new String[0]);
		}
	}

	private DataSet parse(InputStream in, File file, ProgressMonitor instance, Charset charset) throws IOException {
		headerReader = new BufferedReader(new InputStreamReader(in, charset));
		parseHeader();
        try {
        	File dataFile = getDataFile(file, ".dat");
        	ds.mergeFrom(new TabOsmReader(handler, new TabFiles(file, dataFile)).
        			doParse(columns.toArray(new String[0]), instance));
        } catch (IOException e) {
        	System.err.println(e.getMessage());
        }
		return ds;
	}

	@Override
	protected void parseHeaderLine(String[] words) throws IOException {
		if (words[0].equalsIgnoreCase("!table")) {
			// Do nothing
		} else if (words[0].equalsIgnoreCase("!version")) {
			parseVersion(words);
		} else if (words[0].equalsIgnoreCase("!charset")) {
			parseCharset(words);
		} else if (numcolumns > 0) {
			parseField(words);
		} else if (words[0].equalsIgnoreCase("Definition")) {
			// Do nothing
		} else if (words[0].equalsIgnoreCase("Type")) {
			parseType(words);
		} else if (words[0].equalsIgnoreCase("Fields")) {
			parseColumns(words);
		} else if (!line.isEmpty()) {
			System.err.println("Line "+lineNum+". Unknown clause in header: "+line);
		}
	}

	private void parseField(String[] words) {
		columns.add(words[0]);
		--numcolumns;
	}

	private void parseType(String[] words) {
		if (words[1].equalsIgnoreCase("NATIVE") && words[2].equalsIgnoreCase("Charset")) {
			datCharset = parseCharset(words, 3);
		} else {
			System.err.println("Line "+lineNum+". Unknown Type clause in header: "+line);
		}
	}
}
