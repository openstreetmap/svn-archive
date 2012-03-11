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
package org.openstreetmap.josm.plugins.opendata.core.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.InputStream;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.AbstractReader;
import org.openstreetmap.josm.io.OsmServerReader;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.io.archive.ZipReader;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.KmlReader;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.KmzReader;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifReader;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.ShpReader;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.TabReader;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.CsvReader;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.OdsReader;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.XlsReader;
import org.openstreetmap.josm.tools.CheckParameterUtil;

public class NetworkReader extends OsmServerReader {

	private final String url;
	private final AbstractDataSetHandler handler;
	private Class<? extends AbstractReader> readerClass;

	private File file;
	
    public NetworkReader(String url, AbstractDataSetHandler handler, Class<? extends AbstractReader> readerClass) {
        CheckParameterUtil.ensureParameterNotNull(url, "url");
        //CheckParameterUtil.ensureParameterNotNull(readerClass, "readerClass");
    	this.url = url;
        this.readerClass = readerClass;
        this.handler = handler;
    }
    
	public final File getReadFile() {
		return file;
	}

	@Override
	public DataSet parseOsm(ProgressMonitor progressMonitor) throws OsmTransferException {
        InputStream in = null;
        progressMonitor.beginTask(tr("Contacting Server...", 10));
        try {
            in = getInputStreamRaw(url, progressMonitor.createSubTaskMonitor(9, false));
            if (in == null)
                return null;
            progressMonitor.subTask(tr("Downloading data..."));
            ProgressMonitor instance = progressMonitor.createSubTaskMonitor(1, false);
            if (readerClass == null) {
            	String contentType = this.activeConnection.getContentType();
            	if (contentType.startsWith("application/zip")) {
            		readerClass = ZipReader.class;
            	} else {
            		throw new IllegalArgumentException("Unsupported content type: "+contentType);
            	}
            }
            if (readerClass.equals(ZipReader.class)) {
            	ZipReader zipReader = new ZipReader(in, handler);
            	DataSet ds = zipReader.parseDoc(instance);
            	file = zipReader.getReadFile();
            	return ds;
            } else if (readerClass.equals(KmlReader.class)) {
            	return KmlReader.parseDataSet(in, instance);
            } else if (readerClass.equals(KmzReader.class)) {
            	return KmzReader.parseDataSet(in, instance);
            } else if (readerClass.equals(MifReader.class)) {
            	return MifReader.parseDataSet(in, null, handler, instance);
            } else if (readerClass.equals(ShpReader.class)) {
            	return ShpReader.parseDataSet(in, null, handler, instance);
            } else if (readerClass.equals(TabReader.class)) {
            	return TabReader.parseDataSet(in, null, handler, instance);
            } else if (readerClass.equals(CsvReader.class)) {
            	return CsvReader.parseDataSet(in, handler, instance);
            } else if (readerClass.equals(OdsReader.class)) {
            	return OdsReader.parseDataSet(in, handler, instance);
            } else if (readerClass.equals(XlsReader.class)) {
            	return XlsReader.parseDataSet(in, handler, instance);
            } else {
            	throw new IllegalArgumentException("Unsupported reader class: "+readerClass.getName());
            }
        } catch (OsmTransferException e) {
            throw e;
        } catch (Exception e) {
            if (cancel)
                return null;
            throw new OsmTransferException(e);
        } finally {
            progressMonitor.finishTask();
            try {
                activeConnection = null;
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {/* ignore it */}
        }
	}
}
