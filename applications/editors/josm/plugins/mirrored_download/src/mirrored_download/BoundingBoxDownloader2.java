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
import org.openstreetmap.josm.data.osm.DataSource;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.xml.sax.SAXException;

public class BoundingBoxDownloader2 extends BoundingBoxDownloader {

    /**
     * The boundings of the desired map data.
     */
    private final double lat1;
    private final double lon1;
    private final double lat2;
    private final double lon2;
    private final boolean crosses180th;

    public BoundingBoxDownloader2(Bounds downloadArea) {
        super(downloadArea);

        this.lat1 = downloadArea.getMin().lat();
        this.lon1 = downloadArea.getMin().lon();
        this.lat2 = downloadArea.getMax().lat();
        this.lon2 = downloadArea.getMax().lon();
        this.crosses180th = downloadArea.crosses180thMeridian();
    }

    protected InputStream getInputStream(String urlStr, ProgressMonitor progressMonitor)
        throws OsmTransferException  {

        try {
            OsmApi.getOsmApi().initialize(progressMonitor);
            urlStr = MirroredDownloadPlugin.getDownloadUrl() + urlStr;
            return getInputStreamRaw(urlStr, progressMonitor);
        } finally {
            progressMonitor.invalidate();
        }
    }

    public DataSet parseOsm(ProgressMonitor progressMonitor) throws OsmTransferException {

        DataSet ds = super.parseOsm(progressMonitor);

        System.out.println(ds.dataSources.toString());
        if (ds != null && ds.dataSources.size() == 0)
        {
            if (crosses180th)
            {
                Bounds bounds = new Bounds(lat1, lon1, lat2, 180.0);
                DataSource src = new DataSource(bounds, MirroredDownloadPlugin.getDownloadUrl());
                ds.dataSources.add(src);

                bounds = new Bounds(lat1, -180.0, lat2, lon2);
                src = new DataSource(bounds, MirroredDownloadPlugin.getDownloadUrl());
                ds.dataSources.add(src);
            }
            else
            {
                Bounds bounds = new Bounds(lat1, lon1, lat2, lon2);
                DataSource src = new DataSource(bounds, MirroredDownloadPlugin.getDownloadUrl());
                ds.dataSources.add(src);
            }
        }
        System.out.println(ds.dataSources.toString());

        return ds;
    }
}
