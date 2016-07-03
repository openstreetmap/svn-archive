// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.tabular;

import java.nio.charset.Charset;

public interface CsvHandler extends SpreadSheetHandler {

    void setCharset(Charset charset);

    void setCharset(String charset);

    Charset getCharset();

    void setSeparator(String sep);

    String getSeparator();
}
