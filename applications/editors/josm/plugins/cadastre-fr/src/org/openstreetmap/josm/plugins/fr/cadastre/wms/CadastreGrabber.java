// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.wms;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.plugins.fr.cadastre.CadastrePlugin;
import org.openstreetmap.josm.tools.Logging;

public class CadastreGrabber {

    private CadastreInterface wmsInterface = new CadastreInterface();

    GeorefImage grab(WMSLayer wmsLayer, EastNorth lambertMin, EastNorth lambertMax)
            throws IOException, OsmTransferException {
        try {
            URL url = null;
            if (wmsLayer.isRaster())
                url = getURLRaster(wmsLayer, lambertMin, lambertMax);
            else
                url = getURLVector(lambertMin, lambertMax);
            BufferedImage img = grab(url);
            if (img == null)
                throw new OsmTransferException(url.toString());
            ImageModifier imageModified;
            if (wmsLayer.isRaster())
                imageModified = new RasterImageModifier(img);
            else
                imageModified = new VectorImageModifier(img, false);
            return new GeorefImage(imageModified.getBufferedImage(), lambertMin, lambertMax, wmsLayer);
        } catch (MalformedURLException e) {
            throw (IOException) new IOException(tr("CadastreGrabber: Illegal url.")).initCause(e);
        }
    }

    private static URL getURLRaster(WMSLayer wmsLayer, EastNorth lambertMin, EastNorth lambertMax) throws MalformedURLException {
        // GET /scpc/wms?version=1.1&request=GetMap&layers=CDIF:PMC@QH4480001701&format=image/png&bbox=-1186,0,13555,8830&width=576&height=345&exception=application/vnd.ogc.se_inimage&styles= HTTP/1.1
        final int cRasterX = CadastrePlugin.imageWidth; // keep width constant and adjust width to original image proportions
        String str = CadastreInterface.BASE_URL+"/scpc/wms?version=1.1&request=GetMap";
        str += "&layers=CDIF:PMC@";
        str += wmsLayer.getCodeCommune();
        str += "&format=image/png";
        //str += "&format=image/jpeg";
        str += "&bbox=";
        str += wmsLayer.eastNorth2raster(lambertMin, lambertMax);
        str += "&width="+cRasterX+"&height="; // maximum allowed by wms server (576/345, 800/378, 1000/634)
        str += (int) (cRasterX*(wmsLayer.communeBBox.max.getY() - wmsLayer.communeBBox.min.getY())/(wmsLayer.communeBBox.max.getX() - wmsLayer.communeBBox.min.getX()));
        str += "&exception=application/vnd.ogc.se_inimage&styles="; // required for raster images
        Logging.info("URL="+str);
        return new URL(str.replace(" ", "%20"));
    }

    private static URL buildURLVector(String layers, String styles,
            int width, int height,
            EastNorth lambertMin, EastNorth lambertMax) throws MalformedURLException {
        String str = CadastreInterface.BASE_URL+"/scpc/wms?version=1.1&request=GetMap";
        str += "&layers="+ layers;
        str += "&format=image/png";
        str += "&bbox="+lambertMin.east()+",";
        str += lambertMin.north() + ",";
        str += lambertMax.east() + ",";
        str += lambertMax.north();
        str += "&width="+width+"&height="+height;
        str += "&exception=application/vnd.ogc.se_inimage"; // works also without (but slower ?)
        str += "&styles=" + styles;
        Logging.info("URL="+str);
        return new URL(str.replace(" ", "%20"));
    }

    private static URL getURLVector(EastNorth lambertMin, EastNorth lambertMax) throws MalformedURLException {
        return buildURLVector(CadastrePlugin.grabLayers, CadastrePlugin.grabStyles,
                CadastrePlugin.imageWidth, CadastrePlugin.imageHeight,
                lambertMin, lambertMax);
    }

    private BufferedImage grab(URL url) throws IOException, OsmTransferException {
        try (InputStream is = wmsInterface.getContent(url)) {
            return ImageIO.read(is);
        }
    }

    CadastreInterface getWmsInterface() {
        return wmsInterface;
    }
}
