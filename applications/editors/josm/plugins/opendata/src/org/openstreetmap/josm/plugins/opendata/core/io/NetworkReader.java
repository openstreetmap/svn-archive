// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.AbstractReader;
import org.openstreetmap.josm.io.OsmServerReader;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.io.archive.ArchiveReader;
import org.openstreetmap.josm.plugins.opendata.core.io.archive.SevenZipReader;
import org.openstreetmap.josm.plugins.opendata.core.io.archive.ZipReader;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.GmlReader;
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
	private final boolean promptUser;

	private File file;
	private String filename;
    
    /**
     * File readers
     */
    public static final Map<String, Class<? extends AbstractReader>> FILE_READERS = new HashMap<>();
    static {
        FILE_READERS.put(OdConstants.CSV_EXT, CsvReader.class);
        FILE_READERS.put(OdConstants.KML_EXT, KmlReader.class);
        FILE_READERS.put(OdConstants.KMZ_EXT, KmzReader.class);
        FILE_READERS.put(OdConstants.GML_EXT, GmlReader.class);
        FILE_READERS.put(OdConstants.XLS_EXT, XlsReader.class);
        FILE_READERS.put(OdConstants.ODS_EXT, OdsReader.class);
        FILE_READERS.put(OdConstants.SHP_EXT, ShpReader.class);
        FILE_READERS.put(OdConstants.MIF_EXT, MifReader.class);
        FILE_READERS.put(OdConstants.TAB_EXT, TabReader.class);
    }
    
    public static final Map<String, Class<? extends AbstractReader>> FILE_AND_ARCHIVE_READERS = new HashMap<>(FILE_READERS);
    static {
        FILE_AND_ARCHIVE_READERS.put(OdConstants.ZIP_EXT, ZipReader.class);
        FILE_AND_ARCHIVE_READERS.put(OdConstants.SEVENZIP_EXT, SevenZipReader.class);
    }

    public NetworkReader(String url, AbstractDataSetHandler handler, boolean promptUser) {
        CheckParameterUtil.ensureParameterNotNull(url, "url");
    	this.url = url;
        this.handler = handler;
        this.readerClass = null;
        this.promptUser = promptUser;
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
				return findReaderByExtension(filename);
			}
		}
		return null;
	}

	private Class<? extends AbstractReader> findReaderByContentType() {
    	String contentType = this.activeConnection.getContentType();
    	if (contentType.startsWith("application/zip")) {
    		return ZipReader.class;
        } else if (contentType.startsWith("application/x-7z-compressed")) {
            return SevenZipReader.class;
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
		filename = filename.replace("\"", "").toLowerCase();
		for (String ext : FILE_AND_ARCHIVE_READERS.keySet()) {
		    if (filename.endsWith("."+ext)) {
		        return FILE_AND_ARCHIVE_READERS.get(ext);
		    }
		}
		return null;
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
                readerClass = findReaderByExtension(url);
            }
            if (readerClass == null) {
            	readerClass = findReaderByContentType();
            }
            if (readerClass == null) {
           		throw new OsmTransferException("Cannot find appropriate reader !");//TODO handler job ?
            } else if (findReaderByExtension(url) != null) {
            	filename = url.substring(url.lastIndexOf('/')+1);
            }
            instance = progressMonitor.createSubTaskMonitor(ProgressMonitor.ALL_TICKS, false);
            if (readerClass.equals(ZipReader.class) || readerClass.equals(SevenZipReader.class)) {
            	ArchiveReader zipReader = readerClass.equals(ZipReader.class) 
            	        ? new ZipReader(in, handler, promptUser) : new SevenZipReader(in, handler, promptUser);
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
            } else if (readerClass.equals(GmlReader.class)) {
            	return GmlReader.parseDataSet(in, handler, instance);
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
