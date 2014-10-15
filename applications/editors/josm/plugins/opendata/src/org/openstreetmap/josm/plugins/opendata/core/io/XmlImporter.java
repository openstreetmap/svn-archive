// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javax.xml.bind.JAXBException;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;

public class XmlImporter extends AbstractImporter {

    public static final ExtensionFileFilter XML_FILE_FILTER = new ExtensionFileFilter(
            OdConstants.XML_EXT, OdConstants.XML_EXT, tr("OpenData XML files") + " (*."+OdConstants.XML_EXT+")");

    public XmlImporter() {
        super(XML_FILE_FILTER);
    }
    
    @Override
    public boolean acceptFile(File pathname) {
        if (super.acceptFile(pathname)) {
            for (URL schemaURL : NeptuneReader.getSchemas()) {
                if (NeptuneReader.acceptsXmlNeptuneFile(pathname, schemaURL)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected DataSet parseDataSet(InputStream in, ProgressMonitor instance)
            throws IllegalDataException {
        try {
            return NeptuneReader.parseDataSet(in, handler, instance);
        } catch (JAXBException e) {
            throw new IllegalDataException(e);
        }
    }
}
