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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jopendocument.model.OpenDocument;
import org.jopendocument.model.office.OfficeSpreadsheet;
import org.jopendocument.model.table.TableTable;
import org.jopendocument.model.table.TableTableRow;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;

public class OdsReader extends SpreadSheetReader {

	private OpenDocument doc;
	private TableTable sheet;
	private List<TableTableRow> rows;
	private int rowIndex;
	
	private static final String SEP = "TextP:\\[";
	
	public OdsReader(SpreadSheetHandler handler) {
		super(handler);
	}

	public static DataSet parseDataSet(InputStream in,
			AbstractDataSetHandler handler, ProgressMonitor instance) throws IOException {
		return new OdsReader(handler != null ? handler.getSpreadSheetHandler() : null).parse(in, instance);
	}

	@Override
	protected void initResources(InputStream in, ProgressMonitor progressMonitor) throws IOException {
		try {
			System.out.println("Parsing ODS file");
			doc = new OdsDocument(in);
			List<OfficeSpreadsheet> spreadsheets = doc.getBody().getOfficeSpreadsheets();
			if (spreadsheets != null && spreadsheets.size() > 0) {
				List<TableTable> tables = spreadsheets.get(0).getTables();
				if (tables != null && tables.size() > 0) {
					sheet = tables.get(getSheetNumber());
					if (sheet != null) {
						rows = sheet.getRows();
					}
				}
			}
			rowIndex = 0;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	protected String[] readLine(ProgressMonitor progressMonitor) throws IOException {
		if (rows != null && rowIndex < rows.size()) {
			TableTableRow row = rows.get(rowIndex++);

			if (rowIndex % 5000 == 0) {
				System.out.println("Lines read: "+rowIndex);
			}

			List<String> result = new ArrayList<String>();
			boolean allFieldsBlank = true;
			for (String text : row.getText().replaceFirst(SEP, "").replaceAll("\\]", "").replaceAll("null", SEP).split(SEP)) {
				result.add(text);
				if (allFieldsBlank && !text.isEmpty()) {
					allFieldsBlank = false;
				}
			}
			
			return rowIndex == 1 || !allFieldsBlank ? result.toArray(new String[0]) : null;
		}
		return null;
	}
}
