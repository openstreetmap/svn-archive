// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.tabular;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.util.OdUtils;

public class CsvReader extends SpreadSheetReader {

	private final Charset charset;
	private String sep;
	
	private BufferedReader reader;
	private String line;

	public CsvReader(CsvHandler handler) {
		this(handler, ";");
	}

	public CsvReader(CsvHandler handler, String defaultSep) {
		super(handler);
		this.charset = handler != null && handler.getCharset() != null ? handler.getCharset() : Charset.forName(OdConstants.UTF8);
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
			if (csvHandler == null || csvHandler.getSeparator() == null || ";".equals(csvHandler.getSeparator())) {
				// If default sep has been used, try comma
				System.out.println(e.getMessage());
				csvReader.sep = ",";
				return csvReader.doParse(csvReader.splitLine(), instance);
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
			return OdUtils.stripQuotesAndExtraChars(line.split(sep), sep);
		} else {
			return null;
		}
	}
}
