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

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;

public class DefaultSpreadSheetHandler implements SpreadSheetHandler {

	private int sheetNumber = -1;
	private int lineNumber = -1;
	private boolean handlesProjection = false;
	
	private int xCol = -1;
	private int yCol = -1;
	
	@Override
	public int getSheetNumber() {
		return sheetNumber;
	}

	@Override
	public void setSheetNumber(int n) {
		sheetNumber = n;
	}
	
	@Override
	public boolean handlesProjection() {
		return handlesProjection;
	}

	@Override
	public void setHandlesProjection(boolean handle) {
		handlesProjection = handle;
	}

	@Override
	public LatLon getCoor(EastNorth en, String[] fields) {
		return null;
	}

	@Override
	public void setLineNumber(int n) {
		lineNumber = n;
	}

	@Override
	public int getLineNumber() {
		return lineNumber;
	}

	@Override
	public void setXCol(int i) {
		xCol = i;
	}

	@Override
	public void setYCol(int i) {
		yCol = i;
	}

	@Override
	public int getXCol() {
		return xCol;
	}

	@Override
	public int getYCol() {
		return yCol;
	}
}
