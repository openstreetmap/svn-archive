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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.AbstractReader;
import org.openstreetmap.josm.io.OsmServerReader;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
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

public class NetworkReader extends OsmServerReader implements OdConstants {

	private final String url;
	private final AbstractDataSetHandler handler;
	private Class<? extends AbstractReader> readerClass;

	private File file;
	private String filename;
	
    public NetworkReader(String url, AbstractDataSetHandler handler) {
        CheckParameterUtil.ensureParameterNotNull(url, "url");
    	this.url = url;
        this.handler = handler;
        this.readerClass = null;
    }
    
	public final File getReadFile() {
		return file;
	}

	public final String getReadFileName() {
		return filename;
	}

	private Class<? extends AbstractReader> findReaderByAttachment() {
		String cdisp = this.activeConnection.getHeaderField("Content-disposition");
		if (cdisp != null) {
			Matcher m = Pattern.compile("attachment;.?filename=(.*)").matcher(cdisp);
			if (m.matches()) {
				filename = m.group(1);
				return findReaderByExtension(filename.toLowerCase());
			}
		}
		return null;
	}

	private Class<? extends AbstractReader> findReaderByContentType() {
    	String contentType = this.activeConnection.getContentType();
    	if (contentType.startsWith("application/zip")) {
    		return ZipReader.class;
    	} else if (contentType.startsWith("application/vnd.ms-excel")) {
    		return XlsReader.class;
    	} else if (contentType.startsWith("application/octet-stream")) {
        	//return OdsReader.class;//FIXME, can be anything
    	} else if (contentType.startsWith("text/csv")) {
    		return CsvReader.class;
    	} else if (contentType.startsWith("text/plain")) {//TODO: extract charset
    		return CsvReader.class;
    	} else if (contentType.startsWith("tdyn/html")) {
        	//return CsvReader.class;//FIXME, can also be .tar.gz
    	} else {
    		System.err.println("Unsupported content type: "+contentType);
    	}
    	return null;
	}

	private Class<? extends AbstractReader> findReaderByExtension(String filename) {
		filename = filename.replace("\"", "");
    	if (filename.endsWith("."+XLS_EXT)) {
    		return XlsReader.class;
    	} else if (filename.endsWith("."+CSV_EXT)) {
    		return CsvReader.class;
    	} else if (filename.endsWith("."+ODS_EXT)) {
    		return OdsReader.class;
    	} else if (filename.endsWith("."+KML_EXT)) {
    		return KmlReader.class;
    	} else if (filename.endsWith("."+KMZ_EXT)) {
    		return KmzReader.class;
    	} else if (filename.endsWith("."+MIF_EXT)) {
    		return MifReader.class;
    	} else if (filename.endsWith("."+SHP_EXT)) {
    		return ShpReader.class;
    	} else if (filename.endsWith("."+TAB_EXT)) {
    		return TabReader.class;
    	} else if (filename.endsWith("."+ZIP_EXT)) {
    		return ZipReader.class;
    	} else {
    		return null;
    	}
	}

	@Override
	public DataSet parseOsm(ProgressMonitor progressMonitor) throws OsmTransferException {
        InputStream in = null;
        ProgressMonitor instance = null;
        try {
        	in = getInputStreamRaw(url, progressMonitor);
            if (in == null)
                return null;
            progressMonitor.subTask(tr("Downloading data..."));
            if (readerClass == null) {
            	readerClass = findReaderByAttachment();
            }
            if (readerClass == null) {
            	readerClass = findReaderByContentType();
            }
            if (readerClass == null) {
            	readerClass = findReaderByExtension(url.toLowerCase());
            }
            if (readerClass == null) {
           		throw new OsmTransferException("Cannot find appropriate reader !");//TODO handler job ?
            } else if (findReaderByExtension(url.toLowerCase()) != null) {
            	filename = url.substring(url.lastIndexOf('/')+1);
            }
            instance = progressMonitor.createSubTaskMonitor(ProgressMonitor.ALL_TICKS, false);
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
        	if (instance != null) {
        		instance.finishTask();
        	}
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
