// License: GPL. Copyright 2007 by Immanuel Scholz and others
package mirrored_download;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.io.InputStream;

import org.openstreetmap.josm.io.BoundingBoxDownloader;
import org.openstreetmap.josm.io.OsmApi;
import org.openstreetmap.josm.io.OsmTransferException;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.xml.sax.SAXException;

public class BoundingBoxDownloader2 extends BoundingBoxDownloader {

    public BoundingBoxDownloader2(Bounds downloadArea) {
        super(downloadArea);
    }

    protected InputStream getInputStream(String urlStr, ProgressMonitor progressMonitor) throws OsmTransferException  {
        try {
            OsmApi.getOsmApi().initialize(progressMonitor);
            urlStr = "http://overpass-api.de/api/xapi?" + urlStr;
            return getInputStreamRaw(urlStr, progressMonitor);
        } finally {
            progressMonitor.invalidate();
        }
    }

}
