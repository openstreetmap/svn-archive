package iodb;

import java.text.SimpleDateFormat;
import java.util.*;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.imagery.ImageryInfo;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Some common static methods for querying imagery layers.
 * 
 * @author zverik
 */
public class ImageryOffsetTools {
    public static final String DIALOG_TITLE = tr("Imagery Offset");
    
    public static ImageryLayer getTopImageryLayer() {
        if( Main.map == null || Main.map.mapView == null )
            return null;
        List<ImageryLayer> layers = Main.map.mapView.getLayersOfType(ImageryLayer.class);
        for( ImageryLayer layer : layers ) {
            if( layer.isVisible() ) {
                return layer;
            }
        }
        return null;
    }
    
    public static LatLon getMapCenter() {
        Projection proj = Main.getProjection();
        return Main.map == null || Main.map.mapView == null
                ? new LatLon(0, 0) : proj.eastNorth2latlon(Main.map.mapView.getCenter());
    }
    
    public static LatLon getLayerOffset( ImageryLayer layer, LatLon center ) {
        Projection proj = Main.getProjection();
        EastNorth offsetCenter = proj.latlon2eastNorth(center);
        EastNorth centerOffset = offsetCenter.add(-layer.getDx(), -layer.getDy());
        LatLon offsetLL = proj.eastNorth2latlon(centerOffset);
        return offsetLL;
    }
    
    public static void applyLayerOffset( ImageryLayer layer, ImageryOffset offset ) {
        double[] dxy = calculateOffset(offset);
        layer.setOffset(dxy[0], dxy[1]);
    }

    /**
     * Calculate dx and dy for imagery offset.
     * @return [dx, dy]
     */
    public static double[] calculateOffset( ImageryOffset offset ) {
        Projection proj = Main.getProjection();
        EastNorth center = proj.latlon2eastNorth(offset.getPosition());
        EastNorth offsetPos = proj.latlon2eastNorth(offset.getImageryPos());
        return new double[] { center.getX() - offsetPos.getX(), center.getY() - offsetPos.getY() };
    }
    
    public static String getImageryID( ImageryLayer layer ) {
        if( layer == null )
            return null;
        
        String url = layer.getInfo().getUrl();
        if( url == null )
            return null;
        
        // predefined layers
        if( layer.getInfo().getImageryType().equals(ImageryInfo.ImageryType.BING) || url.contains("tiles.virtualearth.net") )
            return "bing";

        if( layer.getInfo().getImageryType().equals(ImageryInfo.ImageryType.SCANEX) && url.toLowerCase().equals("irs") )
            return "scanex_irs";

        boolean isWMS = layer.getInfo().getImageryType().equals(ImageryInfo.ImageryType.WMS);

        System.out.println(url);

        // Remove protocol
        int i = url.indexOf("://");
        url = url.substring(i + 3);

        // Split URL into address and query string
        i = url.indexOf('?');
        String query = "";
        if( i > 0 ) {
            query = url.substring(i);
            url = url.substring(0, i);
        }

        // Parse query parameters into a sorted map
        Map<String, String> qparams = new TreeMap<String, String>();
        String[] qparamsStr = query.length() > 1 ? query.substring(1).split("&") : new String[0];
        for( String param : qparamsStr ) {
            String[] kv = param.split("=");
            kv[0] = kv[0].toLowerCase();
            // WMS: if this is WMS, remove all parameters except map and layers
            if( isWMS && !(kv[0].equals("map") || kv[0].equals("layers")) )
                continue;
            // TMS: skip parameters with variable values
            if( kv.length > 1 && kv[1].indexOf('{') >= 0 && kv[1].indexOf('}') > 0 )
                continue;
            qparams.put(kv[0].toLowerCase(), kv.length > 1 ? kv[1] : null);
        }

        // Reconstruct query parameters
        StringBuilder sb = new StringBuilder();
        for( String qk : qparams.keySet() ) {
            if( sb.length() > 0 )
                sb.append('&');
            else if( query.length() > 0 )
                sb.append('?');
            sb.append(qk).append('=').append(qparams.get(qk));
        }
        query = sb.toString();

        // TMS: remove /{zoom} and /{y}.png parts
        url = url.replaceAll("\\/\\{[^}]+\\}(?:\\.\\w+)?", "");
        // TMS: remove variable parts
        url = url.replaceAll("\\{[^}]+\\}", "");
        while( url.contains("..") )
            url = url.replace("..", ".");
        if( url.startsWith(".") )
            url = url.substring(1);

        System.out.println("-> " + url + query);
        return url + query;
    }
    
    // Following three methods were snatched from TMSLayer
    private static double latToTileY(double lat, int zoom) {
        double l = lat / 180 * Math.PI;
        double pf = Math.log(Math.tan(l) + (1 / Math.cos(l)));
        return Math.pow(2.0, zoom - 1) * (Math.PI - pf) / Math.PI;
    }

    private static double lonToTileX(double lon, int zoom) {
        return Math.pow(2.0, zoom - 3) * (lon + 180.0) / 45.0;
    }

    public static int getCurrentZoom() {
        if (Main.map == null || Main.map.mapView == null) {
            return 1;
        }
        MapView mv = Main.map.mapView;
        LatLon topLeft = mv.getLatLon(0, 0);
        LatLon botRight = mv.getLatLon(mv.getWidth(), mv.getHeight());
        double x1 = lonToTileX(topLeft.lon(), 1);
        double y1 = latToTileY(topLeft.lat(), 1);
        double x2 = lonToTileX(botRight.lon(), 1);
        double y2 = latToTileY(botRight.lat(), 1);

        int screenPixels = mv.getWidth() * mv.getHeight();
        double tilePixels = Math.abs((y2 - y1) * (x2 - x1) * 256 * 256);
        if (screenPixels == 0 || tilePixels == 0) {
            return 1;
        }
        double factor = screenPixels / tilePixels;
        double result = Math.log(factor) / Math.log(2) / 2 + 1;
        int intResult = (int) Math.floor(result);
        return intResult;
    }

    public static String getServerURL() {
        return Main.pref.get("iodb.server.url", "http://offsets.textual.ru/");
    }
}
