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

import java.nio.charset.Charset;

public class DefaultCsvHandler extends DefaultSpreadSheetHandler implements CsvHandler {

	private Charset charset = null;
	private String separator = null;
	
	@Override
	public void setCharset(Charset cs) {
		charset = cs;
	}

	@Override
	public void setCharset(String charset) {
		setCharset(Charset.forName(charset));
	}

	@Override
	public Charset getCharset() {
		return charset;
	}

	@Override
	public void setSeparator(String sep) {
		separator = sep;
	}

	@Override
	public String getSeparator() {
		return separator;
	}
}
