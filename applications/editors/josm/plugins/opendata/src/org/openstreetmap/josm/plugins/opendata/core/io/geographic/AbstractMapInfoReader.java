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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.io.AbstractReader;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;

public abstract class AbstractMapInfoReader extends AbstractReader implements OdConstants {

	protected static final String VERSION_1 = "1";
	protected static final String VERSION_2 = "2";
	protected static final String VERSION_300 = "300";
	protected static final String VERSION_450 = "450";

	protected static final String CHARSET_WINDOWS = "WindowsLatin1";
	protected static final String CHARSET_NEUTRAL = "Neutral";
	protected static final String CHARSET_MAC     = "MacRoman";
	
	protected BufferedReader headerReader;

	protected String line;
	protected int lineNum = 0;

	protected String version;

	// Columns
	protected int numcolumns = -1;
	protected List<String> columns;

	protected final File getDataFile(File headerFile, String extension) {
		String filename = headerFile.getName().substring(0, headerFile.getName().lastIndexOf('.'));
		File dataFile = new File(headerFile.getParent() + File.separator + filename + extension.toUpperCase());
		if (!dataFile.exists()) {
			dataFile = new File(headerFile.getParent() + File.separator + filename + extension.toLowerCase());
		}
		return dataFile;
	}
	
	protected final BufferedReader getDataReader(File headerFile, String extension, Charset charset) throws FileNotFoundException {
		File dataFile = getDataFile(headerFile, extension);
		return dataFile.exists() ? new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), charset)) : null;
	}

	protected Charset parseCharset(String[] words) {
		return parseCharset(words, 1);
	}

	protected Charset parseCharset(String[] words, int index) {
		words[index] = words[index].replace("\"", "");
		if (words[index].equalsIgnoreCase(CHARSET_WINDOWS)) {
			return Charset.forName(CP1252);
		} else if (words[index].equalsIgnoreCase(CHARSET_NEUTRAL)) {
			return Charset.forName(ISO8859_15);
		} else if (words[index].equalsIgnoreCase(CHARSET_MAC)) {
			return Charset.forName(MAC_ROMAN);
		} else {
			System.err.println("Line "+lineNum+". Unknown charset detected: "+line);
			return Charset.forName(words[index]);
		}
	}

	protected void parseVersion(String[] words) {
		version = words[1];
	}
	
	protected void parseColumns(String[] words) {
		columns = new ArrayList<String>();
		numcolumns = Integer.parseInt(words[1]);
	}

	protected final void parseHeader() throws IOException {
		while ((line = headerReader.readLine()) != null) {
			lineNum++;
			while (line.contains("  ")) {
				line = line.replace("  ", " ");
			}
			String [] words = line.isEmpty() ? null : line.trim().split(" ");
			if (words != null && words.length > 0) {
				parseHeaderLine(words);
			}
		}
	}
	
	protected abstract void parseHeaderLine(String[] words) throws IOException;
}
