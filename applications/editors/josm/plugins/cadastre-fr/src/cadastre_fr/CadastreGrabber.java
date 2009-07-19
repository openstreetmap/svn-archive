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

    public static final double epsilon = 1e-11;

    private CadastreInterface wmsInterface = new CadastreInterface(this);
    private String lastWMSLayerName = null;

    CadastreGrabber() {
        getWmsInterface().downloadCancelled = false;
    }

    public GeorefImage grab(WMSLayer wmsLayer, EastNorth lambertMin, EastNorth lambertMax) throws IOException, OsmTransferException {

        try {
            URL url = null;
            if (wmsLayer.isRaster())
                url = getURLRaster(wmsLayer, lambertMin, lambertMax);
            else
                url = getURLVector(lambertMin, lambertMax);
            System.out.println("grab:"+url);
            BufferedImage img = grab(url);
            ImageModifier imageModified = new ImageModifier(img);
            return new GeorefImage(imageModified.bufferedImage, lambertMin, lambertMax);
        } catch (MalformedURLException e) {
            throw (IOException) new IOException(tr("CadastreGrabber: Illegal url.")).initCause(e);
        }
    }

    private URL getURLRaster(WMSLayer wmsLayer, EastNorth lambertMin, EastNorth lambertMax) throws MalformedURLException {
        String str = new String(wmsInterface.baseURL+"/scpc/wms?version=1.1&request=GetMap");
        str += "&layers=CDIF:PMC@";
        str += wmsLayer.getCodeCommune();
        str += "&format=image/png";
        str += "&bbox=";
        str += wmsLayer.eastNorth2raster(lambertMin, lambertMax);
        str += "&width=600&height=600"; // maximum allowed by wms server
        str += "&exception=application/vnd.ogc.se_inimage&styles=";
        return new URL(str.replace(" ", "%20"));
    }

    private URL getURLVector(EastNorth lambertMin, EastNorth lambertMax) throws MalformedURLException {
        String str = new String(wmsInterface.baseURL+"/scpc/wms?version=1.1&request=GetMap");
        str += "&layers=CDIF:LS3,CDIF:LS2,CDIF:LS1,CDIF:PARCELLE,CDIF:NUMERO";
        str += ",CDIF:PT3,CDIF:PT2,CDIF:PT1,CDIF:LIEUDIT";
        str += ",CDIF:SUBSECTION";
        str += ",CDIF:SECTION";
        str += ",CDIF:COMMUNE";
        str += "&format=image/png";
        //str += "&format=image/jpeg";
        str += "&bbox="+lambertMin.east()+",";
        str += lambertMin.north() + ",";
        str += lambertMax.east() + ",";
        str += lambertMax.north();
        //str += "&width=800&height=600"; // maximum allowed by wms server
        str += "&width=1000&height=800"; // maximum allowed by wms server
        str += "&styles=LS3_90,LS2_90,LS1_90,PARCELLE_90,NUMERO_90,PT3_90,PT2_90,PT1_90,LIEUDIT_90";
        str += ",SUBSECTION_90";
        str += ",SECTION_90";
        str += ",COMMUNE_90";
        System.out.println("URL="+str);
        return new URL(str.replace(" ", "%20"));
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

    public String getLastWMSLayerName() {
        return lastWMSLayerName;
    }

    public void setLastWMSLayerName(String lastWMSLayerName) {
        this.lastWMSLayerName = lastWMSLayerName;
    }

}
