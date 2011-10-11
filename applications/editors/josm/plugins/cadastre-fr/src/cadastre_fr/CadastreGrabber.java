// License: GPL. v2 and later. Copyright 2008-2009 by Pieren <pieren3@gmail.com> and others
package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.io.ProgressInputStream;

public class CadastreGrabber {

    private CadastreInterface wmsInterface = new CadastreInterface();

    public GeorefImage grab(WMSLayer wmsLayer, EastNorth lambertMin, EastNorth lambertMax) throws IOException, OsmTransferException {

        try {
            URL url = null;
            if (wmsLayer.isRaster())
                url = getURLRaster(wmsLayer, lambertMin, lambertMax);
            else
                url = getURLVector(lambertMin, lambertMax);
            BufferedImage img = grab(url);
            ImageModifier imageModified;
            if (wmsLayer.isRaster())
                imageModified = new RasterImageModifier(img);
            else
                imageModified = new VectorImageModifier(img, false);
            return new GeorefImage(imageModified.bufferedImage, lambertMin, lambertMax, wmsLayer);
        } catch (MalformedURLException e) {
            throw (IOException) new IOException(tr("CadastreGrabber: Illegal url.")).initCause(e);
        }
    }

    private URL getURLRaster(WMSLayer wmsLayer, EastNorth lambertMin, EastNorth lambertMax) throws MalformedURLException {
        // GET /scpc/wms?version=1.1&request=GetMap&layers=CDIF:PMC@QH4480001701&format=image/png&bbox=-1186,0,13555,8830&width=576&height=345&exception=application/vnd.ogc.se_inimage&styles= HTTP/1.1
        final int cRasterX = CadastrePlugin.imageWidth; // keep width constant and adjust width to original image proportions
        String str = new String(wmsInterface.baseURL+"/scpc/wms?version=1.1&request=GetMap");
        str += "&layers=CDIF:PMC@";
        str += wmsLayer.getCodeCommune();
        str += "&format=image/png";
        //str += "&format=image/jpeg";
        str += "&bbox=";
        str += wmsLayer.eastNorth2raster(lambertMin, lambertMax);
        str += "&width="+cRasterX+"&height="; // maximum allowed by wms server (576/345, 800/378, 1000/634)
        str += (int)(cRasterX*(wmsLayer.communeBBox.max.getY() - wmsLayer.communeBBox.min.getY())/(wmsLayer.communeBBox.max.getX() - wmsLayer.communeBBox.min.getX()));
        str += "&exception=application/vnd.ogc.se_inimage&styles="; // required for raster images
        System.out.println("URL="+str);
        return new URL(str.replace(" ", "%20"));
    }

    private URL buildURLVector(String layers, String styles,
            int width, int height,
            EastNorth lambertMin, EastNorth lambertMax) throws MalformedURLException {
        String str = new String(wmsInterface.baseURL+"/scpc/wms?version=1.1&request=GetMap");
        str += "&layers="+ layers;
        str += "&format=image/png";
        //str += "&format=image/jpeg";
        str += "&bbox="+lambertMin.east()+",";
        str += lambertMin.north() + ",";
        str += lambertMax.east() + ",";
        str += lambertMax.north();
        str += "&width="+width+"&height="+height;
        str += "&exception=application/vnd.ogc.se_inimage"; // works also without (but slower ?)
        str += "&styles=" + styles;
        System.out.println("URL="+str);
        return new URL(str.replace(" ", "%20"));
    }

    private URL getURLVector(EastNorth lambertMin, EastNorth lambertMax) throws MalformedURLException {
        return buildURLVector(CadastrePlugin.grabLayers, CadastrePlugin.grabStyles,
                CadastrePlugin.imageWidth, CadastrePlugin.imageHeight,
                lambertMin, lambertMax);
    }

    private BufferedImage grab(URL url) throws IOException, OsmTransferException {
        wmsInterface.urlConn = (HttpURLConnection)url.openConnection();
        wmsInterface.urlConn.setRequestMethod("GET");
        wmsInterface.setCookie();
        InputStream is = new ProgressInputStream(wmsInterface.urlConn, NullProgressMonitor.INSTANCE);
        BufferedImage img = ImageIO.read(is);
        is.close();
        return img;
    }

    public CadastreInterface getWmsInterface() {
        return wmsInterface;
    }

}
