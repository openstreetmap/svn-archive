// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.tabular;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;

public class XlsReader extends SpreadSheetReader {

	private Workbook wb;
	private Sheet sheet;
	private int rowIndex;
	
	public XlsReader(SpreadSheetHandler handler) {
		super(handler);
	}

	public static DataSet parseDataSet(InputStream in,
			AbstractDataSetHandler handler, ProgressMonitor instance) throws IOException {
		return new XlsReader(handler != null ? handler.getSpreadSheetHandler() : null).parse(in, instance);
	}

	@Override
	protected void initResources(InputStream in, ProgressMonitor progressMonitor) throws IOException {
		System.out.println("Parsing XLS file");
		try {
			wb = new HSSFWorkbook(new POIFSFileSystem(in));
			sheet = wb.getSheetAt(getSheetNumber());
			rowIndex = 0;
		} catch (ExceptionInInitializerError e) {
			Throwable ex = e.getException();
			if (ex != null && ex.getMessage() != null) {
				System.err.println(ex.getClass()+": "+ex.getMessage());
			}
			throw new IOException(e);
	    } catch (Throwable t) {
			throw new IOException(t);
		}
	}

	@Override
	protected String[] readLine(ProgressMonitor progressMonitor) throws IOException {
		if (sheet != null) {
			Row row = sheet.getRow(rowIndex++);
			if (row != null) {
				List<String> result = new ArrayList<>();
				for (Cell cell : row) {
		            switch (cell.getCellType()) {
		                case Cell.CELL_TYPE_STRING:
		                    result.add(cell.getRichStringCellValue().getString());
		                    break;
		                case Cell.CELL_TYPE_NUMERIC:
		                    if (DateUtil.isCellDateFormatted(cell)) {
		                    	result.add(cell.getDateCellValue().toString());
		                    } else {
		                    	result.add(Double.toString(cell.getNumericCellValue()));
		                    }
		                    break;
		                case Cell.CELL_TYPE_BOOLEAN:
		                	result.add(Boolean.toString(cell.getBooleanCellValue()));
		                    break;
		                case Cell.CELL_TYPE_FORMULA:
		                	result.add(cell.getCellFormula());
		                    break;
		                default:
		                    result.add("");
	                }
				}
				return result.toArray(new String[0]);
			}
		}
		return null;
	}
}
