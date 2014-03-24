// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.tabular;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jopendocument.model.OpenDocument;
import org.jopendocument.model.office.OfficeSpreadsheet;
import org.jopendocument.model.table.TableTable;
import org.jopendocument.model.table.TableTableCell;
import org.jopendocument.model.table.TableTableRow;
import org.jopendocument.model.text.TextP;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;

public class OdsReader extends SpreadSheetReader {

	private OpenDocument doc;
	private TableTable sheet;
	private List<TableTableRow> rows;
	private int rowIndex;
	
	private static final String SEP = "TextP:[";
	
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
			for (TableTableCell cell : row.getAllCells()) {
			    TextP textP = cell.getTextP();
			    String text = textP == null ? "" : textP.toString().replace(SEP, "").replace("]", "").replace("null", "").trim();
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
