// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.tabular;

import java.nio.charset.Charset;

public interface CsvHandler extends SpreadSheetHandler {

	public void setCharset(Charset charset);

	public void setCharset(String charset);

	public Charset getCharset();

	public void setSeparator(String sep);
	
	public String getSeparator();
}
