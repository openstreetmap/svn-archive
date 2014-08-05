// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.shapefile.files.TabFiles;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.dbf.DbaseFileReader.Row;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.SpreadSheetHandler;
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
		return new TabReader(handler).parse(in, file, instance, Charset.forName(OdConstants.ISO8859_15));
	}
		
	private class TabOsmReader extends SpreadSheetReader {

		private final DbaseFileReader dbfReader;
		public TabOsmReader(SpreadSheetHandler handler, TabFiles tabFiles) throws IOException {
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
        	List<String> result = new ArrayList<>();
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
        	ds = new TabOsmReader(handler != null ? handler.getSpreadSheetHandler() : null, new TabFiles(file, dataFile)).
        			doParse(columns.toArray(new String[0]), instance);
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
