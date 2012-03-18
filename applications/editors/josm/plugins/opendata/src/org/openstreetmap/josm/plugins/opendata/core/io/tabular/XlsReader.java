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
				List<String> result = new ArrayList<String>();
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
