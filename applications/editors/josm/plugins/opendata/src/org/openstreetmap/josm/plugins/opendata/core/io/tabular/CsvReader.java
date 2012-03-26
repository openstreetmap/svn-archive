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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.util.OdUtils;

public class CsvReader extends SpreadSheetReader {

	private final Charset charset;
	private final String sep;
	
	private BufferedReader reader;
	private String line;

	public CsvReader(CsvHandler handler) {
		this(handler, ";");
	}

	public CsvReader(CsvHandler handler, String defaultSep) {
		super(handler);
		this.charset = handler != null && handler.getCharset() != null ? handler.getCharset() : Charset.forName(UTF8);
		this.sep = handler != null && handler.getSeparator() != null ? handler.getSeparator() : defaultSep;
	}
	
	public static DataSet parseDataSet(InputStream in, AbstractDataSetHandler handler, ProgressMonitor instance) throws IOException {
		CsvHandler csvHandler = null;
		if (handler != null && handler.getSpreadSheetHandler() instanceof CsvHandler) {
			csvHandler = (CsvHandler) handler.getSpreadSheetHandler();
		}
		CsvReader csvReader = new CsvReader(csvHandler);
		try {
			return csvReader.parse(in, instance);
		} catch (IllegalArgumentException e) {
			if (csvHandler == null || (csvHandler.getSeparator() != null && csvHandler.getSeparator().equals(";"))) {
				// If default sep has been used, try comma
				System.out.println(e.getMessage());
				CsvReader newReader = new CsvReader(csvHandler, ",");
				newReader.initResources(in, instance);
				newReader.line = csvReader.line;
				return newReader.doParse(newReader.splitLine(), instance);
			} else {
				throw e;
			}
		}
	}

	@Override
	protected void initResources(InputStream in, ProgressMonitor progressMonitor) throws IOException {
		System.out.println("Parsing CSV file using charset "+charset+" and separator '"+sep+"'");

		reader = new BufferedReader(new InputStreamReader(in, charset));
	}

	@Override
	protected String[] readLine(ProgressMonitor progressMonitor) throws IOException {
		line = reader.readLine();
		return splitLine();
	}
	
	private final String[] splitLine() {
		if (line != null) {
			return OdUtils.stripQuotes(line.split(sep), sep);
		} else {
			return null;
		}
	}
}
